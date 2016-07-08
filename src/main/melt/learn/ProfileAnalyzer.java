package melt.learn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import melt.Config;
import melt.instrument.Predicate;
import melt.test.Pair;
import melt.test.PairArrayList;
import melt.test.Profiles;

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
		ArrayList<Pair> eps = new ArrayList<Pair>();
		getExecutedPredicates(Profiles.executedPredicates, eps);
		Profiles.executedPredicates.clear();
		if (!Profiles.consistant()) {
			System.err.println("[melt] error in compressing executed predicates");
			System.exit(0);
		}
		
		int size = eps.size();
		if (size == 0) { return; }
		int testIndex = Profiles.tests.size() - 1;
		
		PredicateNode current = root;
		Deque<PredicateNode> loopBranchStack = new ArrayDeque<PredicateNode>();
		Deque<Integer> loopRecursionLevelStack = new ArrayDeque<Integer>();
		int recursionLevel = 0;
		for (int i = 0; i < size; i++) {
			// get the branch predicate information
			Pair p = eps.get(i);
			int index = p.getPredicateIndex();
			boolean value = p.isPredicateValue();

			if (index == -2) {
				recursionLevel++;
				continue;
			}
			if (index == -3) {
				recursionLevel--;
				continue;
			}
			
			//System.out.println(current.getPredicate() + " --> " + p);
			
			// set the predicate index if not yet set
			if (current.getPredicate() == -1) {
				current.setPredicate(index);
				addToLeveledNodes(current.getLevel(), nodes.size() - 1);
				addToPredicatedNodes(current.getPredicate(), nodes.size() - 1);
			} else if (current.getPredicate() != index) {
				System.err.println("[melt] error in creating the tree structure");
				System.exit(0);
			}
			
			// attach the dynamic taint results
			if (Config.TAINT.equals("dynamic")) {
				Predicate pd = Profiles.predicates.get(index);
				String key = pd.getClassName() + "@" + pd.getLineNumber();
				HashSet<Integer> newDepInputs = Profiles.taints.get(key);
				current.addToDepInputs(newDepInputs);
			}
			
			// check if the current branch is a loop branch;
			// if yes, push the loop branch into the stack for later references
			boolean isLoopBranch = false;
			Predicate.TYPE type = Profiles.predicates.get(index).getType();
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
			PredicateArc branch;
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
			
			// avoid associating a test input to a loop branch for multiple times
			if (branch.getTests() == null || branch.getTests().get(branch.getTests().size() - 1) != testIndex) {
				if (!(Profiles.predicates.get(branch.getSource().getPredicate()).getType() == Predicate.TYPE.DO && value && isOneIterationDoLoop(eps, i, size))) {
					branch.addTest(testIndex);
				}
			}
		}
		Profiles.taints.clear();
	}
	
	public PredicateNode findUnexploredBranch() {
		if (Config.MODE == Config.Mode.RANDOM) {
			return random();
		} else { // mode == Mode.SYSTEMATIC
			return systematic();
		}
	}
	
	// TODO find an unexplored branch randomly
	private PredicateNode random() {
		return null;
	}
	
	// find an unexplored branch systematically
	private PredicateNode systematic() {
		// locate the level and corresponding unexplored branches for further selection
		int ls = leveledNodes.size();
		for (int i = 0; i < ls; i++) {
			Iterator<Integer> iterator = leveledNodes.get(i).iterator();
			while (iterator.hasNext()) {
				PredicateNode node = nodes.get(iterator.next());
				if (node.getDepInputs() != null) {
					if (node.getAttempts() < Config.MAX_ATTEMPTS) {
						if (!node.isCovered()) {
							return node;
						}
					}
				}
			}
		}
		return null;
	}
		
	public void coverage(PredicateNode target) {
		if (target != null) {
			System.out.println("[melt] target branch " + (target.isCovered() ? "covered" : "not covered"));;
		}
		Iterator<Integer> iterator1 = predicatedNodes.keySet().iterator();
		int pn = 0;
		while (iterator1.hasNext()) {
			Integer predicate = iterator1.next();
			HashSet<Integer> ns = predicatedNodes.get(predicate);
			Iterator<Integer> iterator2 = ns.iterator();
			int cn = 0;
			while (iterator2.hasNext()) {
				if (nodes.get(iterator2.next()).isCovered()) {
					cn++;
				}
			}
			if (cn != 0) {
				pn++;
			}
			System.out.println("[melt] coverage for predicate(" + predicate + ") achieved " + cn + " / " + ns.size());
		}
		System.out.println("[melt] overall coverage achieved " + pn + " / " + predicatedNodes.size() + " / " + Profiles.predicates.size() + "\n");
	}
	
	public void printNodes() {
		for (int i = 0; i < nodes.size(); i++) {
			PredicateNode node = nodes.get(i);
			System.out.println("[melt] " + node);
		}
		System.out.println();
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
	
	// TODO buggy for return statements in loops, and method recursions
	private boolean isOneIterationDoLoop (ArrayList<Pair> eps, int start, int end) {
		Pair p1 = eps.get(start);
		for (int i = start + 1; i < end; i++) {
			Pair p2 = eps.get(i);
			if (p2.getPredicateIndex() == p1.getPredicateIndex() && p2.isPredicateValue()) {
				return false;
			} else if (p2.getPredicateIndex() == p1.getPredicateIndex() && !p2.isPredicateValue()) {
				break;
			}
		}
		return true;
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
	
}
