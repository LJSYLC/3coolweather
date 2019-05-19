package com.lice.iuweather.service;



import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//自动调用
public class AutoUpdateService extends Service {
    public AutoUpdateService() {

    }

    private Handler mhandler;

    private int hourCnt = 0;

    @Override
    public void onCreate() {
        mhandler = Delivery.getHandler();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                hourCnt ++;
                updateBingPic();
                Message msg = new Message();
                msg.what = Delivery.UPDATE_WEATHER;
                mhandler.sendMessage(msg);
                if(hourCnt == 24){

                    hourCnt = 0;

                }
            }
        }).start();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int tensec = 60 * 10 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + tensec;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        if(Build.VERSION.SDK_INT > 19){
            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void updateBingPic(){
        Message msg = new Message();
        msg.what = Delivery.UPDATE_BINGPIC;
        mhandler.sendMessage(msg);
    }
}

