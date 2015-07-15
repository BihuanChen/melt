package edu.ntu.instrument;

import java.util.ArrayList;

public class PInstance {
	
	public static ArrayList<Predicate> predicates = new ArrayList<Predicate>();

	public static void incPredicateCounter(int index, boolean branch) {
		predicates.get(index).incCounter(branch);
	}
	
	public static void resetPredicatesCounters() {
		int size = predicates.size();
		for (int i = 0; i < size; i++) {
			predicates.get(i).resetCounters();
		}
		// TODO only reset the dynamically executed branches
	}
	
	public static void printPredicates() {
		int size = predicates.size();
		for (int i = 0; i < size; i++) {
			System.out.println("[ml-testing] " + predicates.get(i));
		}
	}

}
