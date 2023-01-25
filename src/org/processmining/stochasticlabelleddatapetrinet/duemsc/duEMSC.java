package org.processmining.stochasticlabelleddatapetrinet.duemsc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;
import org.processmining.stochasticlabelleddatapetrinet.probability.TraceProbablility;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;
import lpsolve.LpSolveException;

public class duEMSC {
	public static double compute(XLog log, XEventClassifier classifier, DataStateLogAdapter logAdapter,
			StochasticLabelledDataPetriNetSemantics semantics, ProMCanceller canceller) throws LpSolveException {

		MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

		//gather data
		int logSize = log.size();
		TObjectIntMap<String[]> activitySequences = getActivitySequences(log, classifier);
		TObjectIntMap<DataState[]> dataSequences = getDataSequences(log, logAdapter);

		BigDecimal sum = BigDecimal.ZERO;

		for (TObjectIntIterator<String[]> itAs = activitySequences.iterator(); itAs.hasNext();) {
			itAs.advance();

			String[] activitySequence = itAs.key();
			int activitySequenceWeight = itAs.value();
			BigDecimal activitySequenceProbabilityLog = BigDecimal.valueOf(activitySequenceWeight)
					.divide(BigDecimal.valueOf(logSize), mc);

			BigDecimal activitySequenceProbabilityModel = queryModelForTrace(semantics, canceller, mc, activitySequence,
					dataSequences, logSize);
			
			BigDecimal difference = activitySequenceProbabilityLog.subtract(activitySequenceProbabilityModel)
					.max(BigDecimal.ZERO);

			//			System.out.println("log: " + activitySequenceProbabilityLog + " model: " + activitySequenceProbabilityModel
			//					+ " difference: " + difference + " trace " + Arrays.toString(activitySequence));

			sum = sum.add(difference);
		}

		return BigDecimal.ONE.subtract(sum).doubleValue();
	}

	private static BigDecimal queryModelForTrace(StochasticLabelledDataPetriNetSemantics semantics,
			ProMCanceller canceller, MathContext mc, String[] activitySequence,
			TObjectIntMap<DataState[]> dataSequences, int logSize) throws LpSolveException {
		BigDecimal sum = BigDecimal.ZERO;
		for (TObjectIntIterator<DataState[]> itDs = dataSequences.iterator(); itDs.hasNext();) {
			itDs.advance();

			DataState[] dataSequence = itDs.key();
			int dataSequenceWeight = itDs.value();
			BigDecimal dataSequenceProbabilityLog = BigDecimal.valueOf(dataSequenceWeight)
					.divide(BigDecimal.valueOf(logSize), mc);

			//get the model probability
			BigDecimal probabilityConditionalModel = BigDecimal.valueOf(
					TraceProbablility.getTraceProbability(semantics, activitySequence, dataSequence, canceller));

			sum = sum.add(probabilityConditionalModel.multiply(dataSequenceProbabilityLog));
			
//			System.out.println("    trace+data done");
		}
		
//		System.out.println("  trace done");
		
		//		TObjectIntIterator<DataState[]> it = dataSequences.iterator();
		//		it.advance();
		//		CrossProductResultDot result2 = new CrossProductResultDot();
		//		FollowerSemanticsDataImpl systemB2 = new FollowerSemanticsDataImpl(activitySequence, it.key());
		//		CrossProductSLDPN.traverse(semantics, systemB2, result2, canceller);
		//		System.out.println(result2.toDot());

		return sum;
	}

	public static TObjectIntMap<String[]> getActivitySequences(XLog log, XEventClassifier classifier) {
		TObjectIntMap<String[]> activitySequences = new TObjectIntCustomHashMap<>(new HashingStrategy<String[]>() {
			private static final long serialVersionUID = 1L;

			public int computeHashCode(String[] object) {
				return Arrays.hashCode(object);
			}

			public boolean equals(String[] o1, String[] o2) {
				return Arrays.equals(o1, o2);
			}
		});
		for (XTrace trace : log) {
			String[] activityTrace = TraceProbablility.getActivitySequence(trace, classifier);
			activitySequences.adjustOrPutValue(activityTrace, 1, 1);
		}
		return activitySequences;
	}

	public static TObjectIntMap<DataState[]> getDataSequences(XLog log, DataStateLogAdapter logAdapter) {
		TObjectIntMap<DataState[]> dataSequences = new TObjectIntCustomHashMap<>(new HashingStrategy<DataState[]>() {
			private static final long serialVersionUID = 1L;

			public int computeHashCode(DataState[] object) {
				return Arrays.hashCode(object);
			}

			public boolean equals(DataState[] o1, DataState[] o2) {
				return Arrays.equals(o1, o2);
			}
		});
		for (XTrace trace : log) {

			DataState[] dataTrace = TraceProbablility.getDataSequence(trace, logAdapter);
			dataSequences.adjustOrPutValue(dataTrace, 1, 1);
		}
		return dataSequences;
	}
}