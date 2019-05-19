package com.lice.iuweather.util;

import android.text.TextUtils;

import com.lice.iuweather.db.City;
import com.lice.iuweather.db.County;
import com.lice.iuweather.db.Province;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utility {

    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(jsonObject.getString("name"));
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCityName(jsonObject.getString("name"));
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCityId(cityId);
                    county.setWeatherId(jsonObject.getString("weather_id"));//原为weather_id
                    county.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }


    public static String handleWeatherResponse_new(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            jsonObject = new JSONObject(weatherContent);
            jsonArray = jsonObject.getJSONArray("basic");
            weatherContent = jsonArray.getJSONObject(0).toString();
            jsonObject = new JSONObject(weatherContent);
            String retId = jsonObject.getString("cid");
            //weatherContent = jsonArray.getJSONObject(0).toString();
            return retId;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
