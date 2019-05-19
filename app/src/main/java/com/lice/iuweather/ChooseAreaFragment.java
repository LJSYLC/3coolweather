package com.lice.iuweather;

import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lice.iuweather.db.City;
import com.lice.iuweather.db.County;
import com.lice.iuweather.db.Province;
import com.lice.iuweather.util.HttpUtil;
import com.lice.iuweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    private Handler handler;
    private final static int LEVEL_PROVINCE = 0;
    private final static int LEVEL_CITY = 1;
    private final static int LEVEL_COUNTY = 2;
    private final static int PROCESS_SHOW = 3;
    private final static int PROCESS_DISMISS = 4;
    private int currentLevel = 0;
    private ProgressBar progressBar;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        Context mContext = getContext();
        if(mContext != null){
            adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1,dataList);
        }
        listView.setAdapter(adapter);
        progressBar = view.findViewById(R.id.process_bar);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case PROCESS_SHOW:
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case PROCESS_DISMISS:
                        //materialDialog.dismiss();
                        progressBar.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //DataSupport.deleteAll(City.class);
        //DataSupport.deleteAll(County.class);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();//查询所有的市
                }else if(currentLevel == LEVEL_COUNTY){
                    //selectedCounty = countyList.get(position);
                    String weatherId=countyList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(),WeatherActivity.class);
                    //if(getActivity()instanceof MainActivity){

                        //Intent intent = new Intent(getContext(), WeatherActivity.class);
                        //intent.putExtra("weather_id", selectedCounty.getWeatherId());
                    intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    //}
                    //else
//                        if(getActivity()instanceof WeatherActivity){
//                        WeatherActivity activity = (WeatherActivity) getActivity();
//                        activity.drawerLayout.closeDrawers();
//                        activity.swipeRefresh.setRefreshing(true);
//                        activity.requestWeatherInfo(selectedCounty.getWeatherId());
 //                   }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    //查询全国所有的省，优先从数据库查询，如果没查到再到服务器上查询
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList=DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId = ?", String.valueOf(selectedProvince.getId()))
                .find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId()))
                .find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }


    private void queryFromServer(String address, final String type){
        //showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //closeProgressDialog();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message msg = new Message();
                msg.what = PROCESS_SHOW;
                handler.sendMessage(msg);
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if(result){
                    msg = new Message();
                    msg.what = PROCESS_DISMISS;
                    handler.sendMessage(msg);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }


    public boolean onBackButton(){
        if(currentLevel == LEVEL_COUNTY){
            queryCities();
            return true;
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
            return true;
        }
        return false;
    }
}
