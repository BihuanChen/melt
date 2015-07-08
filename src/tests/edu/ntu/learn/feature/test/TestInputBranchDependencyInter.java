package edu.ntu.learn.feature.test;

public class TestInputBranchDependencyInter {

	public static void m1(int x, int y, int z) {
		int a = x + 1, b = y + 1, c = z + 1;
		max(a, b);
		max(b, c);
	}
	
	public static int max(int x, int y) {
		if (x > y) {
			return x;
		} else {
			return y;
		}
	}
	
	public static void entryPointMain(int x, int y, int z) {
		int a = x + y, b = y + z;
		m1(a, b, 0);
	}
	
	public static void main(String[] args) {
		entryPointMain(1, 1, 1);
	}

}
