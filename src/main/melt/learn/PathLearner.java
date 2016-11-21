package melt.learn;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.vm.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import melt.Config;
import melt.core.Predicate;
import melt.core.PredicateArc;
import melt.core.PredicateNode;
import melt.core.Profile;
import melt.test.generation.search.TestVar;
import melt.test.util.TestCase;

public class PathLearner {
	
	// the possible traces from the root node to the target node
	private PredicateNode root;
	private PredicateNode target;	
	private LinkedHashSet<ArrayList<Step>> traces;

	public PathLearner(PredicateNode root, PredicateNode target) {
		this.root = root;
		this.target = target;
		this.findSourceNodes(target);
	}

	// find the possible traces from the root node to the target node
	private void findSourceNodes(PredicateNode node) {
		if (node.getLevel() > 0) {
			Step ps = findSourceStep(node);
			if (ps == null) {
				System.err.println("[melt] errors in finding prefix traces");
				System.exit(0);
			} else {
				Predicate.TYPE type = Profile.predicates.get(ps.getNode().getPredicate()).getType();
				PredicateArc arc = ps.getNode().getSourceTrueBranch();
				if ((type == Predicate.TYPE.FOR || type == Predicate.TYPE.FOREACH || type == Predicate.TYPE.DO || type == Predicate.TYPE.WHILE) && !ps.getBranch() && arc != null && ps.getNode().getDepInputs() != null) {
					LinkedHashSet<ArrayList<Step>> newTraces = new LinkedHashSet<ArrayList<Step>>();
					Iterator<ArrayList<Step>> iterator = traces.iterator();
					while (iterator.hasNext()) {
						ArrayList<Step> newTrace = new ArrayList<Step>(iterator.next());
						newTrace.remove(newTrace.size() - 1);
						newTrace.add(new Step(ps.getNode(), true));
						newTraces.add(newTrace);
					}
					// loops that do not have ifs inside
					if (arc.getTarget().getPredicate() != arc.getSource().getPredicate()) {
						findTargetNodes(arc.getTarget(), newTraces);
					}
					traces.addAll(newTraces);
				}
				findSourceNodes(ps.getNode());
			}
		}
	}
	
	// find the previous step that reaches a node
	private Step findSourceStep(PredicateNode node) {
		LinkedList<PredicateArc> arcs = node.getTargetTrueBranches();
		if (arcs != null) {
			Iterator<PredicateArc> iterator = arcs.iterator();
			while (iterator.hasNext()) {
				PredicateNode pn = iterator.next().getSource();
				if (pn.getLevel() == node.getLevel() - 1) {
					Step s = new Step(pn, true);
					// branches that do not depend on input are not considered since they are always touched anyway
					if (pn.getDepInputs() != null) {
						addToTraces(s);
					}
					return s;
				}
			}
		}
		arcs = node.getTargetFalseBranches();
		if (arcs!= null) {
			Iterator<PredicateArc> iterator = arcs.iterator();
			while (iterator.hasNext()) {
				PredicateNode pn = iterator.next().getSource();
				if (pn.getLevel() == node.getLevel() - 1) {
					Step s = new Step(pn, false);
					// branches that do not depend on input are not considered since they are always touched anyway
					if (pn.getDepInputs() != null) {
						addToTraces(s);						
					}
					return s;
				}
			}
		}
		return null;
	}
	
	private void findTargetNodes(PredicateNode node, LinkedHashSet<ArrayList<Step>> traces) {
		PredicateArc tArc = node.getSourceTrueBranch();
		PredicateArc fArc = node.getSourceFalseBranch();
		if (tArc != null && fArc != null) {
			int length = traces.iterator().next().size();
			LinkedHashSet<ArrayList<Step>> newTraces = new LinkedHashSet<ArrayList<Step>>();
			Iterator<ArrayList<Step>> iterator = traces.iterator();
			while (iterator.hasNext()) {
				ArrayList<Step> trace = iterator.next();
				ArrayList<Step> newTrace = new ArrayList<Step>(trace);
				newTraces.add(newTrace);
				if (node.getDepInputs() != null) {
					newTrace.add(new Step(node, false));
					trace.add(new Step(node, true));
				}
			}
			if (tArc.getTarget().getLevel() > node.getLevel()) {
				findTargetNodes(tArc.getTarget(), traces);
			}
			if (fArc.getTarget().getLevel() > node.getLevel()) {
				findTargetNodes(fArc.getTarget(), newTraces);
			}
			if (length != traces.iterator().next().size() || length != newTraces.iterator().next().size()) {
				traces.addAll(newTraces);
			}
		} else if (tArc != null && fArc == null) {
			if (node.getDepInputs() != null) {
				Iterator<ArrayList<Step>> iterator = traces.iterator();
				while (iterator.hasNext()) {
					iterator.next().add(new Step(node, true));
				}
			}
			if (tArc.getTarget().getLevel() > node.getLevel()) {
				findTargetNodes(tArc.getTarget(), traces);
			}
		} else if (tArc == null && fArc != null) {
			if (node.getDepInputs() != null) {
				Iterator<ArrayList<Step>> iterator = traces.iterator();
				while (iterator.hasNext()) {
					iterator.next().add(new Step(node, false));
				}
			}
			if (fArc.getTarget().getLevel() > node.getLevel()) {
				findTargetNodes(fArc.getTarget(), traces);
			}
		}
	}

	private void addToTraces(Step step) {
		if (traces == null) {
			traces = new LinkedHashSet<ArrayList<Step>>();
			traces.add(new ArrayList<Step>());
		}
		Iterator<ArrayList<Step>> iterator = traces.iterator();
		while (iterator.hasNext()) {
			iterator.next().add(step);
		}
	}
	
	public PredicateNode getTarget() {
		return target;
	}
	
	public HashSet<ArrayList<Step>> getTraces() {
		return traces;
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
			if (Config.SKIPPED_BRANCH != null && Config.SKIPPED_BRANCH.contains(srcLoc)) {
				continue;
			}
			PredicateNode node = findNode(ns, srcLoc);
			if (node == null) {
				System.out.println("[melt] fail to attach constraints for " + srcLoc);
			} else {
				node.addConstraint(id, constraints.get(inst));
			}
		}
	}
	
	private void collectNodes(PredicateNode node, int testIndex, HashSet<PredicateNode> set) {
		if (node.getPredicate() != -1) {
			PredicateArc arc = node.getSourceTrueBranch();
			if (arc != null && arc.getTriggerTests().contains(testIndex)) {
				set.add(node);
				if (arc.getTarget().getLevel() > node.getLevel()) {
					collectNodes(arc.getTarget(), testIndex, set);
				}
			}
			arc = node.getSourceFalseBranch();
			if (arc != null && arc.getTriggerTests().contains(testIndex)) {
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
		Predicate p = Profile.predicates.get(node.getPredicate());
		String className = p.getClassName();
		String srcLoc = className + "." + p.getMethodName() + "(" + className.substring(className.lastIndexOf(".") + 1) + ".java:" + p.getLineNumber() + ")";
		return srcLoc;
	}
	
	private OneBranchLearner oneLearner = null;
	private HashSet<TwoBranchLearner> twoLearners = new HashSet<TwoBranchLearner>();
	
	public boolean isValidTest(TestCase testCase) throws Exception {
		// a valid test case cannot belong to the executed branch 
		if (oneLearner == null) {
			oneLearner = target.getOneBranchLearner();
			oneLearner.buildInstancesAndClassifier();
		}
		if (oneLearner.classifiyInstance(testCase)[0] == 1.0) {
			return false;
		}
		// a valid test case needs to satisfy one of the traces
		if (traces != null) {
			Iterator<ArrayList<Step>> iterator = traces.iterator();
			while (iterator.hasNext()) {
				ArrayList<Step> trace = iterator.next();
				int size = trace.size();
				boolean valid = true;
				for (int i = 0 ; i < size; i++) {
					TwoBranchLearner twoLearner = trace.get(i).getNode().getTwoBranchesLearner();
					if (twoLearner != null) {
						if (!twoLearners.contains(twoLearner)) {
							twoLearner.buildInstancesAndClassifier();
							twoLearners.add(twoLearner);
						}
						double[] probs = twoLearner.classifiyInstance(testCase);
						if ((probs[0] >= probs[1] && trace.get(i).getBranch()) || (probs[0] < probs[1] && !trace.get(i).getBranch())) {
							valid = false;
							break;
						}
					}
				}
				if (valid) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	public void evaluateTest(TestVar testVar) throws Exception {
		// one branch learner
		if (oneLearner == null) {
			oneLearner = target.getOneBranchLearner();
			oneLearner.buildInstancesAndClassifier();
		}
		double objTarget = oneLearner.classifiyInstance(testVar.getTest())[0];
		// two branch learner
		if (traces != null) {
			Iterator<ArrayList<Step>> iterator = traces.iterator();
			while (iterator.hasNext()) {
				ArrayList<Step> trace = iterator.next();
				double objValue = objTarget;
				HashSet<PredicateNode> violation = null;
				int size = trace.size();
				for (int i = 0 ; i < size; i++) {
					Step step = trace.get(i);
					TwoBranchLearner twoLearner = step.getNode().getTwoBranchesLearner();
					if (twoLearner != null) {
						if (!twoLearners.contains(twoLearner)) {
							twoLearner.buildInstancesAndClassifier();
							twoLearners.add(twoLearner);
						}
						double[] probs = twoLearner.classifiyInstance(testVar.getTest());
						if (step.getBranch()) {
							objValue += probs[0];
						} else {
							objValue += probs[1];
						}
						if ((probs[0] >= probs[1] && step.getBranch()) || (probs[0] < probs[1] && !step.getBranch())) {
							if (violation == null) {
								violation = new HashSet<PredicateNode>();
							}
							violation.add(step.getNode());
						}
					}
				}
				if (objTarget == 1.0) {
					if (violation == null) {
						violation = new HashSet<PredicateNode>(1);
					}
					violation.add(target);
				}
				testVar.addObjValue(objValue);
				testVar.addViolation(violation);
			}
		} else {
			if (objTarget == 1.0) {
				HashSet<PredicateNode> violation = new HashSet<PredicateNode>(1);
				violation.add(target);
				testVar.addViolation(violation);
			} else {
				testVar.addViolation(null);
			}
			testVar.addObjValue(objTarget);
		}
	}

}

class Step {
	
	private PredicateNode node;
	private boolean branch;
	
	public Step(PredicateNode node, boolean branch) {
		this.node = node;
		this.branch = branch;
	}

	public PredicateNode getNode() {
		return node;
	}

	public boolean getBranch() {
		return branch;
	}

	@Override
	public String toString() {
		return "Step [node = " + node.getPredicate() + ", branch = " + branch + "]";
	}
	
}
