package mlt.test.ea;

import java.util.ArrayList;
import java.util.HashSet;

import mlt.learn.PredicateNode;
import mlt.test.TestCase;
import jmetal.core.Variable;

public class TestVar extends Variable {

	private static final long serialVersionUID = -8216717416866562528L;

	private TestCase test;
	
	private ArrayList<Double> objValues = new ArrayList<Double>();
	private int bestIndex;
	private ArrayList<HashSet<PredicateNode>> violations = new ArrayList<HashSet<PredicateNode>>();
	
	public TestVar() {}
	
	public TestVar(TestCase test) {
		this.test = test;
	}

	@Override
	public Variable deepCopy() {
		TestVar newTest = new TestVar();
		newTest.test = this.test.deepCopy();
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

	public int getBestIndex() {
		return bestIndex;
	}

	public void setBestIndex(int bestIndex) {
		this.bestIndex = bestIndex;
	}

	public ArrayList<HashSet<PredicateNode>> getViolations() {
		return violations;
	}

	public void addViolation(HashSet<PredicateNode> violation) {
		violations.add(violation);
	}
	
}
