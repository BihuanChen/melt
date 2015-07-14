package edu.ntu.instrument.test1;

public class TestSwitchStatement {

	// instrumentation for switch statements is implemented but not integrated into the framework
	
	public void test1 (int x) {
		switch (x) {
			case 5 :
				System.out.println ("x == 5");
				break;
			case 10 :
				System.out.println ("x == 10");
				break;
			default :
				System.out.println ("x != 5 && x != 10");
				break;
		}
	}
	
	public void test2 (int x) {
		switch (x) {
			case 5 : case 6 :
				System.out.println ("x == 5 || x == 6");
				break;
			case 10 : case 11 :
				System.out.println ("x == 10 || x == 11");
				break;
			default :
				System.out.println ("x != 5 && x != 6 && x != 10 && x != 11");
				break;
		}
	}
	
	public void test3 (int x) {
		switch (x) {
			default :
				System.out.println ("x = " + x);
				break;
		}
	}
	
	public void test4 (int x, int y) {
		switch (x) {
			case 5 :
				System.out.println ("x == 5");
				break;
			case 10 :
				System.out.println ("x == 10");
				switch (y) {
					case 3 : 
						System.out.println("y == 3");
						break;
					default :
						System.out.println("y != 3");
						break;
				}
				break;
			default :
				System.out.println ("x != 5 && x != 10");
				break;
		}
	}
	
	public void test5 (int x) {
		switch (x) {
		}
	}
	
	public void test6 (int x, int y) {
		switch (x) {
		case 1 :
			System.out.println(y);
			break;
		case 2 :
			System.out.println(-y);
			break;
		}
	}

}
