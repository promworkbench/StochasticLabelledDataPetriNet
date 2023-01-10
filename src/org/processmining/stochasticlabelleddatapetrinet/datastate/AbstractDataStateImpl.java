package org.processmining.stochasticlabelleddatapetrinet.datastate;

public abstract class AbstractDataStateImpl implements DataState {

	protected abstract void putInternal(int varIdx, long value);

	protected abstract long getInternal(int varIdx);

	public final void putDouble(int varIdx, double value) {
		putInternal(varIdx, convertFromDouble(value));
	}

	public final void putLong(int varIdx, long value) {
		putInternal(varIdx, value);
	}

	public final long getLong(int varIdx) {
		return getInternal(varIdx);
	}

	public final double getDouble(int varIdx) {
		return Double.longBitsToDouble(getInternal(varIdx));
	}

	public final Double tryGetDouble(int varIdx) {
		if (contains(varIdx)) {
			return convertToDouble(getInternal(varIdx));	
		} else {
			return null;
		}
	}

	public final Long tryGetLong(int varIdx) {
		if (contains(varIdx)) {
			return getInternal(varIdx);	
		} else {
			return null;
		}
	}

	private static double convertToDouble(long l) {
		return Double.longBitsToDouble(l);
	}

	private static long convertFromDouble(double d) {
		return Double.doubleToLongBits(d);
	}

}