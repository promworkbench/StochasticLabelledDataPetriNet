/*
 * Copyright (c) 2013 F. Mannhardt (f.mannhardt@tue.nl)
 */
package org.processmining.stochasticlabelleddatapetrinet.datastate;

/**
 * Stores the values of a fixed number of variables that are of the
 * types Long or Double.
 * 
 * @author F. Mannhardt
 * 
 */
public interface DataState {

	void putDouble(int varIdx, double value);

	void putLong(int varIdx, long value);

	long getLong(int varIdx);
	
	double getDouble(int varIdx);

	Double tryGetDouble(int varIdx);

	Long tryGetLong(int varIdx);

	boolean contains(int varIdx);

	boolean isEmpty();

	int capacity();
	
	int size();
	
	/**
	 * Set all variables to unset
	 */
	void clear();
	
	/**
	 * Updates the values of this instance with the non-null ones in the supplied one.
	 * 
	 * @param dataEffect
	 */
	void update(DataState dataEffect);
	
	/**
	 * @return an immutable version of this state (might return a clone or the
	 *         original)
	 */
	DataState makeStateImmutable();

	DataState deepCopy();

}
