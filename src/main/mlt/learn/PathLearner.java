package mlt.learn;

import java.util.ArrayList;
import java.util.Iterator;

public class PathLearner {

	private PredicateNode target;
	private ArrayList<PredicateNode> nodes;
	private ArrayList<Boolean> branches;

	public PathLearner(PredicateNode target) {
		this.target = target;
		this.findSourceNodes(target);
	}
	
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
