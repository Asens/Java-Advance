package com.geekutil.designpattern.observer;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    private List<Observer> list = new ArrayList<>();

    void registerObserver(Observer observer){
        list.add(observer);
    }

    void notifyObservers(String event){
        list.forEach(observer -> observer.update(event));
    }
}
