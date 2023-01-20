package org.processmining.stochasticlabelleddatapetrinet;

import java.util.BitSet;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataStateFactoryImpl;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSemantics;

/**
 * Lets an SLPN without data pretent to be a SLDPN
 * 
 * @author sander
 *
 */
public class StochasticLabelledPetriNet2StochasticLabelledDataPetriNet {

	public static StochasticLabelledDataPetriNetSemantics convert(final StochasticLabelledPetriNetSemantics semantics) {
		DataState dataState = new DataStateFactoryImpl(0).newDataState();

		return new StochasticLabelledDataPetriNetSemantics() {

			public void setState(byte[] state) {
				semantics.setState(state);
			}

			public void setInitialState(DataState initialDataState) {
				semantics.setInitialState();
			}

			public void setDataState(DataState dataState) {

			}

			public DataState newDataState() {
				return dataState;
			}

			public boolean isTransitionSilent(int transition) {
				return semantics.isTransitionSilent(transition);
			}

			public boolean isFinalState() {
				return semantics.isFinalState();
			}

			public VariableType getVariableType(int variable) {
				assert false;
				return null;
			}

			public String getVariableLabel(int variable) {
				assert false;
				return null;
			}

			public double getTransitionWeight(int transition) {
				return semantics.getTransitionWeight(transition);
			}

			public String getTransitionLabel(int transition) {
				return semantics.getTransitionLabel(transition);
			}

			public double getTotalWeightOfEnabledTransitions() {
				return semantics.getTotalWeightOfEnabledTransitions();
			}

			public byte[] getState() {
				return semantics.getState();
			}

			public int getNumberOfVariables() {
				return 0;
			}

			public BitSet getEnabledTransitions() {
				return semantics.getEnabledTransitions();
			}

			public DataState getDataState() {
				return dataState;
			}

			public void executeTransition(int transition, DataState dataEffect) {
				semantics.executeTransition(transition);
			}

			public StochasticLabelledDataPetriNetSemantics clone() {
				StochasticLabelledPetriNetSemantics result = semantics.clone();
				StochasticLabelledDataPetriNetSemantics result2 = convert(result);
				result.setState(getState());
				return result2;
			}
		};

	}
}