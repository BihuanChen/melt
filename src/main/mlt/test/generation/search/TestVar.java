package mlt.test.generation.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import mlt.learn.PredicateNode;
import mlt.test.TestCase;
import jmetal.core.Variable;

public class TestVar extends Variable {

	private static final long serialVersionUID = -8216717416866562528L;

	private TestCase test;
	
	private ArrayList<Double> objValues = new ArrayList<Double>();
	private HashSet<Integer> bestObjIndex;
	private ArrayList<HashSet<PredicateNode>> violations = new ArrayList<HashSet<PredicateNode>>();
	
	public TestVar() {}
	
	public TestVar(TestCase test) {
		this.test = test;
	}

	@Override
	public Variable deepCopy() {
		TestVar newTest = new TestVar();
		newTest.test = this.test.deepCopy();
		newTest.objValues.addAll(this.objValues);
		newTest.bestObjIndex = this.bestObjIndex;
		newTest.violations.addAll(this.violations);
		return newTest;
	}

	public TestCase getTest() {
		return test;
	}

	public ArrayList<Double> getObjValues() {
		return objValues;
	}

	public void addObjValue(double objValue) {
		objValues.add(objValue);
	}

	public HashSet<Integer> getBestObjIndex() {
		return bestObjIndex;
	}

	public void addBestIndex(int index) {
		if (bestObjIndex == null) {
			bestObjIndex = new HashSet<Integer>();
		}
		bestObjIndex.add(index);
	}

	public ArrayList<HashSet<PredicateNode>> getViolations() {
		return violations;
	}

	public void addViolation(HashSet<PredicateNode> violation) {
		violations.add(violation);
	}
	
	public void clear() {
		objValues.clear();
		bestObjIndex = null;
		violations.clear();
	}
	
	public HashSet<Integer> computeDepInputs() {
		HashSet<Integer> depInputs = new HashSet<Integer>();
		
		if (bestObjIndex != null) {
			HashSet<PredicateNode> violatedNodes = new HashSet<PredicateNode>();
			Iterator<Integer> iterator1 = bestObjIndex.iterator();
			while (iterator1.hasNext()) {
				HashSet<PredicateNode> part = violations.get(iterator1.next());
				if (part != null) {
					violatedNodes.addAll(part);
				}
			}
			Iterator<PredicateNode> iterator2 = violatedNodes.iterator();
			while (iterator2.hasNext()) {
				depInputs.addAll(iterator2.next().getDepInputs());
			}
		} else {
			// no best index, then all the test inputs need to be crossovered or mutated
			int size = test.getTest().length;
			for (int i = 0; i < size; i++) {
				depInputs.add(i);
			}
		}
		
		return depInputs;
	}
	
	public boolean isSatisfiedForOne() {
		if (bestObjIndex == null) {
			return false;
		}
		Iterator<Integer> iterator = bestObjIndex.iterator();
		while (iterator.hasNext()) {
			if (violations.get(iterator.next()) == null) {
				return true;
			}
		}
		return false;
	}
	
}
