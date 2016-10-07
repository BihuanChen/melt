package melt.instrument;

public class TestIfStatement {

	public void test1 (int x) {
		if (x > 10) {
			System.out.println("x > 10");
		} else {
			System.out.println("x <= 10");
		}
	}
	
	public void test2 (int x) {
		if (x > 20)
			System.out.println("x > 20");
		else
			System.out.println("x <= 20");
	}
	
	public void test3 (int x) {
		if (x > 30)
			System.out.println("x > 30");
		else if (x > 5)
			System.out.println("x > 5");
		else
			System.out.println("x <= 5");
	}
	
	public void test4 (int x) {
		if (x > 40) {
			System.out.println("x > 40");
			if ( x > 100)
				System.out.println("x > 100");
			else
				System.out.println("x <= 100");
		}
	}
	
	public void test5 (int x) {
		if (x > 90)
			System.out.println("x > 90");
		else if (x > 30) {
			System.out.println("x > 30");
			if (x > 60)
				System.out.println("x > 60");
		}
	}
	
}
