package edu.ntu.learn;

import java.util.Iterator;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class DFA {

	public static void main(String[] args) {
		SootClass c = Scene.v().loadClassAndSupport("edu.ntu.learn.test.TestDFA");
		c.setApplicationClass();
		
		SootMethod m = c.getMethodByName("test1");
		Body b = m.retrieveActiveBody();

		UnitGraph g = new ExceptionalUnitGraph(b);
		//InputBranchDependencyAnalysis a = new InputBranchDependencyAnalysis(g);
		
		Iterator<Unit> i = g.iterator();
		while (i.hasNext()) {
			Unit u = i.next();
			System.out.println(u);
			System.out.println("def: " + u.getDefBoxes());
			System.out.println("use: " + u.getUseBoxes() + "\n");
		}
	}
}
	
class InputBranchDependencyAnalysis extends ForwardFlowAnalysis<Unit, Object> {

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
