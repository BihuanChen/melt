package melt.test;

import java.io.Serializable;

import melt.core.Predicate;
import melt.core.Predicate.TYPE;

public class Pair implements Serializable {

	private static final long serialVersionUID = 6942611549827450107L;
	
	private int predicateIndex;
	private boolean predicateValue; // either the true or false branch
	
	private PairArrayList innerPairs; // predicates enclosed in a loop
	
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
	
	public PairArrayList getInnerPairs() {
		return innerPairs;
	}
	
	public void addToInnerPairs(Pair p) {
		if (innerPairs == null) {
			innerPairs = new PairArrayList();
		}
		innerPairs.add(p);
	}

	public boolean equals(Pair p) {
		if (this.predicateIndex == p.predicateIndex && this.predicateValue == p.predicateValue) {
			if (this.innerPairs == null && p.innerPairs == null) {
				return true;
			} else if (this.innerPairs != null && p.innerPairs != null) {
				int l1 = this.innerPairs.size();
				int l2 = p.innerPairs.size();
				if (l1 <= l2) {
					for (int i = 0, j = 0; i < l1 && j < l2; ) {
						Pair p1 = this.innerPairs.get(i);
						Pair p2 = p.innerPairs.get(j);
						if (p1.predicateIndex == -2 || p1.predicateIndex == -3 || (p1.predicateIndex != -1 && Profiles.predicates.get(p1.predicateIndex).getType() == TYPE.IF)) {
							if (!p1.equals(p2)) {
								return false;
							} else {
								i++;
								j++;
							}
						} else {
							for (int k = j; k < l2; k++) {
								Pair pp = p.innerPairs.get(k);
								if (pp.predicateIndex == p2.predicateIndex || pp.predicateIndex == -1) {
									if (p1.equals(pp)) {
										// judge if end of loop
										if (!p1.predicateValue || p1.predicateIndex == -1) {
											j = k + 1;
										}
										i++;
										break;
									}
								} else {
									return false;
								}
							}
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Pair [ predicateIndex = " + predicateIndex + 
				", predicateValue = " + predicateValue + " ]";
	}
	
	public static void main(String[] args) {
		Profiles.predicates.add(new Predicate("", "", "", 0, "", TYPE.IF));
		Profiles.predicates.add(new Predicate("", "", "", 1, "", TYPE.FOR));
		Profiles.predicates.add(new Predicate("", "", "", 2, "", TYPE.IF));
		Profiles.predicates.add(new Predicate("", "", "", 3, "", TYPE.IF));
		Profiles.predicates.add(new Predicate("", "", "", 4, "", TYPE.FOR));
		Profiles.predicates.add(new Predicate("", "", "", 5, "", TYPE.IF));
		Profiles.predicates.add(new Predicate("", "", "", 6, "", TYPE.IF));
		Profiles.predicates.add(new Predicate("", "", "", 7, "", TYPE.IF));
		
		Pair l1 = new Pair(1, true);
		l1.addToInnerPairs(new Pair(2, true));
		l1.addToInnerPairs(new Pair(3, true));

		Pair l21 = new Pair(4, true);
		l21.addToInnerPairs(new Pair(5, true));
		l21.addToInnerPairs(new Pair(6, true));
		l1.addToInnerPairs(l21);
		
		Pair l22 = new Pair(4, true);
		l22.addToInnerPairs(new Pair(5, true));
		l22.addToInnerPairs(new Pair(6, false));
		l1.addToInnerPairs(l22);
		
		l1.addToInnerPairs(new Pair(4, false));
		l1.addToInnerPairs(new Pair(7, true));
		
		Pair ll1 = new Pair(1, true);
		ll1.addToInnerPairs(new Pair(2, true));
		ll1.addToInnerPairs(new Pair(3, true));
		
		Pair ll21 = new Pair(4, true);
		ll21.addToInnerPairs(new Pair(5, true));
		ll21.addToInnerPairs(new Pair(6, true));
		ll1.addToInnerPairs(ll21);
		
		ll1.addToInnerPairs(new Pair(4, false));
		ll1.addToInnerPairs(new Pair(7, true));
		
		System.out.println(ll1.equals(l1));
	}
	
}
