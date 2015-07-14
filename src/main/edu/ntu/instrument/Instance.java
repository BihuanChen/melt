package edu.ntu.instrument;

import java.util.Iterator;
import java.util.LinkedList;

public class Instance {

	public static LinkedList<Predicate> predicates = new LinkedList<Predicate>();
	
	public static void incPredicateCounter(int index, boolean branch) {
		predicates.get(index).incCounter(branch);
	}
	
	public static void resetPredicateCounters() {
		Iterator<Predicate> iterator = predicates.iterator();
		while(iterator.hasNext()) {
			iterator.next().resetCounters();
		}
	}
	
	public static void printPredicates() {
		Iterator<Predicate> iterator = predicates.iterator();
		while(iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}

}
