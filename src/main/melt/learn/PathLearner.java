package melt.learn;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.vm.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import melt.Config;
import melt.instrument.Predicate;
import melt.test.Profiles;
import melt.test.TestCase;
import melt.test.generation.search.TestVar;

public class PathLearner {

	private PredicateNode root;
	private PredicateNode target;
	
	private OneBranchLearner oneLearner = null;
	private HashSet<TwoBranchesLearner> twoLearners = new HashSet<TwoBranchesLearner>();
	
	private LinkedHashSet<ArrayList<Step>> traces;

	public PathLearner(PredicateNode root, PredicateNode target) {
		this.root = root;
		this.target = target;
		this.findSourceNodes(target);
	}
	
	// find all prefix traces for a target branch
	private void findSourceNodes(PredicateNode node) {
		if (node.getLevel() > 0) {
			Step ps = findSourceStep(node);
			if (ps == null) {
				System.err.println("[melt] errors in finding prefix traces");
			} else {
				Predicate.TYPE type = Profiles.predicates.get(ps.getNode().getPredicate()).getType();
				PredicateArc arc = ps.getNode().getSourceTrueBranch();
				if ((type == Predicate.TYPE.FOR || type == Predicate.TYPE.DO || type == Predicate.TYPE.WHILE) && !ps.getBranch() && arc != null) {
					HashSet<ArrayList<Step>> newTraces = new HashSet<ArrayList<Step>>();
					Iterator<ArrayList<Step>> iterator = traces.iterator();
					while (iterator.hasNext()) {
						ArrayList<Step> newTrace = new ArrayList<Step>(iterator.next());
						newTrace.remove(newTrace.size() - 1);
						newTrace.add(new Step(ps.getNode(), true));
						newTraces.add(newTrace);
					}
					findTargetNodes(arc.getTarget(), newTraces);
					traces.addAll(newTraces);
				}
				findSourceNodes(ps.getNode());
			}
		}
	}
	
	private Step findSourceStep(PredicateNode node) {
		ArrayList<PredicateArc> arcs = node.getTargetTrueBranches();
		if (arcs != null) {
			Iterator<PredicateArc> iterator = arcs.iterator();
			while (iterator.hasNext()) {
				PredicateNode pn = iterator.next().getSource();
				if (pn.getLevel() == node.getLevel() - 1) {
					Step s = new Step(pn, true);
					addToTraces(s);
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
					addToTraces(s);
					return s;
				}
			}
		}
		return null;
	}
	
	private void findTargetNodes(PredicateNode node, HashSet<ArrayList<Step>> traces) {
		PredicateArc tArc = node.getSourceTrueBranch();
		PredicateArc fArc = node.getSourceFalseBranch();
		if (tArc != null && fArc != null) {
			HashSet<ArrayList<Step>> newTraces = new HashSet<ArrayList<Step>>();
			Iterator<ArrayList<Step>> iterator = traces.iterator();
			while (iterator.hasNext()) {
				ArrayList<Step> trace = iterator.next();
				ArrayList<Step> newTrace = new ArrayList<Step>(trace);
				newTrace.add(new Step(node, false));
				newTraces.add(newTrace);
				trace.add(new Step(node, true));
			}
			if (tArc.getTarget().getLevel() > node.getLevel()) {
				findTargetNodes(tArc.getTarget(), traces);
			}
			if (fArc.getTarget().getLevel() > node.getLevel()) {
				findTargetNodes(fArc.getTarget(), newTraces);
			}
			traces.addAll(newTraces);
		} else if (tArc != null && fArc == null) {
			Iterator<ArrayList<Step>> iterator = traces.iterator();
			while (iterator.hasNext()) {
				iterator.next().add(new Step(node, true));
			}
			if (tArc.getTarget().getLevel() > node.getLevel()) {
				findTargetNodes(tArc.getTarget(), traces);
			}
		} else if (tArc == null && fArc != null) {
			Iterator<ArrayList<Step>> iterator = traces.iterator();
			while (iterator.hasNext()) {
				iterator.next().add(new Step(node, false));
			}
			if (fArc.getTarget().getLevel() > node.getLevel()) {
				findTargetNodes(fArc.getTarget(), traces);
			}
		}
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
				System.err.println("[melt] error in attaching constraints");
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
					TwoBranchesLearner twoLearner = trace.get(i).getNode().getTwoBranchesLearner();
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
					TwoBranchesLearner twoLearner = step.getNode().getTwoBranchesLearner();
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
	
	public HashSet<ArrayList<Step>> getTraces() {
		return traces;
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
