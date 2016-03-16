package mlt.test;

import java.io.Serializable;

public class PairStack implements Serializable {

	private static final long serialVersionUID = 4285556170782870L;

	private Pair[] array;
    private int top;
    
    public PairStack() {
        array = new Pair[100];
        top = -1;
    }

    public void push(Pair pair) {
        if(top == array.length - 1) {
        	Pair[] tmp = array;
			array = new Pair[array.length + 100];
			System.arraycopy(tmp, 0, array, 0, tmp.length);
        }
        array[++top] = pair;
    }

    public Pair pop() {
        return array[top--];
    }

    public boolean isEmpty(){
        return top == -1;
    }

    public Pair peek(){
        return array[top];
    }
    
}
