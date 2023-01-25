package org.processmining.stochasticlabelleddatapetrinet.probability;

import java.util.ArrayDeque;
import java.util.BitSet;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelledpetrinets.probability.CrossProductResult;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class CrossProductSLDPN {

	private static class Z<B, BD> {
		int stateCounter = 0;
		StochasticLabelledDataPetriNetSemantics semanticsA;
		TObjectIntMap<ABDataState<B, BD>> seen = new TObjectIntHashMap<>(10, 0.5f, -1);
		ArrayDeque<ABDataState<B, BD>> worklist = new ArrayDeque<>();
	}

	private static class Y {
		TIntList outgoingStates = new TIntArrayList();
		TDoubleList outgoingStateProbabilities = new TDoubleArrayList();
	}

	public static <B> void traverse(StochasticLabelledDataPetriNetSemantics semanticsA,
			FollowerSemanticsData<B, DataState> systemB, CrossProductResult result, ProMCanceller canceller) {
		Z<B, DataState> z = new Z<>();
		Y y = new Y();
		z.semanticsA = semanticsA;

		//initialise
		{
			B initialBState = systemB.getInitialState().getA();
			DataState initialBDataState = systemB.getInitialState().getB();

			semanticsA.setInitialState(initialBDataState);

			ABDataState<B, DataState> state = new ABDataState<B, DataState>(z.semanticsA.getState(), initialBState,
					initialBDataState);

			z.worklist.add(state);
			z.seen.put(state, z.stateCounter);
			result.reportInitialState(z.stateCounter);
			z.stateCounter++;
		}

		int deadStateA = z.stateCounter;
		z.stateCounter++;
		result.reportDeadState(deadStateA);

		BitSet enabledTransitions = new BitSet();

		while (!z.worklist.isEmpty()) {
			ABDataState<B, DataState> stateAB = z.worklist.pop();
			int stateABindex = z.seen.get(stateAB);

			z.semanticsA.setState(stateAB.getStateA());
			z.semanticsA.setDataState(stateAB.getDataStateB());

			if (z.semanticsA.isFinalState()) {
				if (systemB.isFinalState(stateAB.getStateB())) {
					result.reportFinalState(stateABindex);
				} else {
					TIntList nextStates = new TIntArrayList();
					nextStates.add(deadStateA);
					TDoubleList nextProbabilities = new TDoubleArrayList();
					nextProbabilities.add(1);
					//B is not ready; report this as a dead end
					result.reportNonFinalState(stateABindex, nextStates, nextProbabilities);
				}
			} else {
				enabledTransitions.clear();
				enabledTransitions.or(z.semanticsA.getEnabledTransitions());
				double totalWeight = z.semanticsA.getTotalWeightOfEnabledTransitions();

				y.outgoingStates.clear();
				y.outgoingStateProbabilities.clear();

				for (int transition = enabledTransitions.nextSetBit(0); transition >= 0; transition = enabledTransitions
						.nextSetBit(transition + 1)) {

					z.semanticsA.setState(stateAB.getStateA());
					z.semanticsA.setDataState(stateAB.getDataStateB());

					if (z.semanticsA.isTransitionSilent(transition)) {
						//silent transition; only A takes a step; no data step
						B newStateB = stateAB.getStateB();
						DataState newDataStateB = stateAB.getDataStateB();

						z.semanticsA.executeTransition(transition, stateAB.getDataStateB());
						byte[] newStateA = z.semanticsA.getState();

						processNewState(z, y, totalWeight, transition, newStateA, newStateB, newDataStateB);
					} else {
						//labelled transition; both A and B need to take steps
						if (systemB.isFinalState(stateAB.getStateB())) {
							//B cannot take a further step, so this is a dead end
							z.semanticsA.executeTransition(transition, stateAB.getDataStateB());
							y.outgoingStates.add(deadStateA);
							y.outgoingStateProbabilities
									.add(z.semanticsA.getTransitionWeight(transition) / totalWeight);
						} else {
							B newStateB = systemB.takeStep(stateAB.getStateB(),
									z.semanticsA.getTransitionLabel(transition));
							if (newStateB != null) {
								DataState newDataStateB = systemB.getDataStateAfter(stateAB.getStateB());
								z.semanticsA.executeTransition(transition, stateAB.getDataStateB());
								byte[] newStateA = z.semanticsA.getState();

								processNewState(z, y, totalWeight, transition, newStateA, newStateB, newDataStateB);
							} else {
								//dead state
								y.outgoingStates.add(deadStateA);
								y.outgoingStateProbabilities
										.add(z.semanticsA.getTransitionWeight(transition) / totalWeight);
							}
						}
					}
				}

				result.reportNonFinalState(stateABindex, y.outgoingStates, y.outgoingStateProbabilities);
			}

			if (canceller.isCancelled()) {
				return;
			}
		}
	}

	private static <B> void processNewState(Z<B, DataState> z, Y y, double totalWeight, int transition,
			byte[] newStateA, B newStateB, DataState newDataStateB) {
		ABDataState<B, DataState> newStateAB = new ABDataState<B, DataState>(newStateA, newStateB, newDataStateB);
		int newStateIndex = z.seen.adjustOrPutValue(newStateAB, 0, z.stateCounter);
		if (newStateIndex == z.stateCounter) {
			//newStateAB was not encountered before
			z.stateCounter++;
			z.worklist.add(newStateAB);
		}

		y.outgoingStates.add(newStateIndex);
		y.outgoingStateProbabilities.add(z.semanticsA.getTransitionWeight(transition) / totalWeight);
	}
}