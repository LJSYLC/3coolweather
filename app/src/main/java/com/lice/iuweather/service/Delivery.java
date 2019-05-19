package com.lice.iuweather.service;

import android.os.Handler;

public class Delivery {

    public final static int UPDATE_WEATHER = 0;
    public final static int UPDATE_BINGPIC = 1;
    public final static int GLIDE_BINGPIC = 2;
    public final static int LOCATE_START = 3;
    public final static int LOCATE_END = 4;
    public final static int REFRESH_CITY = 5;
    public final static int SHOW_CITY = 6;
    public final static int BACK_WEATHER = 7;


    private static Handler handler;

    public static Handler getHandler() {
        return handler;
    }

    public static void setHandler(Handler handler) {
        Delivery.handler = handler;
    }
}