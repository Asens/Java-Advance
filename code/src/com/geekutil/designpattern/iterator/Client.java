package com.geekutil.designpattern.iterator;

/**
 * @author Asens
 * create 2019-10-13 12:07
 **/

public class Client {
    public static void main(String[] args) {
        NormalList list = new NormalList();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");

        Iterator iterator = list.createIterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
