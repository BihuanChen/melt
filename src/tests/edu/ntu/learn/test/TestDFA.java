package edu.ntu.learn.test;

public class TestDFA {

	public void test1(int x, int y, int z) {
		int a = x + 1, b = y + 1, c = x + y;
		
		if (a > 0) { System.out.println("x"); }
		if (b > 0) { System.out.println("y"); }
		if (c > 0) { System.out.println("x, y"); }		
		if (a > 0 && b > 0) { System.out.println("x, y"); }
		if (a > 0 || c > 0) { System.out.println("x, y"); }
		if (b > 0 && c > 0) { System.out.println("x, y"); }
	}
	
	public static void main(String[] args) {
		new TestDFA().test1(1, 1, 1);
	}

}
