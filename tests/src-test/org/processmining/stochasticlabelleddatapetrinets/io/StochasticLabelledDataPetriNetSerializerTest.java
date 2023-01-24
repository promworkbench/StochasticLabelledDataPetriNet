package org.processmining.stochasticlabelleddatapetrinets.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.log.utils.XUtils;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNetWeightsDataDependent;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter;
import org.processmining.stochasticlabelleddatapetrinet.weights.writeops.AllWriteOperationMiner;
import org.processmining.stochasticlabelleddatapetrinets.FakeContext;
import org.processmining.stochasticlabelleddatapetrinets.SepsisTestLog;
import org.processmining.stochasticlabelleddatapetrinets.SimpleTestLog;

public class StochasticLabelledDataPetriNetSerializerTest {
	
	@Test
	public void testWriteSLDPN() throws IOException {
		StochasticLabelledDataPetriNetWeightsDataDependent net = SimpleTestLog.buildDataWeight2VariablesTestModel();
		
		Path tempFile = Files.createTempFile("sldpntests", ".sldpn");
		
		try {
			try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
				net.getDefaultSerializer().serialize(net, fos);	
			}	
		} finally {
			tempFile.toFile().delete();	
		}
		
	}
	
	@Test
	public void testWriteReadSLDPN() throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		StochasticLabelledDataPetriNetWeightsDataDependent net = SimpleTestLog.buildDataWeight2VariablesTestModel();
		
		Path tempFile = Files.createTempFile("sldpntests", ".sldpn");
	
		try {
		
			try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
				net.getDefaultSerializer().serialize(net, fos);
			}

			try (FileInputStream fos = new FileInputStream(tempFile.toFile())) {
				StochasticLabelledDataPetriNet sldpn = net.getDefaultSerializer().deserialize(fos);
				assertNotNull(sldpn);
				assertEqualSLDPN(net, sldpn);
			}

		} finally {
			tempFile.toFile().delete();	
		}
		
	}
	
	@Test
	public void testWriteReadSepsisSLDPN() throws Exception {
		
		StochasticLabelledDataPetriNet net = SepsisTestLog.buildSepsisBaseModel();
		XLog log = SepsisTestLog.loadSepsisLog();
		AllWriteOperationMiner writeOpMiner = new AllWriteOperationMiner(log);
		
		net = writeOpMiner.extendWithWrites(net);
		
		Path tempFile = Files.createTempFile("sldpntests", ".sldpn");
	
		try {
		
			try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
				net.getDefaultSerializer().serialize(net, fos);
			}

			try (FileInputStream fos = new FileInputStream(tempFile.toFile())) {
				StochasticLabelledDataPetriNet sldpn = net.getDefaultSerializer().deserialize(fos);
				assertNotNull(sldpn);
				assertEqualSLDPN(net, sldpn);
			}

		} finally {
			tempFile.toFile().delete();	
		}
		
	}
	
	@Test
	public void testWriteReadRTFMSLDPN() throws Exception {
		
		try (InputStream rtfmModel = getClass().getResourceAsStream("../weights/fitting/Road_Traffic_Fine_Management_Process.xes.gz-IMf.apnml");
				 InputStream rtfmLog = getClass().getResourceAsStream("../weights/fitting/Road_Traffic_Fine_Management_Process.xes.gz1.xes.gz")) {
			
			AcceptingPetriNet pn = AcceptingPetriNetFactory.createAcceptingPetriNet();
			pn.importFromStream(new FakeContext(), rtfmModel);
			
			XLog log = XUtils.loadLog(new GZIPInputStream(rtfmLog));
			StochasticLabelledDataPetriNet net = PetrinetConverter.viewAsSLDPN(pn);
			
			AllWriteOperationMiner writeOpMiner = new AllWriteOperationMiner(log);
			
			net = writeOpMiner.extendWithWrites(net);
			
			Path tempFile = Files.createTempFile("sldpntests", ".sldpn");
			
			try {
			
				try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
					net.getDefaultSerializer().serialize(net, fos);
				}

				try (FileInputStream fos = new FileInputStream(tempFile.toFile())) {
					StochasticLabelledDataPetriNet sldpn = net.getDefaultSerializer().deserialize(fos);
					assertNotNull(sldpn);
					assertEqualSLDPN(net, sldpn);
				}

			} finally {
				tempFile.toFile().delete();	
			}				
			
		}
		
	}	

	private void assertEqualSLDPN(StochasticLabelledDataPetriNet net,
			StochasticLabelledDataPetriNet sldpn) {
		assertEquals(net.getNumberOfTransitions(), sldpn.getNumberOfTransitions());
		assertEquals(net.getNumberOfVariables(), sldpn.getNumberOfVariables());
		for (int i = 0; i < net.getNumberOfTransitions(); i++) {
			assertArrayEquals(net.getInputPlaces(i), sldpn.getInputPlaces(i));
			assertArrayEquals(net.getOutputPlaces(i), sldpn.getOutputPlaces(i));
			assertArrayEquals(net.getReadVariables(i), sldpn.getReadVariables(i));
			assertArrayEquals(net.getWriteVariables(i), sldpn.getWriteVariables(i));
			assertEquals(net.isTransitionSilent(i), sldpn.isTransitionSilent(i));					
		}				
		for (int i = 0; i < net.getNumberOfPlaces(); i++) {
			assertEquals(net.isInInitialMarking(i), sldpn.isInInitialMarking(i));	
		}				
		for (int i = 0; i < net.getNumberOfVariables(); i++) {
			assertEquals(sldpn.getVariableLabel(i), net.getVariableLabel(i));
			assertEquals(sldpn.getVariableType(i), net.getVariableType(i));
		}
	}	
	

}
