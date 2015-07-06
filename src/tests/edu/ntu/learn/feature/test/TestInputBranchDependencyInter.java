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
	
	public static void main(String[] args) {
		String s1 = args[0], s2 = args[1];
		//String s3 = s1 + s2;
		//String s4 = "dummy";
		//String s5 = s1 + s4;
	}

}
