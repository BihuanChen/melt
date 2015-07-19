package mlt.dependency.test2;

public class TestInputBranchDependencyIntra {

	public void test1(int x, int y, int z) {
		int a = x + 1, b = y + 1, c = x + y + 1;
		
		if (a > 0) { System.out.println("x"); }
		if (b > 0) { System.out.println("y"); }
		if (c > 0) { System.out.println("x, y"); }
		
		if (a > 0 && b > 0) { System.out.println("x, y"); }
		if (a > 0 || c > 0) { System.out.println("x, y"); }
		if (b > 0 && c > 0) { System.out.println("x, y"); }

		x = 1; a = x + 1;
		if (a > 0 && b > 0) { System.out.println("y"); }
		if (a > 0 || c > 0) { System.out.println("x, y"); }
		if (b > 0 && c > 0) { System.out.println("x, y"); }
	}
	
	public void test2(int x, int y, int z, boolean p) {
		int a = x + 1, b = y + 1, c = x + y + 1;
		
		if (p) {
			if (a > 0 && b > 0) { System.out.println("x, y"); }
			if (a > 0 || c > 0) { System.out.println("x, y"); }
			if (b > 0 && c > 0) { System.out.println("x, y"); }
		} else {
			x = 1; a = x + 1;
			if (a > 0 && b > 0) { System.out.println("y"); }
			if (a > 0 || c > 0) { System.out.println("x, y"); }
			if (b > 0 && c > 0) { System.out.println("x, y"); }
		}
		
		a = a * 2;
		System.out.println(a);
	}
	
	public static void main(String[] args) {
		new TestInputBranchDependencyIntra().test1(1, 1, 1);
	}

}
