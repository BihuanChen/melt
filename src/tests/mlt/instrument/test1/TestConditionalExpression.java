package mlt.instrument.test1;

public class TestConditionalExpression {

	// instrumentation for conditional expressions is partially implemented and not integrated into the framework
	
	public void test1 (int x) {
		String r = x > 10 ? "x > 10" : "x <= 10";
		System.out.println(r);
	}
	
	public void test2 (int x) {
		System.out.println(x > 10 ? "x > 10" : "x <= 10");
	}
		
	public void test3 (int x) {
		String r = (x > 0 ? x : -x) > 10 ? "|x| > 10" : "|x| <= 10";
		System.out.println(r);
	}
	
	/*
	 * bugs: do not support conditional expressions nested in a conditional expression's then/else expression
	 */
	/*public void test4 (int x) {
		String r = x > 10 ? (x > 20 ? "x > 20" : "x > 10 && x <= 20") : "x <= 10";
		System.out.println(r);
	}*/
	
}
