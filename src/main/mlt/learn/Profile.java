package mlt.learn;

import java.util.ArrayList;

import mlt.instrument.Predicate;

public class Profile {
	
	public static ArrayList<Predicate> predicates = new ArrayList<Predicate>();
	public static ArrayList<Pair> executedPredicates = new ArrayList<Pair>();
	
	public static void add(int index, boolean value) {
		executedPredicates.add(new Pair(index, value));
	}
	
	public static void clear() {
		executedPredicates.clear();
	}
	
	public static void printPredicates() {
		int size = predicates.size();
		for (int i = 0; i < size; i++) {
			System.out.println("[ml-testing] " + predicates.get(i));
		}
	}
	
	public static void printExecutedPridicates() {
		int size = executedPredicates.size();
		if (size > 0) {
			System.out.print("[ml-testing] predicates");
			for (int i = 0; i < size; i++) {
				System.out.print(" " + executedPredicates.get(i).getPredicateIndex() + " (" + executedPredicates.get(i).isPredicateValue() + ")");
			}
			System.out.println(" executed");
		} else {
			System.out.println("[ml-testing] no predicates executed");
		}
	}

}
