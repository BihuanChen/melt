package melt.instrument;

public class DoStatement {

	public void test1 (int x) {
		int sum = 0;
		int i = 1;
		do {
			sum += i++;
		} while (i <= x);
		System.out.println("sum (1 ... " + x + ") is " + sum);
	}
	
	public void test2 (int x) {
		int sum = 0;
		int i = 1;
		do 
			sum += i++;
		while (i <= x);
		System.out.println("sum (1 ... " + x + ") is " + sum);
	}
	
	public void test3 (int x, int y) {
		int sum = 0;
		int i = 1;
		int j = 1;
		do {
			do 
				sum += j++;
			while (j <= y);
			i++;
		} while (i <= x);
		System.out.println(sum);
	}
	
}
