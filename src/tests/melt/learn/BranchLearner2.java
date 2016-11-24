package melt.learn;

public class BranchLearner2 {
	
	// test hidden node 1
	public static void test1(int x, int y, int z) {
		if (func1(x) > 0 || z > 0 || func2(y) > 0) {
			melt.core.Profile.add(0, true, melt.core.Predicate.TYPE.IF);
		} else {
			melt.core.Profile.add(0, false, melt.core.Predicate.TYPE.IF);
		}
	}
	
	// test hidden node 2
	public static void test2(int x, int y, int z, int l) {
		x = edu.columbia.cs.psl.phosphor.runtime.MultiTainter.taintedInt(x, 1);
		y = edu.columbia.cs.psl.phosphor.runtime.MultiTainter.taintedInt(y, 2);
		z = edu.columbia.cs.psl.phosphor.runtime.MultiTainter.taintedInt(z, 4);
		l = edu.columbia.cs.psl.phosphor.runtime.MultiTainter.taintedInt(l, 8);
		for (int i = 0; i < l; i++) {
			melt.core.Profile.add(1, true, melt.core.Predicate.TYPE.FOR);
			if (func1(x) > 0 || z > 0 || func2(y) > 0) {
				melt.core.Profile.add(2, true, melt.core.Predicate.TYPE.IF);
			} else {
				melt.core.Profile.add(2, false, melt.core.Predicate.TYPE.IF);
			}
		}
		melt.core.Profile.add(1, false, melt.core.Predicate.TYPE.FOR);
	}
	
	public static int func1(int x) {
		if (x > 5) {
			melt.core.Profile.add(3, true, melt.core.Predicate.TYPE.IF);
			return x - 5;
		} else {
			melt.core.Profile.add(3, false, melt.core.Predicate.TYPE.IF);
			return x;
		}
	}
	
	public static int func2(int y) {
		if (y > 5) {
			melt.core.Profile.add(4, true, melt.core.Predicate.TYPE.IF);
			return y - 5;
		} else {
			melt.core.Profile.add(4, false, melt.core.Predicate.TYPE.IF);
			return y;
		}
	}
		
	public static void main(String[] args) {
		BranchLearner2.test1(10, 5, 10);
	}

}
