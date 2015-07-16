package edu.ntu.feature.input.test1;

public class TestInputBranchDependencyInter2 {

	public static int max(int x, int y) {
		if (x > y) {
			edu.ntu.learn.Profile.incPredicateCounter(7, true);
			return x;
		} else {
			edu.ntu.learn.Profile.incPredicateCounter(7, false);
			return y;
		}
	}
	
	public static int min(int x, int y) {
		if (x < y) {
			edu.ntu.learn.Profile.incPredicateCounter(8, true);
			return x;
		} else {
			edu.ntu.learn.Profile.incPredicateCounter(8, false);
			return y;
		}
	}
}
