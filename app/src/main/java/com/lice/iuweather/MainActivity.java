package com.lice.iuweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SearchView;

import org.litepal.LitePal;

import interfaces.heweather.com.interfacesmodule.view.HeConfig;


public class MainActivity extends AppCompatActivity {

    ChooseAreaFragment fragment;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LitePal.getDatabase();
        fragment = (ChooseAreaFragment)getSupportFragmentManager().findFragmentById(R.id.choose_area_frag);
        SharedPreferences  pref= PreferenceManager.getDefaultSharedPreferences(this);//数据存储
        HeConfig.init("HE1808111719301164","19523d5b4c38493cad2e093125a5abe7");//id和key
        HeConfig.switchToFreeServerNode();
        String weatherInfo = pref.getString("weather", null);
        if(weatherInfo != null){
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
//
//        Intent intent = new Intent(this, SearchCityActivity.class);
//        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        boolean flag = false;
        if(fragment != null){
            flag = fragment.onBackButton();
        }
        if(!flag){
            super.onBackPressed();
        }
    }
}
