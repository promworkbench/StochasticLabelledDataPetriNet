package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.balancedconformance.BalancedDataXAlignmentPlugin;
import org.processmining.plugins.balancedconformance.config.BalancedProcessorConfiguration;
import org.processmining.plugins.balancedconformance.controlflow.ControlFlowAlignmentException;
import org.processmining.plugins.balancedconformance.dataflow.exception.DataAlignmentException;
import org.processmining.plugins.balancedconformance.export.XAlignmentConverter;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeights;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependent;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter.PetrinetMarkedWithMappings;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetUtils;
import org.processmining.stochasticlabelleddatapetrinet.weights.LogisticWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.fitting.weka.WekaUtil;
import org.processmining.xesalignmentextension.XAlignmentExtension;
import org.processmining.xesalignmentextension.XAlignmentExtension.XAlignment;

import com.google.common.collect.MultimapBuilder.SetMultimapBuilder;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Fits a logistic regression using WEKA to the observations and adds the
 * respective WeightFunction to the transitions. Assumes that transition labels
 * and variable names in the given SLDPN exactly match the attributes in the
 * event log based on the provided classifier.
 * 
 * @author F. Mannhardt
 *
 */
public class LogisticRegressionWeightFitter implements WeightFitter {

	private final XEventClassifier classifier;

	private int defaultMoveOnModelCost = 1;
	private int defaultMoveOnLogCost = 1;
	private int defaultMissingWriteOpCost = 1;
	private int defaultIncorrectWriteOpCost = 1;

	private final boolean keepEvaluation;
	private final Map<Integer, Evaluation> evaluations = new HashMap<>();

	public LogisticRegressionWeightFitter(XEventClassifier classifier) {
		this(classifier, true);
	}

	public LogisticRegressionWeightFitter(XEventClassifier classifier, boolean keepEvaluation) {
		super();
		this.keepEvaluation = keepEvaluation;
		this.classifier = classifier;
	}

	public Map<Integer, Evaluation> getEvaluations() {
		return evaluations;
	}

	@Override
	public StochasticLabelledDataPetriNetWeights fit(XLog log, StochasticLabelledDataPetriNet net)
			throws WeightFitterException {

		StochasticLabelledDataPetriNetWeightsDataDependent sldpnWeights = new StochasticLabelledDataPetriNetWeightsDataDependent(
				net);

		// this is guessing a final marking
		PetrinetMarkedWithMappings markedPN = PetrinetConverter.viewAsPetrinet(net); 

		try {
			// using default alignment
			Iterable<XAlignment> alignIter = alignLog(log, markedPN);

			Map<String, Integer> eventClass2TransitionIdx = builderEventClassMapping(net, markedPN);

			Map<String, Integer> variableIdx = new HashMap<>();
			for (int i = 0; i < net.getNumberOfVariables(); i++) {
				variableIdx.put(net.getVariableLabel(i), i);
			}
			
			// build map of which variable is written where
			SetMultimap<Integer, String> variablesWritten = buildVariablesWritten(net);			
			// consider all attributes
			Set<String> attributesConsidered = variableIdx.keySet();

			ObservationInstanceBuilder builder = createObservationBuilder(net, alignIter);

			ProjectedLog projectedLog = builder.buildProjectedLog(variablesWritten, attributesConsidered,
					eventClass2TransitionIdx);

			Map<Integer, Multiset<Map<Integer, Object>>> instancesMultimap = builder.buildInstancesMultimap(projectedLog,
					eventClass2TransitionIdx);

			for (int tIdx = 0; tIdx < net.getNumberOfTransitions(); tIdx++) {

				Instances wekaInstances = builder.buildInstances(tIdx, instancesMultimap);
				
				System.out.println(String.format("WeightFitter: Building logistic model from %.0f instances",  wekaInstances.sumOfWeights()));
				
				// We need samples for both cases to infer a meaningful function
				if (wekaInstances.numDistinctValues(0) > 1) {
					try {
						Logistic logistic = new weka.classifiers.functions.Logistic();
						logistic.setMaxIts(1000);
						logistic.buildClassifier(wekaInstances);
						
						if (keepEvaluation) {
							Evaluation eval = new weka.classifiers.evaluation.Evaluation(wekaInstances);
							eval.evaluateModel(logistic, wekaInstances);
							evaluations.put(tIdx, eval);
							System.out.println(String.format("%s (%s): roc: %.4f, prc: %.4f, MAE: %.4f, distribution: %.0f, %.0f", 
									tIdx, net.getTransitionLabel(tIdx),
									eval.areaUnderROC(0), eval.areaUnderPRC(0), eval.meanAbsoluteError(), 
									eval.getClassPriors()[0], eval.getClassPriors()[1]));
						}

						double[][] coefficients = logistic.coefficients();

						assert coefficients[0].length == 1 : "We expect only one intercept as we only have two classes ";
						double intercept = coefficients[0][0];

						double[] weightCoeff = new double[net.getNumberOfVariables()];

						Instances internalInstances = getInternalFilteredInstances(logistic);
						
						// skip class attribute, which is first by convention!
						for (int i = 1; i < coefficients.length; i++) {

							assert coefficients[i].length == 1 : "We expect coefficients to be scalars";
							
							assert internalInstances.classIndex() == 0;

							Attribute attr = internalInstances.attribute(i);

							weightCoeff[Integer.valueOf(attr.name())] = coefficients[i][0];
						}

						sldpnWeights.setWeightFunction(tIdx, new LogisticWeightFunction(intercept, weightCoeff));

					} catch (Exception e1) {
						throw new WeightFitterException(e1);
					}
				} else {
					System.out.println("Instances only recorded for one class, no fitting possible!");
				}
			}

		} catch (ControlFlowAlignmentException | DataAlignmentException | WeightFitterException e) {
			throw new WeightFitterException(e);
		}

		return sldpnWeights;
	}

	private Instances getInternalFilteredInstances(Logistic logistic) throws NoSuchFieldException, IllegalAccessException {
		// This is not expose by WEKA but we need it as we need to know the mapping from coefficient to variables/attributes
		Field f = Logistic.class.getDeclaredField("m_structure");
		f.setAccessible(true);
		Instances internalInstances = (Instances) f.get(logistic);
		return internalInstances;
	}

	private Map<String, Integer> builderEventClassMapping(StochasticLabelledDataPetriNet net,
			PetrinetMarkedWithMappings markedPN) {
		Map<String, Integer> eventClass2TransitionIdx = new HashMap<>();
		for (int i = 0; i < net.getNumberOfTransitions(); i++) {
			eventClass2TransitionIdx.put(markedPN.getTransitionIndexToId().get(i), i);
		}
		return eventClass2TransitionIdx;
	}

	private ObservationInstanceBuilder createObservationBuilder(StochasticLabelledDataPetriNet net,
			Iterable<XAlignment> alignIter) {
		Map<String, Object> initialValues = Map.of();
		Map<String, Class<?>> variableClasses = new HashMap<>();
		Map<String, VariableType> variableTypes = new HashMap<>();

		for (int i = 0; i < net.getNumberOfVariables(); i++) {
			VariableType variableType = net.getVariableType(i);
			variableTypes.put(net.getVariableLabel(i), variableType);
			variableClasses.put(net.getVariableLabel(i), variableType.getJavaClass());
		}

		return new ObservationInstanceBuilder(net, alignIter, initialValues, variableClasses, variableTypes);
	}

	private SetMultimap<Integer, String> buildVariablesWritten(StochasticLabelledDataPetriNet net) {
		SetMultimap<Integer, String> variablesWritten = SetMultimapBuilder.hashKeys().hashSetValues().build();
		for (int i = 0; i < net.getNumberOfTransitions(); i++) {
			int[] variablesWrittenIdx = net.getWriteVariables(i);
			variablesWritten.putAll(i,
					Arrays.stream(variablesWrittenIdx).mapToObj((int varIdx) -> net.getVariableLabel(varIdx)).toList());
		}
		return variablesWritten;
	}

	private Iterable<XAlignment> alignLog(XLog log, PetrinetMarkedWithMappings markedPN)
			throws ControlFlowAlignmentException, DataAlignmentException {
		BalancedProcessorConfiguration config = configureAlignment(log, markedPN);
		BalancedDataXAlignmentPlugin alignmentPlugin = new BalancedDataXAlignmentPlugin();
		alignmentPlugin.setConverter(new XAlignmentConverter()); // use plain XES 
		XLog alignedLog = alignmentPlugin.alignLog(markedPN.getNet(), log, config);
		Iterable<XAlignment> alignIter = XAlignmentExtension.instance().extendLog(alignedLog);
		return alignIter;
	}

	protected BalancedProcessorConfiguration configureAlignment(XLog log, PetrinetMarkedWithMappings markedPN) {
		//TODO let marking be configureable
		Marking[] finalMarkings = List.of(PetrinetUtils.guessFinalMarking(markedPN.getNet())).toArray(new Marking[1]);
		return BalancedProcessorConfiguration.newDefaultInstance(markedPN.getNet(), markedPN.getInitialMarking(),
				finalMarkings, log, classifier, defaultMoveOnModelCost, defaultMoveOnLogCost, defaultMissingWriteOpCost,
				defaultIncorrectWriteOpCost);
	}

}
