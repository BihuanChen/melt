package edu.ntu.learn.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.FlowSet;

public class InputBranchDependencyWrapper {
	
	private HashMap<Unit, List<?>> branchToInputs;
	
	public InputBranchDependencyWrapper (DirectedGraph<Unit> graph) {
		InputBranchDependencyAnalysis analysis = new InputBranchDependencyAnalysis(graph);
		branchToInputs = new HashMap<Unit, List<?>>();
		
		Iterator<Unit> ui = graph.iterator();
		while (ui.hasNext()) {
			Unit u = ui.next();
			FlowSet<?> set = (FlowSet<?>) analysis.getFlowAfter(u);
			branchToInputs.put(u, Collections.unmodifiableList(set.toList()));
		}
	}

	public List<?> getInputBranchDependency(Unit u) {
		return branchToInputs.get(u);
	}
	
}
