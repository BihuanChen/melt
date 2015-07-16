package edu.ntu.learn;

import java.util.ArrayList;

import edu.ntu.instrument.Predicate;

public class Profile {
	
	public static ArrayList<Predicate> predicates = new ArrayList<Predicate>();
	public static ArrayList<Integer> executedPredicates = new ArrayList<Integer>();
	
	public static void incPredicateCounter(int index, boolean branch) {
		predicates.get(index).incCounter(branch);
		executedPredicates.add(index);
	}
	
	public static void resetPredicatesCounters() {
		int size = executedPredicates.size();
		for (int i = 0; i < size; i++) {
			int index = executedPredicates.get(i);
			predicates.get(index).resetCounters();
		}
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
		System.out.print("[ml-testing] predicates no.");
		for (int i = 0; i < size; i++) {
			System.out.print(" " + executedPredicates.get(i));
		}
		System.out.println(" executed");
	}

}
