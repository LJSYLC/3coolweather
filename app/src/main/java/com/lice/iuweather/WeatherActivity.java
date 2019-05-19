package com.lice.iuweather;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.lice.iuweather.Instance.WeatherSubject;
import com.lice.iuweather.db.City;
import com.lice.iuweather.db.CityNode;
import com.lice.iuweather.service.AutoUpdateService;
import com.lice.iuweather.service.Delivery;
import com.lice.iuweather.util.HttpUtil;
import com.lice.iuweather.util.Utility;


import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.basic.Basic;
import interfaces.heweather.com.interfacesmodule.bean.search.Search;
import interfaces.heweather.com.interfacesmodule.bean.weather.Weather;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.LifestyleBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    TextView titleCityText;

    TextView titleUpdateTimeText;

    TextView degreeText;

    TextView weatherInfoText;

    TextView aqiText;

    TextView pm25Text;

    TextView comfortText;

    TextView carWashText;

    TextView sportText;

    LinearLayout forecastLayout;

    LinearLayout weatherView;

    SwipeRefreshLayout swipeRefresh;

    Button navButton;

    DrawerLayout drawerLayout;

    Button locateButton;

    MaterialDialog materialDialog;

    private LocationClient mLocationClient;


    private ImageView bingPicImg;

    private ListView listView;

    private RecyclerView recyclerView;

    private String mWeatherId;

    private ScrollView scrollView;

    private LinearLayout nowLayout;

    private int nowHeight;

    private int mDrawerWidth;

    private int statusBarAlpha = 0;

    private float mslideWidth;

    private static String ipAddress;

    private double mLongitude;

    private double mLatitude;

    List<Weather> mWeatherList;

    WifiManager wifiManager;

    private LinearLayout titleBar;

    private MyBDListener myBDListener;

    private CityManagerFragment cityFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        HeConfig.init("HE1808111719301164","19523d5b4c38493cad2e093125a5abe7");
        HeConfig.switchToFreeServerNode();
        getReferences();
        setView();
        setHandler();
        Intent serviceIntent = new Intent(this, AutoUpdateService.class);
        startService(serviceIntent);
        setActionListener();
        requestIpAddress();

    }

    @Override
    protected void onResume() {
        titleBar.getBackground().setAlpha(statusBarAlpha);
        super.onResume();
        int i = 1;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mDrawerWidth = recyclerView.getWidth();
        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        float density = dm.density;
        int height = dm.heightPixels;
        nowHeight =(int)(height * 0.2);
        super.onWindowFocusChanged(hasFocus);
        int i = 0;
    }

    public void getReferences(){
        titleBar = findViewById(R.id.title_bar);
        weatherView = findViewById(R.id.weather_linearLayout);
        nowLayout = findViewById(R.id.now_layout);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        bingPicImg = findViewById(R.id.bing_pic_img);
        scrollView = findViewById(R.id.weather_layout);
        listView = findViewById(R.id.list_view);
        recyclerView = findViewById(R.id.city_recycler);
        titleCityText = findViewById(R.id.title_city);
        titleUpdateTimeText = findViewById(R.id.update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.carwash_text);
        sportText = findViewById(R.id.sport_text);
        locateButton = findViewById(R.id.locate_button);
        mLocationClient = new LocationClient(getApplicationContext());
        myBDListener = new MyBDListener();
        cityFragment = (CityManagerFragment) getSupportFragmentManager().findFragmentById(R.id.city_manager_frag);
    }

    public void setView(){
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
            titleBar.getBackground().setAlpha(0);
            weatherView.setVisibility(View.GONE);
        };
    }

    public void setHandler(){
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case Delivery.UPDATE_WEATHER:
                        requestIpAddress();
                        break;
                    case Delivery.UPDATE_BINGPIC:
                        loadBingPic();
                        break;
                    case Delivery.REFRESH_CITY:
                        cityFragment.refreshCity();
                        cityFragment.onMoveButton();
                        drawerLayout.closeDrawers();
                        CityNode cityNode = cityFragment.getLastCity();
                        swipeRefresh.setRefreshing(true);
                        requestWeatherInfo(cityNode.getCid());
                        break;
                    case Delivery.SHOW_CITY:
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                        String weatherId = pref.getString("NowWeatherId", null);
                        swipeRefresh.setRefreshing(true);
                        requestWeatherInfo(weatherId);
                        drawerLayout.closeDrawers();
                    case Delivery.BACK_WEATHER:
                        break;
                    //case Delivery.GLIDE_BINGPIC:

                }
                return false;
            }
        });
        Delivery.setHandler(handler);
    }

    public void setActionListener() {
        if (Build.VERSION.SDK_INT >= 23) {
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    float Y = v.getScrollY();
                    if (Y > nowHeight)
                        Y = nowHeight;
                    float offset = Y / nowHeight;
                    if (titleBar != null) {
                        titleBar.getBackground().setAlpha((int) (offset * 255));
                        statusBarAlpha = (int) (offset * 255);
                    }

                }
            });
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String cityId = WeatherSubject.getInstance().getNowId();
//                requestWeatherInfo(mWeatherId);
                requestWeatherInfo(cityId);
            }
        });
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
                mslideWidth = v * mDrawerWidth;
                swipeRefresh.setScrollX((int) (-1 * mslideWidth));
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {

            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                if(cityFragment.openFlag){
                    cityFragment.onMoveButton();
                }
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getLocatePermission();
                materialDialog = new MaterialDialog.Builder(WeatherActivity.this)
                        .title("正在定位")
                        .content("请稍等")
                        .progress(true, 0)
                        .progressIndeterminateStyle(false)
                        .canceledOnTouchOutside(false)
                        .show();
                requestLocalWeather();


            }
        });
    }

    class MyBDListener extends BDAbstractLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            int i = bdLocation.getLocType();
            double latitude = bdLocation.getLatitude();
            double longitude = bdLocation.getLongitude();

            mLatitude = latitude;
            mLongitude = longitude;
            requestWeatherIdByLoc();
        }
    }



    public void refreshBingPic(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String imageInfo = pref.getString("bing_pic", null);
        if (imageInfo != null) {
            Glide.with(this).load(imageInfo).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }

    public void requestLocalWeather(){
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            getLocatePermission();
            requestLocation();
        }else{

            requestIpAddress();
        }
    }

    public void requestIpAddress(){
        ipAddress = null;
        HttpUtil.sendOkHttpRequest("http://pv.sohu.com/cityjson?ie=utf-8", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resText = response.body().string();

                String pattern = "\\{.*\\}";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(resText);
                String jsonText = "";
                if(m.find()){
                    jsonText = m.group();
                }
                try{
                    //JSONArray jsonArray = new JSONArray(jsonText);
                    JSONObject jsonObject = new JSONObject(jsonText);
                    ipAddress = jsonObject.getString("cip");
                    requestWeatherIdByIp();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void requestWeatherIdByLoc(){
        HttpUtil.sendOkHttpRequest("https://search.heweather.com/find?location=" + mLongitude + "," + mLatitude +
                "&key=19523d5b4c38493cad2e093125a5abe7", new Callback(){
            @Override
            public void onResponse(Call call, Response response) {
                try{
                    String responseText = response.body().string();
                    String cityId = Utility.handleWeatherResponse_new(responseText);
                    WeatherSubject.getInstance().setNowId(cityId);
                    mWeatherId = cityId;
                    requestWeatherInfo(mWeatherId);
                }catch (Exception e){
                    //Log.d(TAG, "onResponse: Failed");
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void requestWeatherIdByIp(){
        if(ipAddress != null){
            HttpUtil.sendOkHttpRequest("https://search.heweather.com/find?location=" + ipAddress +
                    "&key=19523d5b4c38493cad2e093125a5abe7", new Callback(){
                @Override
                public void onResponse(Call call, Response response) {
                    try{
                        String responseText = response.body().string();
                        String cityId = Utility.handleWeatherResponse_new(responseText);
                        mWeatherId = cityId;
                        WeatherSubject.getInstance().setNowId(cityId);
                        requestWeatherInfo(mWeatherId);
                    }catch (Exception e){
                        //Log.d(TAG, "onResponse: Failed");
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void requestWeatherInfo(final String weatherId) {

        mWeatherId = weatherId;
        HeWeather.getWeather(this, weatherId, new HeWeather.OnResultWeatherDataListBeansListener() {
            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(List<Weather> list) {
                mWeatherList = list;
                showWeatherInfo(mWeatherList.get(0));
                if(materialDialog != null){
                    materialDialog.dismiss();
                }

            }
        });
        HeWeather.getAirNow(this, weatherId, new HeWeather.OnResultAirNowBeansListener() {
            @Override
            public void onError(Throwable throwable) {

                throwable.printStackTrace();
            }

            @Override
            public void onSuccess(List<AirNow> list) {
                showAirNow(list.get(0));
            }
        });
    }

    private void showAirNow(AirNow airNow){
        aqiText.setText(airNow.getAir_now_city().getAqi());
        pm25Text.setText(airNow.getAir_now_city().getPm25());
    }


    private void showWeatherInfo(Weather weather) {
        titleCityText.setText(weather.getBasic().getLocation());
        titleUpdateTimeText.setText(weather.getUpdate().getLoc().split("\\s")[1]);
        degreeText.setText(String.format(getResources().getString(R.string.tmpsymbol),weather.getNow().getTmp()));

        weatherInfoText.setText(weather.getNow().getCond_txt());
        forecastLayout.removeAllViews();
        for (ForecastBase forecast : weather.getDaily_forecast()) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.weather_info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.getDate());
            infoText.setText(forecast.getCond_txt_d());
            maxText.setText(forecast.getTmp_max());
            minText.setText(forecast.getTmp_min());
            forecastLayout.addView(view);
        }
        List<LifestyleBase> lifestyleBases =  weather.getLifestyle();
        LifestyleBase comfortBase = lifestyleBases.get(0);
        LifestyleBase carwashBase = lifestyleBases.get(6);
        LifestyleBase sportBase = lifestyleBases.get(3);
        comfortText.setText(String.format(getResources().getString(R.string.comfort),comfortBase.getTxt()));
        carWashText.setText(String.format(getResources().getString(R.string.carwash), carwashBase.getTxt()));
        sportText.setText(String.format(getResources().getString(R.string.sport), sportBase.getTxt()));
        swipeRefresh.setRefreshing(false);
        weatherView.setVisibility(View.VISIBLE);
    }

    private void loadBingPic() {
        HttpUtil.sendOkHttpRequest("http://guolin.tech/api/bing_pic", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String imagInfo = response.body().string();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("bing_pic", imagInfo);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(imagInfo).into(bingPicImg);
                    }
                });
            }
        });
    }

    private void getLocatePermission(){
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(WeatherActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this, permissions, 1);
        }else{
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for (int result : grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须允许权限才可继续运行", Toast.LENGTH_SHORT).show();
                            //finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{
                    Toast.makeText(this, "发生位置错误", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void requestLocation(){
        mLocationClient.stop();
        mLocationClient = null;
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myBDListener);
        mLocationClient.start();
    }

    public void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        //mLocationClient.setLocOption(option);
    }



}
