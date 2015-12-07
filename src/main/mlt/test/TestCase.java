package mlt.test;

import gov.nasa.jpf.constraints.api.Valuation;

public class TestCase {
	
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

	public TestCase deepCopy() {
		TestCase newTC = new TestCase();
		newTC.test = new Object[this.test.length];
		for (int i = 0; i < this.test.length; i++) {
			newTC.test[i] = this.test[i];
		}
		return newTC;
	}
	
	public String toString() {
		String str = "[" + test[0];
		for (int i = 1; i < test.length; i++) {
			str += ", " + test[i];
		}
		return str + "]";
	}

}
