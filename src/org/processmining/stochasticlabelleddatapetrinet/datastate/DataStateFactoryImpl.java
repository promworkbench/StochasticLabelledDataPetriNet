package org.processmining.stochasticlabelleddatapetrinet.datastate;

public final class DataStateFactoryImpl implements DataStateFactory {

	private int capacity;

	public DataStateFactoryImpl(int capacity) {
		this.capacity = capacity;
	}

	public DataState newDataState() {
		return new DataStateImpl(capacity);
	}

}