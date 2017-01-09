package com.xh.Queue;

import java.util.Arrays;

public class MyArrayQueue<E> {
	
	private static final int DEFAULT_INIT_CAPACITY = 16;
	
	private static final int MAX_CAPACITY = Integer.MAX_VALUE - 8;
	
	private int capacity;
	private int size = 0;
	private E[] queue;
	private int head = 0;
	private int tail = 0;
	
	@SuppressWarnings("unchecked")
	public MyArrayQueue() {
		this(DEFAULT_INIT_CAPACITY);
	}
	
	@SuppressWarnings("unchecked")
	public MyArrayQueue(int initCapacity) {
		capacity = initCapacity;
		queue = newArray(capacity);
	}
	
	public int size() {
		return size;
	}
	
	private void resize(int newCapacity) {
		int size = size();
		if (newCapacity < size) {
			throw new IndexOutOfBoundsException("lose data");
		}
		
		newCapacity++;
		
		if (newCapacity == size) {
			return;
		}
		
		E[] newQueue = newArray(newCapacity);
		for (int i = 0; i < size; i++) {
			newQueue[i] = get(i);
		}
		
		capacity = newCapacity;
		queue = newQueue;
		head = 0;
		tail = size;
	}

	public boolean add(E e) {
		if (size == capacity) {
			resize(capacity << 1);	// assign new value to head and tail
		}
		queue[tail] = e;
		size++;
		tail = (tail + 1) % capacity;
		
		return true;
	}

	// remove head of queue
	public E remove() {
		if (head == tail) {
			throw new IndexOutOfBoundsException("Queue is emtpy");
		}
		E removed = queue[head];
		queue[head] = null;
		head = (head + 1) % capacity;
		size--;
		return removed;
	}
	
	public E get(int i) {
		int size = size();
		if (i < 0 || i >= size) {
			final String msg = "index " + i + ", size " + size; 
			throw new IndexOutOfBoundsException(msg);
		}
		
		int idx = (head + i) % capacity;
		return queue[idx];
	}
	
	@SuppressWarnings("unchecked")
	private E[] newArray(int capacity) {
		return (E[])new Object[capacity];
	}
	
	public static void main(String[] args) {
		MyArrayQueue<String> aq = new MyArrayQueue<String>(2);
		String[] names = {"a", "b", "c", "d", "e", "f", "g", "h", "i","j", "k", "l","m", "n"};
		for (String name : names) {
			aq.add(name);
		}
		
		for (int i = 0; i < aq.size(); i++) {
			System.out.print(aq.get(i));
		}
		System.out.println("\n" + aq.size());
		aq.remove();
		aq.remove();
		System.out.println("=====");
		for (int i = 0; i < aq.size(); i++) {
			System.out.print(aq.get(i));
		}
		System.out.println("\n" + aq.size());
	}
}
