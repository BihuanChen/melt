package melt.test.util;

import java.io.Serializable;

public class FastStack<T> implements Serializable {

	private static final long serialVersionUID = 4285556170782870L;

	private T[] array;
    private int top;
    
    @SuppressWarnings("unchecked")
	public FastStack() {
        array = (T[]) new Object[100];
        top = -1;
    }

    @SuppressWarnings("unchecked")
	public void push(T t) {
        if(top == array.length - 1) {
        	T[] tmp = array;
			array = (T[]) new Object[array.length + 100];
			System.arraycopy(tmp, 0, array, 0, tmp.length);
        }
        array[++top] = t;
    }

    public T pop() {
        return array[top--];
    }

    public boolean isEmpty(){
        return top == -1;
    }

    public T peek(){
        return array[top];
    }
    
}
