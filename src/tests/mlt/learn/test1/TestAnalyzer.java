package mlt.learn.test1;

public class TestAnalyzer {

	public void test1(int a, int b, int c) {
		if (a > 0) {
			mlt.test.Profiles.add(0, true);
			b = 1;
		} else {
			mlt.test.Profiles.add(0, false);
			c = 1;
		}
		if (b > 0) {
			mlt.test.Profiles.add(1, true);
		} else {
			mlt.test.Profiles.add(1, false);
		}
		if (c > 0) {
			mlt.test.Profiles.add(2, true);
		} else {
			mlt.test.Profiles.add(2, false);
		}
	}
	
	public static void main(String[] args) {
		new TestAnalyzer().test1(1, 1, 1);
	}

}
