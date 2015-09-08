package mlt.learn;

import java.util.LinkedList;
import java.util.Stack;

import mlt.test.Pair;
import mlt.test.Profiles;

public class ProfileAnalyzer {

	private PredicateNode root;
	private LinkedList<PredicateNode> nodes;
	
	public ProfileAnalyzer() {
		root = new PredicateNode();
		nodes = new LinkedList<PredicateNode>();
		nodes.add(root);
	}

	public void update() {
		int size = Profiles.executedPredicates.size();
		if (size == 0) { return; }
		int testInputIndex = Profiles.testInputs.size() - 1;
		
		PredicateNode current = root;
		Stack<PredicateNode> loopBranchStack = new Stack<PredicateNode>();
		for (int i = 0; i < size; i++) {
			// get the branch predicate information
			Pair p = Profiles.executedPredicates.get(i);
			int index = p.getPredicateIndex();
			boolean value = p.isPredicateValue();

			// set the predicate index if not yet set
			if (current.getPredicate() == -1) {
				current.setPredicate(index);
			} else if (current.getPredicate() != p.getPredicateIndex()) {
				System.err.println("[ml-testing] error in creating the tree structure");
			}
			
			// check if the current branch is a loop branch;
			// if yes, push the loop branch into the stack for later references
			boolean isLoopBranch = false;
			String type = Profiles.predicates.get(index).getType();
			if (type.equals("for") || type.equals("do") || type.equals("while")) {
				isLoopBranch = true;
				if (loopBranchStack.size() == 0 || loopBranchStack.peek().getPredicate() != index) {
					loopBranchStack.push(current);
				}
			}
			
			// pop if exit the loop by taking its false branch
			if (isLoopBranch && !value) {
				loopBranchStack.pop();
			}
			
			// check if the next branch is a loop branch
			PredicateNode next = null;
			if (loopBranchStack.size() > 0 && i + 1 < size) {
				next = loopBranchStack.peek();
				if (Profiles.executedPredicates.get(i + 1).getPredicateIndex() != next.getPredicate()) {
					next = null;
				}
			}
			
			// set either the true branch or the false branch
			PredicateArc branch;
			if (value) {
				if (current.getTrueBranch() == null) {
					if (next == null) {
						next = new PredicateNode();
						nodes.add(next);
					}
					current.setTrueBranch(new PredicateArc(current, next));
				}
				branch = current.getTrueBranch();
				current = current.getTrueBranch().getTarget();
			} else {
				if (current.getFalseBranch() == null) {
					if (next == null) {
						next = new PredicateNode();
						nodes.add(next);
					}
					current.setFalseBranch(new PredicateArc(current, next));
				}
				branch = current.getFalseBranch();
				current = current.getFalseBranch().getTarget();
			}
			
			// avoid associating a test input to a loop branch for multiple times
			if (branch.getTestInputs() == null || branch.getTestInputs().get(branch.getTestInputs().size() - 1) != testInputIndex) {
				branch.addTestInput(testInputIndex);
			}
		}
		Profiles.executedPredicates.clear();
	}
	
	// TODO find a target branch
	public void findTarget() {
		
	}
	
	public void printNodes() {
		for (int i = 0; i < nodes.size(); i++) {
			PredicateNode node = nodes.get(i);
			System.out.println("[ml-testing] " + node);
		}
	}

}
