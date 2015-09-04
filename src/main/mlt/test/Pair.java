package mlt.test;

public class Pair {

	private int predicateIndex;
	private boolean predicateValue; // either the true or false branch
	
	public Pair(int predicateIndex, boolean predicateValue) {
		this.predicateIndex = predicateIndex;
		this.predicateValue = predicateValue;
	}

	public int getPredicateIndex() {
		return predicateIndex;
	}

	public void setPredicateIndex(int predicateIndex) {
		this.predicateIndex = predicateIndex;
	}

	public boolean isPredicateValue() {
		return predicateValue;
	}

	public void setPredicateValue(boolean predicateValue) {
		this.predicateValue = predicateValue;
	}

	@Override
	public String toString() {
		return "Pair [ predicateIndex = " + predicateIndex + 
				", predicateValue = " + predicateValue + " ]";
	}
	
}
