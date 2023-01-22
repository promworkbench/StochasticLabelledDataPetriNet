package org.processmining.stochasticlabelleddatapetrinet.weights.fitting;

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

	public LogisticRegressionWeightFitter(XEventClassifier classifier) {
		super();
		this.classifier = classifier;
	}

	@Override
	public StochasticLabelledDataPetriNetWeights fit(XLog log, StochasticLabelledDataPetriNet net)
			throws WeightFitterException {

		StochasticLabelledDataPetriNetWeightsDataDependent sldpnWeights = new StochasticLabelledDataPetriNetWeightsDataDependent(
				net);

		PetrinetMarkedWithMappings markedPN = PetrinetConverter.viewAsPetrinet(net); // this is guessing a final marking

		try {

			// using default alignment
			Iterable<XAlignment> alignIter = alignLog(log, markedPN);

			Map<String, Integer> eventClass2TransitionIdx = builderEventClassMapping(net, markedPN);

			SetMultimap<Integer, String> variablesWritten = buildVariablesWritten(net);
			// TODO support filtering of attributes 
			Set<String> attributesConsidered = Set.copyOf(variablesWritten.values());

			Map<String, Integer> variableIdx = new HashMap<>();
			for (int i = 0; i < net.getNumberOfVariables(); i++) {
				variableIdx.put(net.getVariableLabel(i), i);
			}

			ObservationInstanceBuilder builder = createObservationBuilder(net, alignIter);

			ProjectedLog projectedLog = builder.buildProjectedLog(variablesWritten, attributesConsidered,
					eventClass2TransitionIdx);

			Map<Integer, Multiset<Map<String, Object>>> instancesMultimap = builder.buildInstancesMultimap(projectedLog,
					eventClass2TransitionIdx);

			for (int tIdx = 0; tIdx < net.getNumberOfTransitions(); tIdx++) {

				Instances wekaInstances = builder.buildInstances(tIdx, instancesMultimap);

				// We need samples for both cases to infer a meaningful function
				if (wekaInstances.numDistinctValues(0) > 1) {
					try {
						Logistic logistic = new weka.classifiers.functions.Logistic();
						logistic.buildClassifier(wekaInstances);

						double[][] coefficients = logistic.coefficients();

						assert coefficients[0].length == 1 : "We expect only one intercept as we only have two classes ";
						double intercept = coefficients[0][0];

						double[] weightCoeff = new double[net.getNumberOfVariables()];
						
						// skip class attribute, which is first by convention!
						for (int i = 1; i < coefficients.length; i++) {

							assert coefficients[i].length == 1 : "We expect coefficients to be scalars";
							assert wekaInstances.classIndex() == 0;

							Attribute attr = wekaInstances.attribute(i);
							Integer varIdxInModel = variableIdx.get(WekaUtil.wekaUnescape(attr.name())); //TODO this is still dangerous since escape<->unescape is not lossless in all cases! 

							weightCoeff[varIdxInModel] = coefficients[i][0];
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
			variableClasses.put(net.getVariableLabel(i), typeToClass(variableType));
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
		XLog alignedLog = new BalancedDataXAlignmentPlugin().alignLog(markedPN.getNet(), log, config);
		Iterable<XAlignment> alignIter = XAlignmentExtension.instance().extendLog(alignedLog);
		return alignIter;
	}

	private Class<?> typeToClass(VariableType variableType) {
		//TODO
		return Double.class;
	}

	protected BalancedProcessorConfiguration configureAlignment(XLog log, PetrinetMarkedWithMappings markedPN) {
		//TODO let marking be configureable
		Marking[] finalMarkings = List.of(PetrinetUtils.guessFinalMarking(markedPN.getNet())).toArray(new Marking[1]);
		return BalancedProcessorConfiguration.newDefaultInstance(markedPN.getNet(), markedPN.getInitialMarking(),
				finalMarkings, log, classifier, defaultMoveOnModelCost, defaultMoveOnLogCost, defaultMissingWriteOpCost,
				defaultIncorrectWriteOpCost);
	}

}
