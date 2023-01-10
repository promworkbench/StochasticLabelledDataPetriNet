package org.processmining.stochasticlabelleddatapetrinet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

/**
 * Weight is based directly on a data value. Does probably not make much sense except for testing.
 * 
 * @author F. Mannhardt
 *
 */
public class DirectDataWeightFunction implements SerializableWeightFunction {

	private static final double EPSILON = 1E-10; // to avoid zero weight
	
	private int variableIdx;

	/**
	 * Constructs the function to read the weight from the Double value of a specific variable.
	 * 
	 * @param variableIdx of a numeric variable
	 */
	public DirectDataWeightFunction(int variableIdx) {
		super();
		this.variableIdx = variableIdx;
	}

	public double evaluateWeight(DataState dataState) {
		double val = dataState.getDouble(variableIdx);
		if (val == 0) {
			return EPSILON;
		} else {
			return Math.abs(val);			
		}
	}

	public void serialize(OutputStream os) throws IOException {
		try (DataOutputStream dos = new DataOutputStream(os)) {
			dos.writeInt(variableIdx);
		}		
	}

	public void deserialize(InputStream is) throws IOException {		
		try (DataInputStream dis = new DataInputStream(is)) {
			variableIdx = dis.readInt();
		}
	}

}
