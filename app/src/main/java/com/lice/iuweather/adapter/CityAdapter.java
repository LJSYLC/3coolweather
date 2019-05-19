package com.lice.iuweather.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lice.iuweather.Instance.WeatherSubject;
import com.lice.iuweather.R;
import com.lice.iuweather.db.CityNode;


import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> implements com.lice.iuweather.Instance.Observer{

    private List<CityNode> mCityList;

    private Context mContext;

    private onButtonListener mListener;

    private ViewHolder selectCity;

    private static boolean isEditable = false;

    private List<Boolean> selectList = new ArrayList<>();

    private int nowPos = -1;

    private List<CityAdapter.ViewHolder> holders = new ArrayList<>();

    static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView cityName;

        private CheckBox checkBox;

        private CardView cityCard;

        public ViewHolder(View view){
            super(view);
            cityCard = view.findViewById(R.id.city_card);
            cityName = view.findViewById(R.id.city_name);
            checkBox = view.findViewById(R.id.city_checkbox);
        }
    }

    public interface onButtonListener{
        void onButtonClick(int position);
    }

    public CityAdapter(List<CityNode> list, onButtonListener listener){
        mCityList = list;
        WeatherSubject.getInstance().add(this);
        mListener = listener;
        for (int i = 0; i <  mCityList.size(); i++){
            selectList.add(false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        if(mContext == null){
            mContext = viewGroup.getContext();
        }
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.city_item,viewGroup,false);
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                if(isEditable){
                    CheckBox checkBox = viewHolder.checkBox;
                    checkBox.setChecked(!checkBox.isChecked());
                    selectList.set(position, checkBox.isChecked());
                }else{
                    if(mListener != null){
                        if(selectCity != null){
                            mCityList.get(position).setCurrent(true);
                            mCityList.get(nowPos).setCurrent(false);
                            selectCity.cityCard.setCardBackgroundColor(mContext.getResources().getColor(R.color.grayCard));
                        }
                        mListener.onButtonClick(position);
                        nowPos = position;
                        selectCity = viewHolder;
                        WeatherSubject.getInstance().setNowId(mCityList.get(position).getCid());
                        WeatherSubject.getInstance().updateAllObserver();
                        viewHolder.cityCard.setCardBackgroundColor(mContext.getResources().getColor(R.color.whiteCard));
                    }
                }

            }
        });
        holders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        CityNode cityNode = mCityList.get(i);
        viewHolder.cityName.setText(cityNode.getLoc());
        int position = viewHolder.getAdapterPosition();
        if(isEditable){
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setChecked(selectList.get(position));
        }else{
            viewHolder.checkBox.setVisibility(View.GONE);
            if(cityNode.isCurent()){
                selectCity = viewHolder;
                viewHolder.cityCard.setCardBackgroundColor(mContext.getResources().getColor(R.color.whiteCard));
            }else{
                viewHolder.cityCard.setCardBackgroundColor(mContext.getResources().getColor(R.color.grayCard));
            }

        }
    }

    @Override
    public int getItemCount() {
        return mCityList.size();
    }

    public void editEnter(){
        isEditable = true;
        for(ViewHolder viewHolder : holders){
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        }
        for(boolean flag : selectList){
            flag = false;
        }
    }

    public void editExit(){
        isEditable = false;
        for(ViewHolder viewHolder : holders){
            viewHolder.checkBox.setVisibility(View.GONE);
            viewHolder.checkBox.setChecked(false);
        }
    }

    public boolean getModel(){
        return isEditable;
    }

    public void deleteCity(){
        for(int i = mCityList.size() - 1; i >= 0; i--){
            if(selectList.get(i)){
                selectList.remove(i);
                mCityList.get(i).delete();
                mCityList.remove(i);

            }
        }
    }
    public void addData(){
        selectList.add(false);
    }

    @Override
    public void update(String weatherId) {
        if(nowPos != -1){
            int lastPos = mCityList.indexOf(new CityNode(weatherId));
            mCityList.get(lastPos).setCurrent(true);
            mCityList.get(nowPos).setCurrent(false);
            nowPos = lastPos;
        }
    }
}
