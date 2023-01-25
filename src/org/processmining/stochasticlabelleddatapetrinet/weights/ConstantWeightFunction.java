package org.processmining.stochasticlabelleddatapetrinet.weights;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

/**
 * Weight is constant regardless of data values.
 * 
 * @author F. Mannhardt
 *
 */
public class ConstantWeightFunction implements SerializableWeightFunction {
	
	private double weight;
	
	public ConstantWeightFunction() {
		super();
		this.weight = 1;
	}

	public ConstantWeightFunction(double weight) {
		this.weight = weight;
	}

	public double evaluateWeight(StochasticLabelledDataPetriNet net, DataState dataState) {
		return weight;
	}

	public void serialize(OutputStream os) throws IOException {
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeInt(0); // version reserved
		dos.writeDouble(weight);
		dos.flush();
	}

	public void deserialize(InputStream is) throws IOException {		
		DataInputStream dis = new DataInputStream(is);
		dis.readInt();
		weight = dis.readDouble();
	}

	public int hashCode() {
		return Objects.hash(weight);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConstantWeightFunction other = (ConstantWeightFunction) obj;
		return Double.doubleToLongBits(weight) == Double.doubleToLongBits(other.weight);
	}

	public String toString() {
		return "ConstantWeightFunction with weight + weight";
	}
}
