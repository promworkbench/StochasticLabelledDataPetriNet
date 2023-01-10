package org.processmining.stochasticlabelleddatapetrinet.datastate;

import java.util.Arrays;


final class DataStateImpl extends AbstractDataStateImpl {

	private final long[] values;	
	private final int capacity;
	
	private int hash = 0;

	public DataStateImpl(int capacity) {
		super();
		this.capacity = capacity;		
		this.values = new long[capacity + sizeBitset()];
	}

	DataStateImpl(long[] values, int capacity) {
		super();		
		this.capacity = capacity;
		this.values = values;
		assert values.length == capacity + sizeBitset();
	}

	// ***********************************
	// Borrowed from Lucene project (OpenBitSet) under Apache 2.0 license 
	private final void mark(int index) {
		assert index < capacity;
		int wordNum = index >> 6;
		long bitmask = 1L << index;
		assert wordNum < sizeBitset();
		values[wordNum] |= bitmask;
	}

	private final void unmark(int index) {
		assert index < capacity;
		int wordNum = index >> 6;
		long bitmask = 1L << index;
		assert wordNum < sizeBitset();
		values[wordNum] &= ~bitmask;
	}

	private final boolean isMarked(int index) {
		assert index < capacity;
		int wordNum = index >> 6;
		long bitmask = 1L << index;
		assert wordNum < sizeBitset();
		return (values[wordNum] & bitmask) != 0;
	}

	private final int nextSetBit(int index, int sizeBitset) {
		if (values.length == 0) {
			return -1;
		}
		int wordNum = index >> 6;
		long word = values[wordNum] >> index; // skip all the bits to the right of index
		if (word != 0) {
			return index + Long.numberOfTrailingZeros(word);
		}
		while (++wordNum < sizeBitset) {
			word = values[wordNum];
			if (word != 0) {
				return (wordNum << 6) + Long.numberOfTrailingZeros(word);
			}
		}
		return -1;
	}

	// ***********************************

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public int size() {
		// Borrowed from Lucene project (BitUtil) under Apache 2.0 license 
		final int sizeBitset = sizeBitset();
		int count = 0;
		for (int i = 0, end = sizeBitset; i < end; ++i) {
			count += Long.bitCount(values[i]);
		}
		return count;
	}

	private int sizeBitset() {
		return ((capacity - 1) >> 6) + 1;
	}
	
	@Override
	public int capacity() {
		return capacity;
	}	

	@Override
	public DataState deepCopy() {
		return new DataStateImpl(Arrays.copyOf(values, values.length), capacity);
	}

	@Override
	public DataState makeStateImmutable() {
		hash = hashCode();
		return this;
	}

	@Override
	protected void putInternal(int varIdx, long value) {
		mark(varIdx); //mark original index 
		values[varIdx + sizeBitset()] = value; // adjust for bitset
	}

	@Override
	public final boolean contains(int varIdx) {
		return isMarked(varIdx);
	}

	@Override
	protected long getInternal(int varIdx) {
		if (isMarked(varIdx)) {
			return values[varIdx + sizeBitset()];  // adjust for bitset
		} else {
			throw new RuntimeException("DataState does not contain variable " + varIdx);
		}
	}
	
	public void clear() {
		for (int i = 0; i < capacity; ++i) {
			unmark(i);
		}
	}	
	
	public void update(DataState dataEffect) {
		if (dataEffect.capacity() != capacity()) {
			throw new RuntimeException("Can only update DataState over the same set of variables");
		}
		for (int i = 0; i < capacity(); i++) {
			if (dataEffect.contains(i)) {
				putInternal(i, dataEffect.getLong(i));
			} // do nothing with unset variables, we never remove a variable through updating
		}
	}	

	@Override
	public int hashCode() {
		if (hash != 0) {
			return hash;
		}
		return Arrays.hashCode(values);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DataStateImpl other = (DataStateImpl) obj;
		if (!valuesEquals(other)) {
			return false;
		}
		return true;
	}

	private final boolean valuesEquals(final DataStateImpl other) {
		for (int i = 0; i < values.length; i++) {
			if (values[i] != other.values[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("[");
		int sizeBitset = sizeBitset();
		for (int i = nextSetBit(0, sizeBitset); i >= 0; i = nextSetBit(i + 1, sizeBitset)) {
			long value = values[i + sizeBitset];
			sb.append(i);
			sb.append("=");
			sb.append(String.valueOf(value));
			sb.append(", ");
		}
		if (sb.length() > 1) {
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append("]");
		return sb.toString();
	}

}