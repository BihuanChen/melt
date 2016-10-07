package melt.learn;

public class TestAnalyzer {

	public void test(byte a, byte b, byte c) {
		test2(a, b, c);
	}
	
	public void test1(byte a, byte b, byte c) {
		if (a > 0) {
			melt.core.Profile.add(0, true, melt.core.Predicate.TYPE.IF);
			b = 3;
		} else {
			melt.core.Profile.add(0, false, melt.core.Predicate.TYPE.IF);
			c = 1;
		}
		for (int i = 0; i < b; i++) {
			melt.core.Profile.add(1, true, melt.core.Predicate.TYPE.FOR);
			if (c > 0) {
				melt.core.Profile.add(2, true, melt.core.Predicate.TYPE.IF);
			} else {
				melt.core.Profile.add(2, false, melt.core.Predicate.TYPE.IF);
			} 
		}
		melt.core.Profile.add(1, false, melt.core.Predicate.TYPE.FOR);
	}
	
	public void test2(byte a, byte b, byte c) {
		for (int i = 0; i < a; i++) {
			melt.core.Profile.add(3, true, melt.core.Predicate.TYPE.FOR);
			if (c > 0) {
				melt.core.Profile.add(4, true, melt.core.Predicate.TYPE.IF);
			} else {
				melt.core.Profile.add(4, false, melt.core.Predicate.TYPE.IF);
			} 
			for (int j = 0; j < b; j++) {
				melt.core.Profile.add(5, true, melt.core.Predicate.TYPE.FOR);
				if (c > 0) {
					melt.core.Profile.add(6, true, melt.core.Predicate.TYPE.IF);
				} else {
					melt.core.Profile.add(6, false, melt.core.Predicate.TYPE.IF);
				} 
			}
			melt.core.Profile.add(5, false, melt.core.Predicate.TYPE.FOR);
		}
		melt.core.Profile.add(3, false, melt.core.Predicate.TYPE.FOR);
	}
	
	public void test3(byte a, byte b, byte c) {
		int i = 0, j = 0;
		do {
			melt.core.Profile.add(7, true, melt.core.Predicate.TYPE.DO);
			i++;
			do {
				melt.core.Profile.add(8, true, melt.core.Predicate.TYPE.DO);
				j++;
			} while (j < a);
			melt.core.Profile.add(8, false, melt.core.Predicate.TYPE.DO);
		} while (i < b);
		melt.core.Profile.add(7, false, melt.core.Predicate.TYPE.DO);
	}
	
	public static void main(String[] args) {
		new TestAnalyzer().test((byte)1, (byte)1, (byte)1);
	}

}
