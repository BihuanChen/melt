package edu.ntu.feature.input.test1;

public class TestInputBranchDependencyInter1 {

	public static void m1(int x, int y, int z, boolean p) {	
		int a = x + 1, b = y + 1, c = x + y + 1;
		if (p) {
			edu.ntu.learn.Profile.incPredicateCounter(0, true);
			if (a > 0 && b > 0) {
				edu.ntu.learn.Profile.incPredicateCounter(1, true);
				System.out.println("x, y");
			} else {
				edu.ntu.learn.Profile.incPredicateCounter(1, false);
			}
			if (a > 0 || c > 0) {
				edu.ntu.learn.Profile.incPredicateCounter(2, true);
				System.out.println("x, y");
			} else {
				edu.ntu.learn.Profile.incPredicateCounter(2, false);
			}
			if (b > 0 && c > 0) {
				edu.ntu.learn.Profile.incPredicateCounter(3, true);
				System.out.println("x, y");
			} else {
				edu.ntu.learn.Profile.incPredicateCounter(3, false);
			}
		} else {
			edu.ntu.learn.Profile.incPredicateCounter(0, false);
			x = 1; a = x + 1;
			if (a > 0 && b > 0) {
				edu.ntu.learn.Profile.incPredicateCounter(4, true);
				System.out.println("y");
			} else {
				edu.ntu.learn.Profile.incPredicateCounter(4, false);
			}
			if (a > 0 || c > 0) {
				edu.ntu.learn.Profile.incPredicateCounter(5, true);
				System.out.println("x, y");
			} else {
				edu.ntu.learn.Profile.incPredicateCounter(5, false);
			}
			if (b > 0 && c > 0) {
				edu.ntu.learn.Profile.incPredicateCounter(6, true);
				System.out.println("x, y");
			} else {
				edu.ntu.learn.Profile.incPredicateCounter(6, false);
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
