package org.processmining.stochasticlabelleddatapetrinet.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet.VariableType;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependent;
import org.processmining.stochasticlabelleddatapetrinet.weights.ConstantWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.DirectDataWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.LinearWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.LogisticWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.SerializableWeightFunction;
import org.processmining.stochasticlabelleddatapetrinet.weights.WeightFunction;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsEditable;
import org.processmining.stochasticlabelledpetrinets.StochasticLabelledPetriNetSimpleWeightsImpl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class StochasticLabelledDataPetriNetSerializerImpl implements StochasticLabelledDataPetriNetSerializer<StochasticLabelledDataPetriNetWeightsDataDependent> {

	private static final int VERSION = 1;

	private static final int EOT = -1;
	private static final int EOF = Integer.MAX_VALUE;

	private static BiMap<Class<? extends SerializableWeightFunction>, Integer> WEIGHT_FUNCTIONS = 
			HashBiMap.create(Map.of(
					ConstantWeightFunction.class, 1, 
					DirectDataWeightFunction.class, 2,
					LinearWeightFunction.class, 3, 
					LogisticWeightFunction.class, 4));

	@Override
	public void serialize(StochasticLabelledDataPetriNetWeightsDataDependent net, OutputStream os) throws IOException {
		
		DataOutputStream dos = new DataOutputStream(os);
		
		dos.writeInt(VERSION);
		
		dos.writeInt(net.getNumberOfPlaces());
		for (int place = 0; place < net.getNumberOfPlaces(); place++) {
			dos.writeInt(net.isInInitialMarking(place));
		}
		
		dos.writeInt(net.getNumberOfTransitions());

		for (int transition = 0; transition < net.getNumberOfTransitions(); transition++) {

			dos.writeBoolean(net.isTransitionSilent(transition));
			if (!net.isTransitionSilent(transition)) {
				dos.writeUTF(net.getTransitionLabel(transition));
			}
			
			dos.writeInt(net.getInputPlaces(transition).length);
			for (int place : net.getInputPlaces(transition)) {
				dos.writeInt(place);
			}

			dos.writeInt(net.getOutputPlaces(transition).length);
			for (int place : net.getOutputPlaces(transition)) {
				dos.writeInt(place);
			}

			dos.writeInt(net.getReadVariables(transition).length);
			for (int variable : net.getReadVariables(transition)) {
				dos.writeInt(variable);
			}

			dos.writeInt(net.getWriteVariables(transition).length);
			for (int variable : net.getWriteVariables(transition)) {
				dos.writeInt(variable);
			}

			WeightFunction weightFunction = net.getWeightFunction(transition);
			if (weightFunction instanceof SerializableWeightFunction) {
				dos.writeInt(getWeightFunctionIndex((SerializableWeightFunction) weightFunction));
				((SerializableWeightFunction) weightFunction).serialize(dos);
			} else {
				throw new RuntimeException(
						"Trying to serialize non-serializable WeightFunction! " + weightFunction.getClass());
			}

			dos.writeInt(EOT);
		}

		dos.writeInt(net.getNumberOfVariables());
		for (int i = 0; i < net.getNumberOfVariables(); i++) {
			dos.writeUTF(net.getVariableLabel(i));
			dos.writeUTF(net.getVariableType(i).name());
		}

		dos.writeInt(EOF);
		
		dos.flush();
	}

	@Override
	public StochasticLabelledDataPetriNetWeightsDataDependent deserialize(InputStream is)
			throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		DataInputStream dis = new DataInputStream(is);

		StochasticLabelledPetriNetSimpleWeightsEditable slpn = new StochasticLabelledPetriNetSimpleWeightsImpl();

		int readVersion = dis.readInt();
		if (readVersion != VERSION) {
			throw new RuntimeException("Unknown file version " + readVersion);
		}

		int numPlaces = dis.readInt();

		for (int place = 0; place < numPlaces; place++) {
			int idx = slpn.addPlace();
			int tokens = dis.readInt();
			if (tokens > 0) {
				slpn.addPlaceToInitialMarking(idx, tokens);
			}
		}

		int numTransitions = dis.readInt();

		SerializableWeightFunction[] weights = new SerializableWeightFunction[numTransitions];
		List<int[]> reads = new ArrayList<int[]>();
		List<int[]> writes =  new ArrayList<int[]>();

		for (int transition = 0; transition < numTransitions; transition++) {
			boolean silent = dis.readBoolean();
			int idx;
			if (silent) {
				idx = slpn.addTransition(0);
			} else {
				idx = slpn.addTransition(dis.readUTF(), 0);
			}

			int numInputs = dis.readInt();
			for (int i = 0; i < numInputs; i++) {
				int inPlace = dis.readInt();
				slpn.addPlaceTransitionArc(inPlace, idx);
			}

			int numOutputs = dis.readInt();
			for (int i = 0; i < numOutputs; i++) {
				int outPlace = dis.readInt();
				slpn.addTransitionPlaceArc(idx, outPlace);
			}

			int numReads = dis.readInt();
			int[] readVars = new int[numReads];
			for (int i = 0; i < numReads; i++) {
				int readVar = dis.readInt();
				readVars[i] = readVar;
			}
			reads.add(readVars);

			int numWrites = dis.readInt();
			int[] writeVars = new int[numWrites];
			for (int i = 0; i < numWrites; i++) {
				int writeVar = dis.readInt();
				writeVars[i] = writeVar;
			}
			writes.add(writeVars);

			int weightFunctionIdx = dis.readInt();
			Class<? extends SerializableWeightFunction> weightClass = getWeightFunctionByIndex(weightFunctionIdx);
			SerializableWeightFunction fun = weightClass.getDeclaredConstructor().newInstance();
			fun.deserialize(dis);
			weights[idx] = fun;

			if (dis.readInt() != EOT) {
				throw new RuntimeException("Missing EOT token");
			}
		}

		int numVariables = dis.readInt();
		List<String> varNames = new ArrayList<>();
		List<VariableType> varTypes = new ArrayList<>();
		for (int i = 0; i < numVariables; i++) {
			varNames.add(dis.readUTF());
			varTypes.add(VariableType.valueOf(dis.readUTF()));
		}

		StochasticLabelledDataPetriNetWeightsDataDependent sldpn = new StochasticLabelledDataPetriNetWeightsDataDependent(
				slpn, varNames, varTypes, reads, writes);

		for (int i = 0; i < weights.length; i++) {
			sldpn.setWeightFunction(i, weights[i]);
		}

		if (dis.readInt() != EOF) {
			throw new RuntimeException("Missing EOF token");
		}

		return sldpn;

	}

	private static int getWeightFunctionIndex(SerializableWeightFunction weightFunction) {
		return WEIGHT_FUNCTIONS.get(weightFunction.getClass());
	}

	private static Class<? extends SerializableWeightFunction> getWeightFunctionByIndex(int index) {
		return WEIGHT_FUNCTIONS.inverse().get(index);
	}

}
