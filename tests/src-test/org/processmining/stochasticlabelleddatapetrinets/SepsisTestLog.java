package org.processmining.stochasticlabelleddatapetrinets;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.List;

import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.mining.MiningParameters;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIM;
import org.processmining.stochasticlabelleddatapetrinet.StochasticLabelledDataPetriNet;
import org.processmining.stochasticlabelleddatapetrinet.pnadapater.PetrinetConverter;

public class SepsisTestLog {

	private static final String SEPSIS_4TU = "https://data.4tu.nl/ndownloader/files/24061976";

	public static XLog loadSepsisLog() throws Exception {
		BufferedInputStream in = new BufferedInputStream(new URL(SEPSIS_4TU).openStream());
		XesXmlGZIPParser parser = new XesXmlGZIPParser();
		List<XLog> logs = parser.parse(in);
		assert logs.size() == 1;
		return logs.get(0);
	}
	
	public static AcceptingPetriNet discoverInductiveMiner(XLog log, MiningParameters params) throws UnknownTreeNodeException, ReductionFailedException {		
		IMLog imLog = params.getIMLog(log);
		return new InductiveMinerPlugin().minePetriNet(imLog, params, new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		});
	}
	
	public static StochasticLabelledDataPetriNet buildSepsisBaseModel() throws UnknownTreeNodeException, ReductionFailedException, Exception {	
		AcceptingPetriNet pn = discoverInductiveMiner(loadSepsisLog(), new MiningParametersIM());
		return PetrinetConverter.viewAsSLDPN(pn);
	}

		

}
