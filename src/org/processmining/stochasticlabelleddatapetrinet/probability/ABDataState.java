package org.processmining.stochasticlabelleddatapetrinet.probability;

import java.util.Arrays;

import org.processmining.stochasticlabelledpetrinets.probability.ABState;

public class ABDataState<B, BD> extends ABState<B> {

	private final BD dataStateB;

	public ABDataState(byte[] stateA, B stateB, BD dataStateB) {
		super(stateA, stateB);
		this.dataStateB = dataStateB;
	}

	public String toString() {
		return Arrays.toString(getStateA()) + "-(D)-" + getStateB().toString();
	}

	public BD getDataStateB() {
		return dataStateB;
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + dataStateB.hashCode();
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ABDataState<?, ?> other = (ABDataState<?, ?>) obj;
		return dataStateB.equals(other.dataStateB);
	}

}