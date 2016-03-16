package melt.instrument.test1;

public class TestForStatement {

	public void test1 (int x) {
		int sum = 0;
		for (int i = 1; i <= x; i++) {
			sum += i;
		}
		System.out.println(sum);
	}
	
	public void test2 (int x) {
		int sum = 0;
		for (int i = 1; i <= x; i++)
			sum += i;
		System.out.println(sum);
	}
	
	public void test3 (int x, int y, int z) {
		int sum = 0;
		for (int i = 1; i <= x; i++)
			for (int j = 1; j <= y; j++)
				for (int k = 1; k <= z; k++)
					sum += k;
		System.out.println(sum);
	}
	
	public void test4 (int x, int y, int z) {
		int sum = 0;
		for (int i = 1; i <= x; i++) {
			for (int j = 1; j <= y; j++)
				sum += j;
			for (int k = 1; k <= z; k++)
				sum += k;
		}
		System.out.println(sum);
	}
	
}
