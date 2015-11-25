package mlt.learn;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.vm.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import mlt.instrument.Predicate;
import mlt.test.Profiles;

public class PathLearner {

	private PredicateNode root;
	private PredicateNode target;
	
	private ArrayList<PredicateNode> nodes;
	private ArrayList<Boolean> branches;

	public PathLearner(PredicateNode root, PredicateNode target) {
		this.root = root;
		this.target = target;
		this.findSourceNodes(target);
	}
	
	//TODO find all prefix branches for a target branch?
	private void findSourceNodes(PredicateNode node) {
		if (node.getLevel() > 0) {
			PredicateNode pn = findSourceNode(node);
			if (pn == null) {
				System.err.println("[ml-testing] errors in finding source nodes");
			} else {
				findSourceNodes(pn);
			}
		}
	}
	
	private PredicateNode findSourceNode(PredicateNode node) {
		ArrayList<PredicateArc> arcs = node.getTargetTrueBranches();
		if (arcs != null) {
			Iterator<PredicateArc> iterator = arcs.iterator();
			while (iterator.hasNext()) {
				PredicateNode pn = iterator.next().getSource();
				if (pn.getLevel() == node.getLevel() - 1) {
					addToNodes(pn);
					addToBranches(true);
					return pn;
				}
			}
		}
		arcs = node.getTargetFalseBranches();
		if (arcs!= null) {
			Iterator<PredicateArc> iterator = arcs.iterator();
			while (iterator.hasNext()) {
				PredicateNode pn = iterator.next().getSource();
				if (pn.getLevel() == node.getLevel() - 1) {
					addToNodes(pn);
					addToBranches(false);
					return pn;
				}
			}
		}
		return null;
	}

	public void attachConstraints(int testIndex, HashMap<Instruction, Expression<Boolean>> constraints) {
		// collect the nodes that are related to the test
		HashSet<PredicateNode> ns = new HashSet<PredicateNode>();
		collectNodes(root, testIndex, ns);
		// attach the constraints
		Iterator<Instruction> iterator = constraints.keySet().iterator();
		while (iterator.hasNext()) {
			Instruction inst = iterator.next();
			String id = inst.getPosition() + " " + inst.toString();
			String srcLoc = inst.getSourceLocation();
			PredicateNode node = findNode(ns, srcLoc);
			if (node == null) {
				System.err.println("[ml-testing] error in attaching constraints");
			} else {
				node.addConstraint(id, constraints.get(inst));
			}
		}
	}
	
	private void collectNodes(PredicateNode node, int testIndex, HashSet<PredicateNode> set) {
		if (node.getPredicate() != -1) {
			PredicateArc arc = node.getSourceTrueBranch();
			if (arc != null && arc.getTests().contains(testIndex)) {
				set.add(node);
				if (arc.getTarget().getLevel() > node.getLevel()) {
					collectNodes(arc.getTarget(), testIndex, set);
				}
			}
			arc = node.getSourceFalseBranch();
			if (arc != null && arc.getTests().contains(testIndex)) {
				set.add(node);
				if (arc.getTarget().getLevel() > node.getLevel()) {
					collectNodes(arc.getTarget(), testIndex, set);
				}
			}
		}
	}
	
	private PredicateNode findNode(HashSet<PredicateNode> ns, String srcLoc) {
		if (srcLoc.equals(getSrcLoc(target))) {
			return target;
		}
		Iterator<PredicateNode> iterator = ns.iterator();
		while (iterator.hasNext()) {
			PredicateNode n = iterator.next();
			if (srcLoc.equals(getSrcLoc(n))) {
				return n;
			}
		}
		return null;
	}
	
	private String getSrcLoc(PredicateNode node) {
		Predicate p = Profiles.predicates.get(node.getPredicate());
		String className = p.getClassName();
		String srcLoc = className + "." + p.getMethodName() + "(" + className.substring(className.lastIndexOf(".") + 1) + ".java:" + p.getLineNumber() + ")";
		return srcLoc;
	}
	
	public boolean isValidTest(Object[] test) throws Exception {
		if (nodes != null) {
			int size = nodes.size();
			for (int i = 0 ; i < size; i++) {
				TwoBranchesLearner learner = nodes.get(i).getTwoBranchesLearner();
				if (learner != null) {
					learner.buildInstancesAndClassifier();
					double c = learner.classifiyInstance(test);
					if ((c == 0.0 && branches.get(i)) || (c == 1.0 && !branches.get(i))) {
						return false;
					}
				}
			}
		}
		OneBranchLearner learner = target.getOneBranchLearner();
		learner.buildInstancesAndClassifier();
		if ( Double.isNaN(learner.classifiyInstance(test)) ) {
			return true;
		} else {
			return false;
		}
	}
	
	public ArrayList<PredicateNode> getNodes() {
		return nodes;
	}

	public void addToNodes(PredicateNode node) {
		if (nodes == null) {
			nodes = new ArrayList<PredicateNode>();
		}
		nodes.add(node);
	}

	public ArrayList<Boolean> getBranches() {
		return branches;
	}

	public void addToBranches(Boolean branch) {
		if (branches == null) {
			branches = new ArrayList<Boolean>();
		}
		branches.add(branch);
	}

	public PredicateNode getTarget() {
		return target;
	}

}
