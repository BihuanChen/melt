package edu.ntu.learn.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

public class InputBranchDependencyWrapper {
	
	private HashMap<Unit, List> branchToInputs;
	
	public InputBranchDependencyWrapper (DirectedGraph<Unit> graph) {
		InputBranchDependencyAnalysis analysis = new InputBranchDependencyAnalysis(graph);
		branchToInputs = new HashMap<Unit, List>();
		
		Iterator<Unit> ui = graph.iterator();
		while (ui.hasNext()) {
			Unit u = ui.next();
			FlowSet set = (FlowSet) analysis.getFlowAfter(u);
			branchToInputs.put(u, Collections.unmodifiableList(set.toList()));
		}
	}

	public List getInputBranchDependency(Unit u) {
		return null;
	}
	
	public static void main(String[] args) {
		SootClass c = Scene.v().loadClassAndSupport("edu.ntu.learn.feature.test.TestInputBranchDependency");
		c.setApplicationClass();
		
		SootMethod m = c.getMethodByName("test1");
		Body b = m.retrieveActiveBody();

		UnitGraph g = new ExceptionalUnitGraph(b);
		//InputBranchDependencyWrapper w = new InputBranchDependencyWrapper(g);
		
		Iterator<Unit> i = g.iterator();
		while (i.hasNext()) {
			Unit u = i.next();
			System.out.println(u);
			//w.getInputBranchDependency(u);
			System.out.println("def: " + u.getDefBoxes());
			System.out.println("use: " + u.getUseBoxes() + "\n");
		}
	}
	
}
