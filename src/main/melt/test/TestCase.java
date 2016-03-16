package melt.test;

import java.io.Serializable;
import java.util.Arrays;

import gov.nasa.jpf.constraints.api.Valuation;

public class TestCase implements Serializable {

	private static final long serialVersionUID = -8821706477496782862L;

	private Object[] test;
	private Valuation valuation;
	
	public TestCase() {
	}
	
	public TestCase(Object[] test) {
		this.test = test;
	}
	
	public TestCase(Object[] test, Valuation valuation) {
		this.test = test;
		this.valuation = valuation;
	}

	public Object[] getTest() {
		return test;
	}

	public Valuation getValuation() {
		if (valuation == null) {
			valuation = Util.testToValuation(test);
		}
		return valuation;
	}
	
	public void setValuation(Valuation valuation) {
		this.valuation = valuation;
	}

	public TestCase deepCopy() {
		TestCase newTC = new TestCase();
		newTC.test = new Object[this.test.length];
		for (int i = 0; i < this.test.length; i++) {
			newTC.test[i] = this.test[i];
		}
		return newTC;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TestCase) {
			TestCase tc = (TestCase)o;
			int size = tc.getTest().length;
			for (int i = 0; i < size; i++) {
				if (!this.test[i].equals(tc.test[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(test);
	}

	public String toString() {
		String str = "[" + test[0];
		for (int i = 1; i < test.length; i++) {
			str += ", " + test[i];
		}
		return str + "]";
	}
	
	public static void main(String[] args) {
		java.util.HashSet<TestCase> set = new java.util.HashSet<TestCase>();
		TestCase t1 = new TestCase(new Object[]{1, 2});
		TestCase t2 = new TestCase(new Object[]{1, 2});
		set.add(t1);
		set.add(t2);
		System.out.println(set);
	}

}
