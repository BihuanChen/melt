package melt.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import melt.Config;
import melt.test.util.Pair;
import melt.test.util.PairArrayList;

public class ProfileAnalyzer {

	private PredicateNode root;
	private ArrayList<PredicateNode> nodes;
	
	private HashMap<Integer, HashSet<Integer>> leveledNodes; // the level of the nodes is the key	
	private HashMap<Integer, HashSet<Integer>> predicatedNodes; // the corresponding predicate is the key
	
	public ProfileAnalyzer() {
		root = new PredicateNode();
		root.setLevel(0);
		nodes = new ArrayList<PredicateNode>();
		nodes.add(root);
		
		leveledNodes = new HashMap<Integer, HashSet<Integer>>();
		predicatedNodes = new HashMap<Integer, HashSet<Integer>>();
	}

	public void update() {
		// check if there is any error during compressing executed predicates
		if (!Profile.consistant()) {
			System.err.println("[melt] error during compressing executed predicates");
			System.exit(0);
		}
		
		// get the list of compressed executed predicates
		ArrayList<Pair> eps = new ArrayList<Pair>();
		this.getExecutedPredicates(Profile.executedPredicates, eps);
		Profile.executedPredicates.clear();

		// iterate over the list to build the branch execution tree 
		int size = eps.size();
		if (size == 0) { return; }
		int testIndex = Profile.tests.size() - 1;
		PredicateNode current = root;
		PredicateArc branch = null; // the branch to the current node
		Deque<PredicateNode> loopBranchStack = new ArrayDeque<PredicateNode>();
		Deque<Integer> loopRecursionLevelStack = new ArrayDeque<Integer>();
		int recursionLevel = 0;

		for (int i = 0; i < size; i++) {
			// get the branch predicate information
			Pair p = eps.get(i);
			int index = p.getPredicateIndex();
			boolean value = p.getPredicateValue();

			// update the recursion level
			if (index == -2) {
				recursionLevel++;
				continue;
			}
			if (index == -3) {
				recursionLevel--;
				continue;
			}
						
			// set the predicate index if not yet set
			if (current.getPredicate() == -1) {
				current.setPredicate(index);
				addToLeveledNodes(current.getLevel(), nodes.size() - 1);
				addToPredicatedNodes(current.getPredicate(), nodes.size() - 1);
			} else if (current.getPredicate() == -2) {
				PredicateNode node = current.getSourceTrueBranch().getTarget();
				if (node.getPredicate() != index) {
					node = current.getSourceFalseBranch().getTarget();
					if (node.getPredicate() != index) {
						System.err.println("[melt] error in hidden nodes of the branch execution tree");
						System.exit(0);
					}
				}
				current = node;
			} else if (current.getPredicate() != index) {
				System.out.println("[melt] adding a hidden node in the branch execution tree");
				// create the hidden node
				PredicateNode hiddenNode = new PredicateNode();
				hiddenNode.setPredicate(-2);
				nodes.add(hiddenNode);
				// de-link to the current node
				boolean result = current.removeTargetTrueBranch(branch);
				if (!result) {
					current.removeTargetFalseBranch(branch);
				}
				// link to the hidden node
				branch.setTarget(hiddenNode);
				if (result) {
					hiddenNode.addTargetTrueBranch(branch);
				} else {
					hiddenNode.addTargetFalseBranch(branch);
				}
				// link hidden node to the original current node
				PredicateArc tb = new PredicateArc(hiddenNode, current);
				hiddenNode.setSourceTrueBranch(tb);
				current.addTargetTrueBranch(tb);
				// create the new current node
				PredicateNode newNode = new PredicateNode();
				newNode.setLevel(current.getLevel());
				newNode.setPredicate(index);
				nodes.add(newNode);
				addToLeveledNodes(newNode.getLevel(), nodes.size() - 1);
				addToPredicatedNodes(newNode.getPredicate(), nodes.size() - 1);
				// link hidden node to the new current node
				PredicateArc fb = new PredicateArc(hiddenNode, newNode);
				hiddenNode.setSourceFalseBranch(fb);
				newNode.addTargetFalseBranch(fb);
				// reset the current node
				current = newNode;
			}
			
			// attach the dynamic taint results
			Predicate pd = Profile.predicates.get(index);
			String key = pd.getClassName() + "@" + pd.getLineNumber();
			HashSet<Integer> newDepInputs = Profile.taints.get(key);
			current.addToDepInputs(newDepInputs);
			
			// check if the current branch is a loop branch;
			// if yes, push the loop branch into the stack for later references
			boolean isLoopBranch = false;
			Predicate.TYPE type = Profile.predicates.get(index).getType();
			if (type == Predicate.TYPE.FOR || type == Predicate.TYPE.FOREACH || type == Predicate.TYPE.DO || type == Predicate.TYPE.WHILE) {
				isLoopBranch = true;
				if (loopBranchStack.size() == 0 || loopBranchStack.peek().getPredicate() != index || loopRecursionLevelStack.peek() != recursionLevel) {
					loopBranchStack.push(current);
					loopRecursionLevelStack.push(recursionLevel);
				}
			}
			
			// pop if exit the loop by taking its false branch
			if (isLoopBranch && !value) {
				loopBranchStack.pop();
				loopRecursionLevelStack.pop();
			}
						
			// deal with return statements in loops
			while (i + 1 < size && eps.get(i + 1).getPredicateIndex() == -1) {
				loopBranchStack.pop();
				loopRecursionLevelStack.pop();
				i++;
			}
			
			// check if the next branch is a loop branch
			PredicateNode next = null;
			if (loopBranchStack.size() > 0 && i + 1 < size) {
				next = loopBranchStack.peek();
				if (eps.get(i + 1).getPredicateIndex() != next.getPredicate()) {
					next = null;
				}
			}
			
			// set either the true branch or the false branch
			
			if (value) {
				if (current.getSourceTrueBranch() == null) {
					if (next == null) {
						next = new PredicateNode();
						int l = current.getLevel() + 1;
						next.setLevel(l);
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
						nodes.add(next);
					}
					PredicateArc arc = new PredicateArc(current, next);
					current.setSourceFalseBranch(arc);
					next.addTargetFalseBranch(arc);
				}
				branch = current.getSourceFalseBranch();
				current = current.getSourceFalseBranch().getTarget();
			}
			
			// avoid associating a test case to a loop branch for multiple times
			if (branch.getTriggerTests() == null || branch.getTriggerTests().get(branch.getTriggerTests().size() - 1) != testIndex) {
				if (!(Profile.predicates.get(branch.getSource().getPredicate()).getType() == Predicate.TYPE.DO && value && isOneIterationDoLoop(eps, i, size))) {
					branch.addToTriggerTests(testIndex);
				}
			}
		}
		Profile.taints.clear();
	}
	
	private void getExecutedPredicates(PairArrayList from, ArrayList<Pair> to) {
		if (from != null) {
			int size = from.size();
			for (int i = 0; i < size; i++) {
				Pair p = from.get(i);
				to.add(p);
				getExecutedPredicates(p.getInnerPairs(), to);
			}
		}
	}
	
	// TODO buggy for return statements in loops, and method recursions
	private boolean isOneIterationDoLoop (ArrayList<Pair> eps, int start, int end) {
		Pair p1 = eps.get(start);
		for (int i = start + 1; i < end; i++) {
			Pair p2 = eps.get(i);
			if (p2.getPredicateIndex() == p1.getPredicateIndex() && p2.getPredicateValue()) {
				return false;
			} else if (p2.getPredicateIndex() == p1.getPredicateIndex() && !p2.getPredicateValue()) {
				break;
			}
		}
		return true;
	}
		
	// find an unexplored branch systematically
	//TODO more strategies to find the unexplored branch?
	public PredicateNode findUnexploredBranch() {
		// locate the level and corresponding unexplored branches for further selection
		int ls = leveledNodes.size();
		for (int i = 0; i < ls; i++) {
			Iterator<Integer> iterator = leveledNodes.get(i).iterator();
			while (iterator.hasNext()) {
				PredicateNode node = nodes.get(iterator.next());
				if (node.getDepInputs() != null) {
					if (node.getAttempts() < Config.MAX_ATTEMPTS) {
						if (!node.isCovered()) {
							node.incAttempts();
							return node;
						}
					}
				}
			}
		}
		return null;
	}
	
	public void computeCoverage(PredicateNode target) {
		// coverage information of the target node
		if (target != null) {
			System.out.println("[melt] target branch " + (target.isCovered() ? "covered" : "not covered"));;
		}
		// coverage information of the whole program
		Iterator<Integer> preIterator = predicatedNodes.keySet().iterator();
		int coveredPre = 0;
		while (preIterator.hasNext()) {
			Integer predicate = preIterator.next();
			HashSet<Integer> ns = predicatedNodes.get(predicate);
			Iterator<Integer> nodeIterator = ns.iterator();
			int coveredNode = 0;
			while (nodeIterator.hasNext()) {
				if (nodes.get(nodeIterator.next()).isCovered()) {
					coveredNode++;
				}
			}
			if (coveredNode != 0) {
				coveredPre++;
			}
			System.out.println("[melt] coverage for predicate(" + predicate + ") achieved " + coveredNode + " / " + ns.size());
		}
		System.out.println("[melt] overall coverage achieved " + coveredPre + " / " + predicatedNodes.size() + " / " + Profile.predicates.size() + "\n");
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

	public PredicateNode getRoot() {
		return root;
	}

	public void setRoot(PredicateNode root) {
		this.root = root;
	}

	public ArrayList<PredicateNode> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<PredicateNode> nodes) {
		this.nodes = nodes;
	}
	
	public void printNodes() {
		for (int i = 0; i < nodes.size(); i++) {
			PredicateNode node = nodes.get(i);
			System.out.println("[melt] " + node);
		}
		System.out.println();
	}
	
}
