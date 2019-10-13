package com.geekutil.designpattern.templatemethod;

/**
 * @author Asens
 * create 2019-10-13 11:29
 **/

public class Client {
    public static void main(String[] args) {
        Farmer farmer = new Farmer();
        Worker worker = new Worker();
        farmer.happyDay();
        worker.happyDay();
    }
}
