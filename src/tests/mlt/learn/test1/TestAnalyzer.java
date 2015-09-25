package mlt.learn.test1;

public class TestAnalyzer {

	public void test(int a, int b, int c) {
		test2(a, b, c);
	}
	
	public void test1(int a, int b, int c) {
		if (a > 0) {
			mlt.test.Profiles.add(0, true);
			b = 3;
		} else {
			mlt.test.Profiles.add(0, false);
			c = 1;
		}
		for (int i = 0; i < b; i++) {
			mlt.test.Profiles.add(1, true);
			if (c > 0) {
				mlt.test.Profiles.add(2, true);
			} else {
				mlt.test.Profiles.add(2, false);
			} 
		}
		mlt.test.Profiles.add(1, false);
	}
	
	public void test2(int a, int b, int c) {
		for (int i = 0; i < a; i++) {
			mlt.test.Profiles.add(3, true);
			if (c > 0) {
				mlt.test.Profiles.add(4, true);
			} else {
				mlt.test.Profiles.add(4, false);
			}
			for (int j = 0; j < b; j++) {
				mlt.test.Profiles.add(5, true);
				if (c > 0) {
					mlt.test.Profiles.add(6, true);
				} else {
					mlt.test.Profiles.add(6, false);
				}
			}
			mlt.test.Profiles.add(5, false);
		}
		mlt.test.Profiles.add(3, false);
	}
	
	public static void main(String[] args) {
		new TestAnalyzer().test(1, 1, 1);
	}

}
