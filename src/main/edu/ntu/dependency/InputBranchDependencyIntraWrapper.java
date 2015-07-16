package edu.ntu.dependency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import soot.Unit;
import soot.Value;
import soot.toolkits.graph.DirectedGraph;

public class InputBranchDependencyIntraWrapper {
	
	private HashMap<Unit, HashMap<Value, HashSet<Value>>> localsToInputsDependencyBefore;
	private HashMap<Unit, HashMap<Value, HashSet<Value>>> localsToInputsDependencyAfter;
		
	public InputBranchDependencyIntraWrapper (DirectedGraph<Unit> graph) {
		InputBranchDependencyIntraAnalysis analysis = new InputBranchDependencyIntraAnalysis(graph);
		
		localsToInputsDependencyBefore = new HashMap<Unit, HashMap<Value, HashSet<Value>>>();
		localsToInputsDependencyAfter = new HashMap<Unit, HashMap<Value, HashSet<Value>>>();
		
		Iterator<Unit> ui = graph.iterator();
		while (ui.hasNext()) {
			Unit u = ui.next();
			HashMap<Value, HashSet<Value>> mapBefore = analysis.getFlowBefore(u);
			HashMap<Value, HashSet<Value>> mapAfter = analysis.getFlowAfter(u);
			localsToInputsDependencyBefore.put(u, mapBefore);
			localsToInputsDependencyAfter.put(u, mapAfter);
		}
	}

	public HashMap<Value, HashSet<Value>> getLocalsToInputsBefore(Unit u) {
		return localsToInputsDependencyBefore.get(u);
	}
	
	public HashMap<Value, HashSet<Value>> getLocalsToInputsAfter(Unit u) {
		return localsToInputsDependencyAfter.get(u);
	}
	
}
