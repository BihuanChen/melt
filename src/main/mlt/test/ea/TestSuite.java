package mlt.test.ea;

import mlt.test.TestCase;
import jmetal.core.Variable;

public class TestSuite extends Variable {

	private static final long serialVersionUID = -8216717416866562528L;
	
	private TestCase[] array;	
	private int size;

	public TestSuite() {
	}
	
	public TestSuite(int size) {
		this.size = size;
		this.array = new TestCase[size];
	}

	@Override
	public Variable deepCopy() {
		TestSuite newTS = new TestSuite();
		newTS.array = new TestCase[this.size];
		for (int i = 0; i < this.size; i++) {
			newTS.array[i] = this.array[i].deepCopy();
		}
		newTS.size = this.size;
		return newTS;
	}

	public TestCase[] getArray() {
		return array;
	}

	public int getSize() {
		return size;
	}
	
}
