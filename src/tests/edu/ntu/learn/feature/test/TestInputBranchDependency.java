package edu.ntu.learn.feature.test;

public class TestInputBranchDependency {

	public void test1(int x, int y, int z, boolean p, boolean q) {
		int a = x + 1, b = y + 1, c = x + y + 1;
		
		if (a > 0) { System.out.println("x"); }
		if (b > 0) { System.out.println("y"); }
		if (c > 0) { System.out.println("x, y"); }
		
		if (p) {
			if (a > 0 && b > 0) { System.out.println("x, y"); }
			if (a > 0 || c > 0) { System.out.println("x, y"); }
			if (b > 0 && c > 0) { System.out.println("x, y"); }
		} else {
			x = 1; a = x;
			if (a > 0 && b > 0) { System.out.println("y"); }
			if (a > 0 || c > 0) { System.out.println("y"); }
			if (b > 0 && c > 0) { System.out.println("x, y"); }
		}
	}
	
	public static void main(String[] args) {
		new TestInputBranchDependency().test1(1, 1, 1, true, true);
	}

}
