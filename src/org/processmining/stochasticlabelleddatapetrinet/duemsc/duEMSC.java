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
		int size = log.size();
		TObjectIntMap<String[]> activitySequences = getActivitySequences(log, classifier);
		TObjectIntMap<DataState[]> dataSequences = getDataSequences(log, logAdapter);

		BigDecimal sum = BigDecimal.ZERO;

		for (TObjectIntIterator<String[]> itAs = activitySequences.iterator(); itAs.hasNext();) {
			itAs.advance();

			String[] activitySequence = itAs.key();
			int activitySequenceWeight = itAs.value();
			BigDecimal activitySequenceProbabilityLog = BigDecimal.valueOf(activitySequenceWeight)
					.divide(BigDecimal.valueOf(size), mc);

			for (TObjectIntIterator<DataState[]> itDs = dataSequences.iterator(); itDs.hasNext();) {
				itDs.advance();

				DataState[] dataSequence = itDs.key();
				int dataSequenceWeight = itDs.value();
				BigDecimal dataSequenceProbabilityLog = BigDecimal.valueOf(dataSequenceWeight)
						.divide(BigDecimal.valueOf(size), mc);

				//perform a single unit-based comparison
				sum = sum.add(singleComparison(semantics, canceller, mc, activitySequence,
						activitySequenceProbabilityLog, dataSequence, dataSequenceProbabilityLog));
			}
		}

		return BigDecimal.ONE.subtract(sum).doubleValue();
	}

	private static BigDecimal singleComparison(StochasticLabelledDataPetriNetSemantics semantics,
			ProMCanceller canceller, MathContext mc, String[] activitySequence,
			BigDecimal activitySequenceProbabilityLog, DataState[] dataSequence, BigDecimal dataSequenceProbabilityLog)
			throws LpSolveException {

		//get the log probability
		BigDecimal probabilityLog = activitySequenceProbabilityLog;

		//get the model probability
		BigDecimal probabilityModel = BigDecimal
				.valueOf(TraceProbablility.getTraceProbability(semantics, activitySequence, dataSequence, canceller));

		BigDecimal difference = probabilityLog.subtract(probabilityModel).max(BigDecimal.ZERO);

		System.out.println(Arrays.toString(activitySequence) + " log: " + probabilityLog + " model: " + probabilityModel
				+ " weighing: " + dataSequenceProbabilityLog);

		//scale the difference by the likelihood of the data sequence
		return difference.multiply(dataSequenceProbabilityLog, mc);
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