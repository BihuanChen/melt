package mlt.test;

import gov.nasa.jpf.constraints.api.Valuation;

public class TestCase {
	
	private Object[] test;
	private Valuation valuation;
	
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

}
