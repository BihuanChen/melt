package mlt.learn.test1;

public class TestAnalyzer {

	public void test(byte a, byte b, byte c) {
		test2(a, b, c);
	}
	
	public void test1(byte a, byte b, byte c) {
		if (a > 0) {
			mlt.test.Profiles.add11(0, true);
			b = 3;
		} else {
			mlt.test.Profiles.add11(0, false);
			c = 1;
		}
		for (int i = 0; i < b; i++) {
			mlt.test.Profiles.add2(1, true, mlt.instrument.Predicate.TYPE.FOR);
			if (c > 0) {
				mlt.test.Profiles.add12(2, true);
			} else {
				mlt.test.Profiles.add12(2, false);
			} 
		}
		mlt.test.Profiles.add2(1, false, mlt.instrument.Predicate.TYPE.FOR);
	}
	
	public void test2(byte a, byte b, byte c) {
		for (int i = 0; i < a; i++) {
			mlt.test.Profiles.add2(3, true, mlt.instrument.Predicate.TYPE.FOR);
			if (c > 0) {
				mlt.test.Profiles.add12(4, true);
			} else {
				mlt.test.Profiles.add12(4, false);
			} 
			for (int j = 0; j < b; j++) {
				mlt.test.Profiles.add2(5, true, mlt.instrument.Predicate.TYPE.FOR);
				if (c > 0) {
					mlt.test.Profiles.add12(6, true);
				} else {
					mlt.test.Profiles.add12(6, false);
				} 
			}
			mlt.test.Profiles.add2(5, false, mlt.instrument.Predicate.TYPE.FOR);
		}
		mlt.test.Profiles.add2(3, false, mlt.instrument.Predicate.TYPE.FOR);
	}
	
	public void test3(byte a, byte b, byte c) {
		int i = 0, j = 0;
		do {
			mlt.test.Profiles.add2(7, true, mlt.instrument.Predicate.TYPE.DO);
			i++;
			do {
				mlt.test.Profiles.add2(8, true, mlt.instrument.Predicate.TYPE.DO);
				j++;
			} while (j < a);
			mlt.test.Profiles.add2(8, false, mlt.instrument.Predicate.TYPE.DO);
		} while (i < b);
		mlt.test.Profiles.add2(7, false, mlt.instrument.Predicate.TYPE.DO);
	}
	
	public static void main(String[] args) {
		new TestAnalyzer().test((byte)1, (byte)1, (byte)1);
	}

}
