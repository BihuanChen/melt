package melt.learn.test1;

public class TestAnalyzer {

	public void test(byte a, byte b, byte c) {
		test2(a, b, c);
	}
	
	public void test1(byte a, byte b, byte c) {
		if (a > 0) {
			melt.test.Profiles.add11(0, true);
			b = 3;
		} else {
			melt.test.Profiles.add11(0, false);
			c = 1;
		}
		for (int i = 0; i < b; i++) {
			melt.test.Profiles.add2(1, true, melt.instrument.Predicate.TYPE.FOR);
			if (c > 0) {
				melt.test.Profiles.add12(2, true);
			} else {
				melt.test.Profiles.add12(2, false);
			} 
		}
		melt.test.Profiles.add2(1, false, melt.instrument.Predicate.TYPE.FOR);
	}
	
	public void test2(byte a, byte b, byte c) {
		for (int i = 0; i < a; i++) {
			melt.test.Profiles.add2(3, true, melt.instrument.Predicate.TYPE.FOR);
			if (c > 0) {
				melt.test.Profiles.add12(4, true);
			} else {
				melt.test.Profiles.add12(4, false);
			} 
			for (int j = 0; j < b; j++) {
				melt.test.Profiles.add2(5, true, melt.instrument.Predicate.TYPE.FOR);
				if (c > 0) {
					melt.test.Profiles.add12(6, true);
				} else {
					melt.test.Profiles.add12(6, false);
				} 
			}
			melt.test.Profiles.add2(5, false, melt.instrument.Predicate.TYPE.FOR);
		}
		melt.test.Profiles.add2(3, false, melt.instrument.Predicate.TYPE.FOR);
	}
	
	public void test3(byte a, byte b, byte c) {
		int i = 0, j = 0;
		do {
			melt.test.Profiles.add2(7, true, melt.instrument.Predicate.TYPE.DO);
			i++;
			do {
				melt.test.Profiles.add2(8, true, melt.instrument.Predicate.TYPE.DO);
				j++;
			} while (j < a);
			melt.test.Profiles.add2(8, false, melt.instrument.Predicate.TYPE.DO);
		} while (i < b);
		melt.test.Profiles.add2(7, false, melt.instrument.Predicate.TYPE.DO);
	}
	
	public static void main(String[] args) {
		new TestAnalyzer().test((byte)1, (byte)1, (byte)1);
	}

}