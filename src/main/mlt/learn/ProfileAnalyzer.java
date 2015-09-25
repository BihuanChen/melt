package mlt.learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import mlt.test.Pair;
import mlt.test.Profiles;

public class ProfileAnalyzer {

	private PredicateNode root;
	private ArrayList<PredicateNode> nodes;
	private int height;
	
	private HashMap<Integer, HashSet<Integer>> leveledNodes; // the level of the nodes is the key	
	private HashMap<Integer, HashSet<Integer>> predicatedNodes; // the corresponding predicate is the key
	
	public ProfileAnalyzer() {
		root = new PredicateNode();
		root.setLevel(0);
		nodes = new ArrayList<PredicateNode>();
		nodes.add(root);
		height = 0;
		
		leveledNodes = new HashMap<Integer, HashSet<Integer>>();
		predicatedNodes = new HashMap<Integer, HashSet<Integer>>();
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
				addToLeveledNodes(current.getLevel(), nodes.size() - 1);
				addToPredicatedNodes(current.getPredicate(), nodes.size() - 1);
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
				if (current.getSourceTrueBranch() == null) {
					if (next == null) {
						next = new PredicateNode();
						int l = current.getLevel() + 1;
						next.setLevel(l);
						height = height > l ? height : l;
						nodes.add(next);
					}
					PredicateArc arc = new PredicateArc(current, next);
					current.setSourceTrueBranch(arc);
					next.addTargetTrueBranch(arc);
				}
				branch = current.getSourceTrueBranch();
				current = current.getSourceTrueBranch().getTarget();
			} else {
				if (current.getSourceFalseBranch() == null) {
					if (next == null) {
						next = new PredicateNode();
						int l = current.getLevel() + 1;
						next.setLevel(l);
						height = height > l ? height : l;
						nodes.add(next);
					}
					PredicateArc arc = new PredicateArc(current, next);
					current.setSourceFalseBranch(arc);
					next.addTargetFalseBranch(arc);
				}
				branch = current.getSourceFalseBranch();
				current = current.getSourceFalseBranch().getTarget();
			}
			
			// avoid associating a test input to a loop branch for multiple times
			if (branch.getTestInputs() == null || branch.getTestInputs().get(branch.getTestInputs().size() - 1) != testInputIndex) {
				branch.addTestInput(testInputIndex);
			}
		}
		Profiles.executedPredicates.clear();
		System.out.println(leveledNodes);
		System.out.println(predicatedNodes);
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

	private void addToLeveledNodes(int level, int index) {
		if (leveledNodes.get(level) == null) {
			leveledNodes.put(level, new HashSet<Integer>());
		}
		leveledNodes.get(level).add(index);
	}
	
	private void addToPredicatedNodes(int predicate, int index) {
		if (predicatedNodes.get(predicate) == null) {
			predicatedNodes.put(predicate, new HashSet<Integer>());
		}
		predicatedNodes.get(predicate).add(index);
	}
	
}
