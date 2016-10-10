package melt.concolic;

public class FailureProgram {
	
	// When doing concolic execution on (-1, 1) for covering line 9, no test input will be generated.
	// This is due to the mapping gap between source code branch and byte code branch.
	public static void test(int x, int y) {
		if (x > 0 || y > 0) {
			if (y > 0) {
			}
		}
	}
	
}
