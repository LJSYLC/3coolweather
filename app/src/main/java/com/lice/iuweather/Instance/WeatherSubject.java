package com.lice.iuweather.Instance;

import java.util.ArrayList;
import java.util.List;

public class WeatherSubject {

    private String nowId;

    private static List<Observer> observers = new ArrayList<>();

    private static WeatherSubject weatherSubject;

    public void setNowId(String nowId) {
        this.nowId = nowId;
    }

    public String getNowId() {
        return nowId;
    }

    private WeatherSubject(){}

    public static WeatherSubject getInstance(){
        if(weatherSubject == null){
            synchronized (WeatherSubject.class){
                if(weatherSubject == null){
                    weatherSubject= new WeatherSubject();
                }
            }
        }
        return weatherSubject;
    }

    public void add(Observer observer){
        observers.add(observer);
    }

    public void remove(Observer observer){
        if(observers.contains(observer)){
            observers.remove(observer);
        }
    }

    public void removeAll(){
        observers.clear();
        int i = 1;
        int j = 1;
    }

    public void updateAllObserver(){
        for (Observer observer : observers) {
            observer.update(nowId);
        }
    }

}
