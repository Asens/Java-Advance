package com.geekutil.designpattern.iterator;

/**
 * @author Asens
 * create 2019-10-13 12:09
 **/

public class NormalList implements Aggregate{
    private String[] arr = new String[10];
    private int size = 0;

    public void add(String data){
        if(size>9){
            throw new IllegalStateException("max length 10");
        }
        arr[size++] = data;
    }

    @Override
    public Iterator createIterator() {
        return new ConcreteIterator();
    }

    private class ConcreteIterator implements Iterator{
        private int current = 0;

        @Override
        public boolean hasNext() {
            return current<size;
        }

        @Override
        public Object next() {
            return arr[current++];
        }
    }
}
