package melt.test.generation.search2;

import java.util.HashSet;
import java.util.Iterator;

import melt.core.PredicateNode;
import melt.test.util.TestCase;
import jmetal.core.Variable;

public class TestVar extends Variable {

	private static final long serialVersionUID = -8216717416866562528L;

	private TestCase test;
	
	private double objValue;
	private HashSet<PredicateNode> violations;
	
	public TestVar() {}
	
	public TestVar(TestCase test) {
		this.test = test;
	}

	@Override
	public Variable deepCopy() {
		TestVar newTest = new TestVar();
		newTest.test = this.test.deepCopy();
		newTest.objValue = this.objValue;
		if (this.violations != null) {
			newTest.violations = new HashSet<PredicateNode>(this.violations);
		}
		return newTest;
	}

	public TestCase getTest() {
		return test;
	}

	public double getObjValue() {
		return objValue;
	}

	public void setObjValue(double objValue) {
		this.objValue = objValue;
	}

	public HashSet<PredicateNode> getViolations() {
		return violations;
	}

	public void setViolations(HashSet<PredicateNode> violations) {
		this.violations = violations;
	}
	
	public void clear() {
		objValue = -1;
		violations = null;
	}
	
	public HashSet<Integer> computeDepInputs() {
		HashSet<Integer> depInputs = new HashSet<Integer>();
		
		if (objValue == -1) {
			int size = test.getTest().length;
			for (int i = 0; i < size; i++) {
				depInputs.add(i);
			}
		} else {
			if (violations != null) {
				Iterator<PredicateNode> iterator = violations.iterator();
				while (iterator.hasNext()) {
					depInputs.addAll(iterator.next().getDepInputs());
				}
			}
		}
		
		return depInputs;
	}
	
}
