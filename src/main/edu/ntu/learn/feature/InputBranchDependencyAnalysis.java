package edu.ntu.learn.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

//reference: https://github.com/pcpratts/soot-rb/tree/master/tutorial/guide/examples

/**
 * identify the set of input parameters that each variable (and branch) depends on
 * @author huan
 *
 */
public class InputBranchDependencyAnalysis extends ForwardFlowAnalysis<Unit, HashMap<Value, HashSet<Value>>> {

	public InputBranchDependencyAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		doAnalysis();
	}

	@Override
	protected void flowThrough(HashMap<Value, HashSet<Value>> in, Unit d, HashMap<Value, HashSet<Value>> out) {
		out.clear();
		
		if (d instanceof IdentityStmt) {
			Value v = d.getUseBoxes().get(0).getValue();
			if (v instanceof ParameterRef) {
				out.put(v, new HashSet<Value>());
			}
		} else if (d instanceof DefinitionStmt) {
			
		}
	}

	@Override
	protected void merge(HashMap<Value, HashSet<Value>> in1, HashMap<Value, HashSet<Value>> in2, HashMap<Value, HashSet<Value>> out) {
		out.clear();
		out.putAll(in1);
		Iterator<Value> i = in2.keySet().iterator();
		while (i.hasNext()) {
			Value v = i.next();
			if (out.get(v) != null) {
				out.get(v).addAll(in2.get(v));
			} else {
				out.put(v, in2.get(v));
			}
		}
	}

	@Override
	protected void copy(HashMap<Value, HashSet<Value>> source, HashMap<Value, HashSet<Value>> dest) {		
		dest.clear();
		dest.putAll(source);
	}
	
	@Override
	protected HashMap<Value, HashSet<Value>> newInitialFlow() {
		return new HashMap<Value, HashSet<Value>>();
	}
	
}
