package mlt.learn;

public class PredicateNode {

	private int predicate; // -1 represents a leaf node
	
	private PredicateArc trueBranch;
	private PredicateArc falseBranch;
	
	public PredicateNode() {
		this.predicate = -1;
	}

	public PredicateNode(int predicate) {
		this.predicate = predicate;
	}

	public int getPredicate() {
		return predicate;
	}

	public void setPredicate(int predicate) {
		this.predicate = predicate;
	}

	public PredicateArc getTrueBranch() {
		return trueBranch;
	}

	public void setTrueBranch(PredicateArc trueBranch) {
		this.trueBranch = trueBranch;
	}

	public PredicateArc getFalseBranch() {
		return falseBranch;
	}

	public void setFalseBranch(PredicateArc falseBranch) {
		this.falseBranch = falseBranch;
	}

	@Override
	public String toString() {
		return "PredicateNode [ predicate = " + predicate + ", trueBranch = "
				+ trueBranch + ", falseBranch = " + falseBranch + " ]";
	}

}
