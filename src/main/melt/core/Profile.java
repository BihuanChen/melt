package melt.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import melt.core.Predicate.TYPE;
import melt.test.util.FastStack;
import melt.test.util.Pair;
import melt.test.util.PairArrayList;
import melt.test.util.TestCase;

public class Profile {
	
	// static info (collected during instrumentation)
	public static ArrayList<Predicate> predicates = new ArrayList<Predicate>();

	// dynamic info
	public static ArrayList<TestCase> tests = new ArrayList<TestCase>();
	public static HashSet<TestCase> testsSet = new HashSet<TestCase>();
	
	public static PairArrayList executedPredicates = new PairArrayList();
	public static HashMap<String, HashSet<Integer>> taints = new HashMap<String, HashSet<Integer>>();

	// compress the executed predicates for loops and recursions
	private static int recursionLevel = 0;
	private static FastStack<Pair> loopPairStack = new FastStack<Pair>();
	private static FastStack<Integer> loopRecursionLevelStack = new FastStack<Integer>();
			
	// dummy add for concolic execution
	//public static void add(int index, boolean value, TYPE type) {}
	
	public static void add(int index, boolean value, TYPE type) {
		Pair p = new Pair(index, value);
		
		// exit a recursion
		if (index == -3) {
			recursionLevel--;
			if (loopPairStack.isEmpty()) {
				executedPredicates.add(p);
			} else {
				loopPairStack.peek().addToInnerPairs(p);
			}
			return;
		}
		
		// enter a recursion
		if (index == -2) {
			recursionLevel++;
			if (loopPairStack.isEmpty()) {
				executedPredicates.add(p);
			} else {
				loopPairStack.peek().addToInnerPairs(p);
			}
			return;
		}
		
		if (index == -1) {
			int ret_i = loopPairStack.pop().getPredicateIndex();
			int level_i = loopRecursionLevelStack.pop();
			Predicate ret_pre_i = Profile.predicates.get(ret_i);
			boolean mark = false;
			while (!loopPairStack.isEmpty()) {
				loopPairStack.peek().addToInnerPairs(p);
				int ret_j = loopPairStack.peek().getPredicateIndex();
				int level_j = loopRecursionLevelStack.peek();
				Predicate ret_pre_j = Profile.predicates.get(ret_j);
				if (level_i == level_j && ret_pre_i.getClassName().equals(ret_pre_j.getClassName()) &&
						ret_pre_i.getMethodName().equals(ret_pre_j.getMethodName()) &&
						ret_pre_i.getSignature().equals(ret_pre_j.getSignature())) {
					loopPairStack.pop();
					loopRecursionLevelStack.pop();
				} else {
					mark = true;
					break;
				}
			}
			if (!mark) {
				executedPredicates.add(p);
			}
			return; // the last iteration before the return statement could be redundant
		}
		
		if (loopPairStack.isEmpty()) {
			if (type == TYPE.IF) {
				executedPredicates.add(p);
			} else if (type != TYPE.IF) {
				executedPredicates.add(p);
				if (value) { // for executions that directly execute the false loop branch
					loopPairStack.push(p);
					loopRecursionLevelStack.push(recursionLevel);
				}
			}
		} else {
			if (type != TYPE.IF && (index != loopPairStack.peek().getPredicateIndex() || recursionLevel != loopRecursionLevelStack.peek())) {
				loopPairStack.peek().addToInnerPairs(p);
				if (value) {
					loopPairStack.push(p);
					loopRecursionLevelStack.push(recursionLevel);
				}
			} else if (type != TYPE.IF && index == loopPairStack.peek().getPredicateIndex() && recursionLevel == loopRecursionLevelStack.peek()) {
				loopPairStack.pop();
				loopRecursionLevelStack.pop();
				
				PairArrayList ps = loopPairStack.isEmpty() ? executedPredicates : loopPairStack.peek().getInnerPairs();				
				int size = ps.size();
				if ((type == TYPE.FOR || type == TYPE.FOREACH || type == TYPE.WHILE) && size >= 2){
					Pair p1 = ps.get(size - 1);
					Pair p2 = ps.get(size - 2);
					if (p1.getPredicateIndex() == index && p1.getPredicateValue() 
							&& p2.getPredicateIndex() == index && p2.getPredicateValue()) {
						for (int i = size - 2; i >= 0; i--) {
							Pair pt = ps.get(i);
							if (pt.getPredicateIndex() == index && pt.getPredicateValue()) {
								if (p1.equals(pt)) {
									ps.remove(size - 1);
									break;
								}
							} else {
								break;
							}
						}
					}
				} else if (type == TYPE.DO && size >= 3) {
					Pair p1 = ps.get(size - 1);
					Pair p2 = ps.get(size - 2);
					Pair p3 = ps.get(size - 3);
					if (p1.getPredicateIndex() == index && p1.getPredicateValue() 
							&& p2.getPredicateIndex() == index && p2.getPredicateValue() 
							&& p3.getPredicateIndex() == index && p3.getPredicateValue()) {
						for (int i = size - 2; i >= 0; i--) {
							Pair pt = ps.get(i);
							if (pt.getPredicateIndex() == index && pt.getPredicateValue()) {
								if (p1.equals(pt)) {
									ps.remove(size - 1);
									break;
								}
							} else {
								break;
							}
						}
					}
				}
				
				ps.add(p);
				if (value) {
					loopPairStack.push(p);
					loopRecursionLevelStack.push(recursionLevel);
				}
			} else {
				loopPairStack.peek().addToInnerPairs(p);
			}
		}
	}
	
	public static boolean consistant() {
		return loopPairStack.isEmpty() && loopRecursionLevelStack.isEmpty() && recursionLevel == 0;
	}
	
	public static void reset() {
		while (!loopPairStack.isEmpty()) {
			loopPairStack.pop();
		}
		while (!loopRecursionLevelStack.isEmpty()) {
			loopRecursionLevelStack.pop();
		}
		recursionLevel = 0;
	}
	
	public static void printPredicates() {
		int size = predicates.size();
		for (int i = 0; i < size; i++) {
			System.out.println("[melt] " + predicates.get(i));
		}
		System.out.println();
	}
	
	public static void printTests() {
		int size = tests.size();
		System.out.println("[melt] tests");
		for (int i = 0; i < size; i++) {
			System.out.println("[melt] " + tests.get(i).getTest());
		}
		System.out.println();
	}
	
	public static void printExecutedPredicates() {
		int size = executedPredicates.size();
		System.out.println("[melt] predicates");
		for (int i = 0; i < size; i++) {
			System.out.println(executedPredicates.get(i).getPredicateIndex() + " (" + executedPredicates.get(i).getPredicateValue() + ")");
			print(executedPredicates.get(i).getInnerPairs());
		}
	}
	
	private static void print(PairArrayList list) {
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				System.out.println(list.get(i).getPredicateIndex() + " (" + list.get(i).getPredicateValue() + ")");
				print(list.get(i).getInnerPairs());
			}
		}
	}
	
	public static void printTaints() {
		Iterator<String> iterator = taints.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			System.out.println("[melt] " + key + " taints are " + taints.get(key));
		}
	}
	
}
