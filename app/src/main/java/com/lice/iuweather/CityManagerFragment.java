package com.lice.iuweather;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lice.iuweather.Instance.WeatherSubject;
import com.lice.iuweather.adapter.CityAdapter;
import com.lice.iuweather.db.City;
import com.lice.iuweather.db.CityNode;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class CityManagerFragment extends Fragment {

    private CityAdapter cityAdapter;
    private RecyclerView recyclerView;
    private List<CityNode> cityNodes;
    private FloatingActionButton floatButton;
    private FloatingActionButton addButton;
    private FloatingActionButton deleteButton;
    private int rotate = 0;
    private int rotation = 135;
    private boolean rotateDirection = false;
    public boolean openFlag = false;
    private List<FloatingActionButton> buttons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.citymanager_frag,container,false);
        recyclerView = view.findViewById(R.id.city_recycler);
        cityNodes = DataSupport.findAll(CityNode.class);
        floatButton = view.findViewById(R.id.float_button);
        addButton = view.findViewById(R.id.add_button);
        deleteButton = view.findViewById(R.id.delete_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),SearchCityActivity.class);
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            @SuppressLint("RestrictedApi")
            public void onClick(View v) {
                if(!cityAdapter.getModel()){
                    cityAdapter.editEnter();
                    floatButton.setVisibility(View.GONE);
                    onMoveButton();
                }else{
                    floatButton.setVisibility(View.VISIBLE);
                    cityAdapter.deleteCity();
                    cityAdapter.notifyDataSetChanged();
                    cityAdapter.editExit();
                }
            }
        });
        buttons.add(addButton);
        buttons.add(deleteButton);
        floatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRotate(v);
                onMoveButton();
            }
        });
        cityAdapter = new CityAdapter(cityNodes, new CityAdapter.onButtonListener() {
            @Override
            public void onButtonClick(int position) {
                CityNode cityNode = cityNodes.get(position);
                WeatherActivity activity = (WeatherActivity) getActivity();
                activity.drawerLayout.closeDrawers();
                activity.swipeRefresh.setRefreshing(true);
                activity.requestWeatherInfo(cityNode.getCid());
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(cityAdapter);
        return view;
    }

    public void refreshCity(){
        List<CityNode> list = DataSupport.findAll(CityNode.class);
        cityNodes.add(list.get(list.size()-1));
        cityAdapter.notifyDataSetChanged();
        cityAdapter.addData();
        WeatherSubject.getInstance().setNowId(list.get(list.size()-1).getCid());
        WeatherSubject.getInstance().updateAllObserver();
        recyclerView.smoothScrollToPosition(cityAdapter.getItemCount()-1);
    }

    public CityNode getLastCity(){
        return cityNodes.get(cityNodes.size()-1);
    }

    public void onMoveButton(){
        float[] distance = {200,400};
        for(int i = 0;i < buttons.size(); i++){
            ObjectAnimator objectAnimatorY;
            if(!openFlag){
                objectAnimatorY = ObjectAnimator.ofFloat(buttons.get(i), "x",buttons.get(i).getX(),floatButton.getX()-distance[i]);
            }else{
                objectAnimatorY = ObjectAnimator.ofFloat(buttons.get(i), "x",buttons.get(i).getX(),floatButton.getX());
            }
            objectAnimatorY.setDuration(350);
            objectAnimatorY.start();
        }
        openFlag = !openFlag;
    }

    private void onRotate(View v){
        ObjectAnimator animator;
        if(rotateDirection){
            animator = ObjectAnimator.ofFloat(v, "rotation",rotate,rotate - rotation);
            rotate -= rotation;

        }else{
            animator = ObjectAnimator.ofFloat(v, "rotation",rotate,rotate + rotation);
            rotate += rotation;
        }

        animator.setDuration(350);
        animator.start();
        rotateDirection = !rotateDirection;
    }

}