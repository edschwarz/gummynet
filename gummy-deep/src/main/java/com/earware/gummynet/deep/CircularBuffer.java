package com.earware.gummynet.deep;

import java.util.Arrays;

public class CircularBuffer {
  private Integer data[];
  private int head;
  private int tail;

  public CircularBuffer(Integer number) {
    data = new Integer[number];
    head = 0;
    tail = 0;
  }

  public boolean store(Integer value) {
    if (bufferFull()) {
    		read();
    }
    data[tail++] = value;
    if (tail == data.length) {
        tail = 0;
    }
      return true;
  }

  public Integer read() {
    if (head != tail) {
      int value = data[head++];
      if (head == data.length) {
        head = 0;
      }
      return value;
    } else {
      return null;
    }
  }

  private boolean bufferFull() {
    if (tail + 1 == head) {
      return true;
    }
    if (tail == (data.length - 1) && head == 0) {
      return true;
    }
    return false;
  }
  
  public CircularBuffer copy() {
	  CircularBuffer cb = new CircularBuffer(data.length);
	  cb.data = Arrays.copyOf(data, data.length);
	  cb.head = head;
	  cb.tail = tail;
	  return cb;
  }
  
  public double total() {
	  CircularBuffer cb = this.copy();
	  double total=0;
	  Integer i;
	  while ((i=cb.read())!=null) {
		  total+=i;
	  }
	  return total;
  }
  
  public double average() {
	  CircularBuffer cb = this.copy();
	  double total=0;
	  Integer i;
	  int count=0;
	  while ((i=cb.read())!=null) {
		  total+=i;
		  count++;
	  }
	  return count>0?total/count:0;
  }
}
