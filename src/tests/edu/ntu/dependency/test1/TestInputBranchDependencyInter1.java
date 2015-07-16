package edu.ntu.dependency.test1;

public class TestInputBranchDependencyInter1 {

	public static void m1(int x, int y, int z, boolean p) {	
		int a = x + 1, b = y + 1, c = x + y + 1;
		if (p) {
			if (a > 0 && b > 0) {
				System.out.println("x, y");
			}
			if (a > 0 || c > 0) {
				System.out.println("x, y");
			}
			if (b > 0 && c > 0) {
				System.out.println("x, y");
			}
		} else {
			x = 1; a = x + 1;
			if (a > 0 && b > 0) {
				System.out.println("y");
			}
			if (a > 0 || c > 0) {
				System.out.println("x, y");
			}
			if (b > 0 && c > 0) {
				System.out.println("x, y");
			}
		}
		
		//TestInputBranchDependencyInter2.max(a, b);
		//TestInputBranchDependencyInter2.max(b, c);
		TestInputBranchDependencyInter2.max(a, c);
		TestInputBranchDependencyInter2.min(b, c);
	}
	
	public static void entryPointMain(int x, int y, int z, boolean p) {
		m1(x, y, z, p);
	}
	
	public static void main(String[] args) {
		entryPointMain(1, 2, 3, true);
	}

}
