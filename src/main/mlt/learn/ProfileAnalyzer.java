package mlt.learn;

import java.util.LinkedList;

import mlt.test.Pair;
import mlt.test.Profiles;

public class ProfileAnalyzer {

	private PredicateNode root;
	
	public ProfileAnalyzer() {
		root = new PredicateNode();
	}

	// TODO loop branches
	public void update() {
		int size = Profiles.executedPredicates.size();
		if (size == 0) { return; }
		int testInputIndex = Profiles.testInputs.size() - 1;
		
		PredicateNode current = root;
		for (int i = 0; i < size; i++) {
			Pair p = Profiles.executedPredicates.get(i);
			current.addTestInput(testInputIndex);
			
			if (current.getPredicate() == -1) {
				current.setPredicate(p.getPredicateIndex());
			} else if (current.getPredicate() != p.getPredicateIndex()) {
				System.err.println("[ml-testing] error in creating the tree structure");
			}
			if (p.isPredicateValue()) {
				if (current.getChild_t() == null) {
					current.setChild_t(new PredicateNode());
					current.getChild_t().setParent(current);
				}
				current = current.getChild_t();
			} else {
				if (current.getChild_f() == null) {
					current.setChild_f(new PredicateNode());
					current.getChild_f().setParent(current);
				}
				current = current.getChild_f();
			}
		}
		current.addTestInput(testInputIndex);
		Profiles.executedPredicates.clear();
	}

	public void print() {
		LinkedList<PredicateNode> list = new LinkedList<PredicateNode>();
		list.add(root);
		while (!list.isEmpty()) {
			PredicateNode node = list.getFirst();
			list.removeFirst();
			System.out.println("[ml-testing] " + node);
			if (node.getChild_t() != null) {
				list.add(node.getChild_t());
			}
			if (node.getChild_f() != null) {
				list.add(node.getChild_f());
			}
		}
	}

}
