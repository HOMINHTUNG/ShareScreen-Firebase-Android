package com.hominhtung.seenscreen.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.hominhtung.seenscreen.R;
import com.hominhtung.seenscreen.object.driverItem;

import java.util.ArrayList;

import static com.hominhtung.seenscreen.activity.ShowDriverActivity.sendMessage;

/**
 * Created by HOMINHTUNG-PC on 3/29/2018.
 */

public class driverAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<driverItem> driver;

    public driverAdapter(Context context, ArrayList<driverItem> mdriverItem) {
        this.context = context;
        this.driver = mdriverItem;
    }

    @Override
    public int getCount() {
        return driver.size();
    }

    @Override
    public Object getItem(int position) {
        return driver.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inf = ((Activity)context).getLayoutInflater();
        View view = inf.inflate(R.layout.layout_custom,parent,false);

        TextView text = (TextView) view.findViewById(R.id.txt_ten);
        text.setText(driver.get(position).getNameDevice().toString());
        Button call = (Button) view.findViewById(R.id.btn_goi);
        call.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                sendMessage(driver.get(position).getToken(),"CALL");
                // FB_TOKEN_DRIVER = driver.get(position).getNameDevice().toString();

            }
        });

        return view;
    }


}
