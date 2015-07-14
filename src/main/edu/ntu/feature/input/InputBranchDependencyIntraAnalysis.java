package edu.ntu.feature.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.ParameterRef;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

/**
 * identify the set of input parameters that each variable (and branch) depends on
 * @author huan
 *
 */
public class InputBranchDependencyIntraAnalysis extends ForwardFlowAnalysis<Unit, HashMap<Value, HashSet<Value>>> {
	
	private LinkedHashMap<Unit, HashSet<Value>> branchesToInputsDependency;
	
	public InputBranchDependencyIntraAnalysis(DirectedGraph<Unit> graph) {
		super(graph);
		branchesToInputsDependency = new LinkedHashMap<Unit, HashSet<Value>>();
		doAnalysis();
	}

	@Override
	protected void flowThrough(HashMap<Value, HashSet<Value>> in, Unit d, HashMap<Value, HashSet<Value>> out) {
		out.clear();
		out.putAll(in);
		
		if (d instanceof IfStmt) {
			Iterator<ValueBox> i = ((IfStmt) d).getCondition().getUseBoxes().iterator();
			HashSet<Value> newSet = new HashSet<Value>();
			while (i.hasNext()) {
				Value v = i.next().getValue();
				HashSet<Value> vSet = out.get(v);
				if (vSet != null) {
					newSet.addAll(vSet);
				}
			}
			branchesToInputsDependency.put(d, newSet);
		} else if (d instanceof IdentityStmt) {
			IdentityStmt stmt = (IdentityStmt) d;
			Value lo = stmt.getLeftOp();
			Value ro = stmt.getRightOp();
			if (ro instanceof ParameterRef) {
				HashSet<Value> set = new HashSet<Value>();
				set.add(ro);
				out.put(lo, set);
			}
		} else if (d instanceof DefinitionStmt) {
			DefinitionStmt stmt = (DefinitionStmt) d;
			Value lo = stmt.getLeftOp();
			HashSet<Value> set = new HashSet<Value>();
			Iterator<ValueBox> i = stmt.getUseBoxes().iterator();
			while (i.hasNext()) {
				Value ro = i.next().getValue();
				HashSet<Value> roSet = out.get(ro);
				if (roSet != null) {
					set.addAll(roSet);
				}
			}
			if (set.size() != 0) {
				out.put(lo, set);
			} else {
				out.remove(lo);
			}
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

	public LinkedHashMap<Unit, HashSet<Value>> getBranchesToInputsDependency() {
		return branchesToInputsDependency;
	}
	
}
