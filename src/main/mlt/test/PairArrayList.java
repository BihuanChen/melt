package mlt.test;

import java.io.Serializable;

public class PairArrayList implements Serializable {

	private static final long serialVersionUID = -8105677804726657173L;
	
	private Pair[] array;
	private int size = 0;

	public PairArrayList() {
		array = new Pair[200];
	}
	
	public Pair get(int i) {
		return array[i];
	}
	
	public int size() {
		return size;
	}
	
	public void add(Pair p) {
		if(size >= array.length - 1) {
			Pair[] tmp = array;
			array = new Pair[array.length + 100];
			System.arraycopy(tmp, 0, array, 0, tmp.length);
		}
		array[size] = p;
		size++;
	}
	
	public void remove(int index) {
		size--;
		array[size] = null;
	}
	
	public void clear() {
		for (int i = 0; i < size; i++) {
			array[i] = null;
		}
		size = 0;
	}
}
