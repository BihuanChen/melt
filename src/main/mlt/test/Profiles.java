package mlt.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import mlt.instrument.Predicate;

public class Profiles {
	
	public static ArrayList<Predicate> predicates = new ArrayList<Predicate>();
	public static ArrayList<TestCase> tests = new ArrayList<TestCase>();
	
	// dynamic info
	public static ArrayList<Pair> executedPredicates = null;
	public static HashMap<String, HashSet<Integer>> taints = new HashMap<String, HashSet<Integer>>();
	
	// for instrumentation
	public static void add(int index, boolean value) {
		executedPredicates.add(new Pair(index, value));
	}
		
	public static void printPredicates() {
		int size = predicates.size();
		for (int i = 0; i < size; i++) {
			System.out.println("[ml-testing] " + predicates.get(i));
		}
		System.out.println();
	}
	
	public static void printTests() {
		int size = tests.size();
		System.out.println("[ml-testing] tests");
		for (int i = 0; i < size; i++) {
			System.out.println("[ml-testing] " + tests.get(i).getTest());
		}
		System.out.println();
	}
	
	public static void printExecutedPredicates() {
		int size = executedPredicates.size();
		System.out.print("[ml-testing] predicates");
		for (int i = 0; i < size; i++) {
			System.out.print(" " + executedPredicates.get(i).getPredicateIndex() + " (" + executedPredicates.get(i).isPredicateValue() + ")");
		}
		System.out.println(" executed");
	}
	
	public static void printTaints() {
		Iterator<String> iterator = taints.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			System.out.println("[ml-testing] " + key + " taints are " + taints.get(key));
		}
	}
	
}