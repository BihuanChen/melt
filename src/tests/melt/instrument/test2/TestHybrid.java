package melt.instrument.test2;

public class TestHybrid {
	
	public void test1 (int x, int y) {
		if (x > 0) {
			int sum = 0;
			for (int i = 1; i <= x; i++) {
				sum += i;
			}
			System.out.println(sum);
		} else {
			if (x < 0) {
				switch (y) {
				case 1 :
					System.out.println(-x);
					break;
				case 2 :
					System.out.println(x);
					break;
				}
			} else {
				System.out.println(y > 0 ? y : -y);
			}
		}
	}
	
	public void test2 (int x, int y, int z) {
		switch (x) {
		case 1 :
			int sum = 0;
			int i = 0;
			while (i <= y) {
				sum += i++;
			}
			System.out.println(sum);
			break;
		case 2 :
			String str = (y > 0 ? y : -y) > 10 ? "|y| > 10" : "|y| <= 10";
			System.out.println(str);
			break;
		case 3 :
			if (z > 0) {
				System.out.println(z);
			} else if (z < 0) {
				System.out.println(-z);
			} 
			break;
		}
	}
	
	public void test3 (int x, int y, int z) {
		for (int i = 0; i < x; i++) {
			if (y < 0) {
				System.out.println(-y);
			} else {
				System.out.println(y);
			}
			switch(z) {
			case 1 :
				System.out.println(-y);
				break;
			case 2 :
				System.out.println(y);
				break;
			}
			System.out.println(y > 0 ? y : -y);
		}
	}
	
}
