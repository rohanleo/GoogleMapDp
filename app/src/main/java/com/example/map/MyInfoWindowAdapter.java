package com.example.map;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public MyInfoWindowAdapter(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_info_window,null);
        TextView name = view.findViewById(R.id.name);
        TextView type = view.findViewById(R.id.type);
        TextView opening = view.findViewById(R.id.openingTime);
        TextView closing = view.findViewById(R.id.closingTime);
        TextView addedby = view.findViewById(R.id.addedBy);
        TextView addedon = view.findViewById(R.id.addedOn);
        TextView remark = view.findViewById(R.id.remark);
        TextView phoneNum = view.findViewById(R.id.phoneNum);

        InfoWindowData infoWindowData = (InfoWindowData) marker.getTag();

        name.setText(infoWindowData.getName());
        type.setText(infoWindowData.getType());
        opening.setText(infoWindowData.getOpening());
        closing.setText(infoWindowData.getClosing());
        addedby.setText(infoWindowData.getAddedby());
        addedon.setText(infoWindowData.getAddedon());
        remark.setText(infoWindowData.getRemark());
        phoneNum.setText(infoWindowData.getPhoneNum());
        return view;
    }
}
