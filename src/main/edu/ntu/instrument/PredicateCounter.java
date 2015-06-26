package edu.ntu.instrument;

import java.util.Iterator;
import java.util.LinkedList;

public class PredicateCounter {

	public static LinkedList<String> branchPredicates = new LinkedList<String>();
	
	private static int[] branchPredicateCounters;
	
	public static void initialBranchPredicateCounters() {
		branchPredicateCounters = new int[branchPredicates.size()];
		resetBranchPredicateCounters();
	}
	
	public static void incBranchPredicateCounters(int index) {
		branchPredicateCounters[index] += 1;
	}
	
	public static int[] getBranchPredicateCounters() {
		return branchPredicateCounters;
	}
	
	public static void resetBranchPredicateCounters() {
		for (int i = 0; i < branchPredicateCounters.length; i++) {
			branchPredicateCounters[i] = 0;
		}
	}
	
	public static void printBranchPredicates() {
		Iterator<String> iterator = branchPredicates.iterator();
		while(iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}

}
