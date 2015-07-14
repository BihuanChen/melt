package edu.ntu.instrument.test1;

public class TestWhileStatement {

	public void test1 (int x) {
		int sum = 0;
		int i = 1;
		while (i <= x) {
			sum += i++;
		}
		System.out.println("sum (1 ... " + x + ") is " + sum);
	}
	
	public void test2 (int x) {
		int sum = 0;
		int i = 1;
		while (i <= x)
			sum += i++;
		System.out.println("sum (1 ... " + x + ") is " + sum);
	}
	
	public void test3 (int x, int y) {
		int sum = 0;
		int i = 1, j = 1;
		while (i <= x) {
			while (j <= y)
				sum += j++;
			i++;
		}
		System.out.println(sum);
	}
	
}
