package org.processmining.stochasticlabelleddatapetrinet.pnadapater;

import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class PetrinetUtils {
	
	public static final ImmutableSet<String> SOURCE_PLACES = ImmutableSet.of("source", "start", "initial", "init",
			"src");
	public static final ImmutableSet<String> SINK_PLACES = ImmutableSet.of("sink", "end", "final", "snk");	

	private PetrinetUtils() {
	}
	
	public static Set<Transition> getTransitionPostSet(PetrinetGraph net, Place place) {
		Set<Transition> postSet = Sets.newHashSetWithExpectedSize(net.getOutEdges(place).size());
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> arc : net.getOutEdges(place)) {
			if (arc.getTarget() instanceof Transition) {
				postSet.add((Transition) arc.getTarget());
			}
		}
		return postSet;
	}
	
	public static Marking guessInitialMarking(final PetrinetGraph currentNet) {
		Place source = findSourceByStructure(currentNet);
		if (source != null) {
			return new Marking(ImmutableList.of(source));
		} else {
			source = findPlaceByNames(currentNet, SOURCE_PLACES);
			if (source != null) {
				return new Marking(ImmutableList.of(source));
			}
		}
		return null;
	}
	

	public static Marking guessFinalMarking(final PetrinetGraph currentNet) {
		Place sink = findSinkByStructure(currentNet);
		if (sink != null) {
			return new Marking(ImmutableList.of(sink));
		} else {
			sink = findPlaceByNames(currentNet, SINK_PLACES);
			if (sink != null) {
				return new Marking(ImmutableList.of(sink));
			}
		}
		return null;
	}

	private static Place findSinkByStructure(PetrinetGraph net) {
		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty()) {
				return p;
			}
		}
		return null;
	}

	private static Place findSourceByStructure(PetrinetGraph net) {
		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty()) {
				return p;
			}
		}
		return null;
	}

	private static Place findPlaceByNames(PetrinetGraph net, Set<String> names) {
		for (Place p : net.getPlaces()) {
			if (names.contains(p.getLabel().toLowerCase())) {
				return p;
			}
		}
		return null;
	}
	
}
