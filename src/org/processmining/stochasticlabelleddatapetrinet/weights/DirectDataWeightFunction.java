package org.processmining.stochasticlabelleddatapetrinet.weights;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

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
	
	public DirectDataWeightFunction() {
		super(); // for deserialization, needs to be public to be accessible by serializer
	}

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
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(0); // version reserved
		dos.writeInt(variableIdx);
		dos.flush();
	}

	public void deserialize(InputStream is) throws IOException {		
		DataInputStream dis = new DataInputStream(is);
		dis.readInt();
		variableIdx = dis.readInt();
	}

	public int hashCode() {
		return Objects.hash(variableIdx);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DirectDataWeightFunction other = (DirectDataWeightFunction) obj;
		return variableIdx == other.variableIdx;
	}

	public String toString() {
		return "DirectDataWeightFunction based on variable with index " + variableIdx;
	}

}
