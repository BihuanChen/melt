package edu.ntu.learn.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import soot.Unit;
import soot.Value;
import soot.toolkits.graph.DirectedGraph;

public class InputBranchDependencyWrapper {
	
	private HashMap<Unit, HashMap<Value, HashSet<Value>>> branchToInputsBefore;
	private HashMap<Unit, HashMap<Value, HashSet<Value>>> branchToInputsAfter;
	
	public InputBranchDependencyWrapper (DirectedGraph<Unit> graph) {
		InputBranchDependencyAnalysis analysis = new InputBranchDependencyAnalysis(graph);
		branchToInputsBefore = new HashMap<Unit, HashMap<Value, HashSet<Value>>>();
		branchToInputsAfter = new HashMap<Unit, HashMap<Value, HashSet<Value>>>();
		
		Iterator<Unit> ui = graph.iterator();
		while (ui.hasNext()) {
			Unit u = ui.next();
			HashMap<Value, HashSet<Value>> mapBefore = analysis.getFlowBefore(u);
			HashMap<Value, HashSet<Value>> mapAfter = analysis.getFlowAfter(u);
			branchToInputsBefore.put(u, mapBefore);
			branchToInputsAfter.put(u, mapAfter);
		}
	}

	public HashMap<Value, HashSet<Value>> getInputBranchDependencyBefore(Unit u) {
		return branchToInputsBefore.get(u);
	}
	
	public HashMap<Value, HashSet<Value>> getInputBranchDependencyAfter(Unit u) {
		return branchToInputsAfter.get(u);
	}
	
}
