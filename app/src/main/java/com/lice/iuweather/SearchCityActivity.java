package com.lice.iuweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.lice.iuweather.db.CityNode;
import com.lice.iuweather.service.Delivery;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import interfaces.heweather.com.interfacesmodule.bean.basic.Basic;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;

public class SearchCityActivity extends AppCompatActivity {
    private EditText searchText;
    private ImageView clearView;
    private Handler handler;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> cityList = new ArrayList<>();
    private List<Basic> mList;
    private Runnable searchRun = new Runnable() {
        @Override
        public void run() {
            HeWeather.getSearch(SearchCityActivity.this, searchText.getText().toString(), "CN", 5, null, new HeWeather.OnResultSearchBeansListener() {
                @Override
                public void onError(Throwable throwable) {
                    throwable.printStackTrace();
                    cityList.clear();
                    String msg = throwable.getMessage();
                    String pattern = "\\.*未知的城市\\.*";
                    Pattern p = Pattern.compile(pattern);
                    Matcher m = p.matcher(msg);
                    if(m.find()){
                        cityList.add("无匹配项");
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onSuccess(Search search) {
                    cityList.clear();
                    List<Basic> basics = search.getBasic();
                    mList = basics;
                    for (Basic basic : basics){
                        String city = basic.getLocation();
                        String parentcity = basic.getParent_city();
                        String adminarea = basic.getAdmin_area();
                        String space = "   ";
                        cityList.add(city + space + parentcity + space + adminarea);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        searchText = findViewById(R.id.search_text);
        clearView = findViewById(R.id.clear_view);
        listView = findViewById(R.id.city_list);
        handler = new Handler();
        adapter = new ArrayAdapter<>(this, R.layout.list_item,cityList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Basic selectBasic = mList.get(position);
                CityNode cityNode = new CityNode(selectBasic);
                List<CityNode> nodes = DataSupport.where("cid == ?", cityNode.getCid())
                        .find(CityNode.class);
                Handler hand = Delivery.getHandler();
                if(!(nodes.size() > 0)){
                    cityNode.save();
                    if(hand != null){
                        Message msg = new Message();
                        msg.what = Delivery.REFRESH_CITY;
                        hand.sendMessage(msg);
                    }
//                    WeatherSubject.getInstance().setNowId(cityNode.getCid());
//                    WeatherSubject.getInstance().updateAllObserver();
                }else{
                    Toast.makeText(SearchCityActivity.this,"该城市已在列表中",Toast.LENGTH_SHORT).show();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SearchCityActivity.this);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("NowWeatherId",cityNode.getCid());
                    editor.apply();
                    if(hand != null){
                        Message msg = new Message();
                        msg.what = Delivery.SHOW_CITY;
                        hand.sendMessage(msg);
                    }
                }

//                Intent intent = new Intent(SearchCityActivity.this,WeatherActivity.class);
//                startActivity(intent);

                finish();
            }
        });
        setEvent();

    }

    private void setEvent(){
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!searchText.getText().toString().equals("")){
                    clearView.setVisibility(View.VISIBLE);
                    if (searchText != null){
                        handler.removeCallbacks(searchRun);
                    }
                    handler.postDelayed(searchRun, 200);
                }else{
                    handler.removeCallbacks(searchRun);
                    clearView.setVisibility(View.GONE);
                    cityList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setText("");
                clearView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {

        Handler handler = Delivery.getHandler();
        Message msg = new Message();
        msg.what = Delivery.BACK_WEATHER;
        handler.sendMessage(msg);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
