package melt.core;

public class LoopReturn {

	public static void test1(int a, int b, int c, int d) {
		a = edu.columbia.cs.psl.phosphor.runtime.MultiTainter.taintedInt(a, 1);
		b = edu.columbia.cs.psl.phosphor.runtime.MultiTainter.taintedInt(b, 2);
		c = edu.columbia.cs.psl.phosphor.runtime.MultiTainter.taintedInt(c, 4);
		d = edu.columbia.cs.psl.phosphor.runtime.MultiTainter.taintedInt(d, 8);
		for (int i = 0; i < a; i++) {
			melt.core.Profile.add(0, true, melt.core.Predicate.TYPE.FOR);
			if (b > 0) {
				melt.core.Profile.add(1, true, melt.core.Predicate.TYPE.IF);
			} else {
				melt.core.Profile.add(1, false, melt.core.Predicate.TYPE.IF);
			}
			test2(c, d);
		}
		melt.core.Profile.add(0, false, melt.core.Predicate.TYPE.FOR);
	}
	
	public static void test2(int c, int d) {
		for (int i = 0; i < c; i++) {
			melt.core.Profile.add(2, true, melt.core.Predicate.TYPE.FOR);
			for (int j = 0; j < d; j++) {
				melt.core.Profile.add(3, true, melt.core.Predicate.TYPE.FOR);
				if (j == 4) {
					melt.core.Profile.add(4, true, melt.core.Predicate.TYPE.IF);
					melt.core.Profile.add(-1, false, null);
					return;
				} else {
					melt.core.Profile
							.add(4, false, melt.core.Predicate.TYPE.IF);
				}
			}
			melt.core.Profile.add(3, false, melt.core.Predicate.TYPE.FOR);
		}
		melt.core.Profile.add(2, false, melt.core.Predicate.TYPE.FOR);
	}
	
	public static void main(String[] args) {
		LoopReturn.test1(10, 5, 10, 5);
	}

}
