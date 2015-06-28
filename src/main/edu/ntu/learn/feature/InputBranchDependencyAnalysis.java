package edu.ntu.learn.feature;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

//reference: https://github.com/pcpratts/soot-rb/tree/master/tutorial/guide/examples

public class InputBranchDependencyAnalysis extends ForwardFlowAnalysis<Unit, Object> {

	public InputBranchDependencyAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		doAnalysis();
	}

	@Override
	protected void flowThrough(Object in, Unit arg1, Object out) {			
	}

	@Override
	protected void copy(Object source, Object dest) {
	}

	@Override
	protected Object entryInitialFlow() {
		return null;
	}

	@Override
	protected void merge(Object in1, Object in2, Object out) {
	}

	@Override
	protected Object newInitialFlow() {
		return null;
	}
	
}
