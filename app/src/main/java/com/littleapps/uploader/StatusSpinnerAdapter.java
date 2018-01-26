package com.littleapps.uploader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jitendra on 11/11/2017.
 */

public class StatusSpinnerAdapter extends ArrayAdapter<String> {

    private ArrayList<String> statusList = new ArrayList<>();

    public StatusSpinnerAdapter(@NonNull Context context, int resource, ArrayList<String> statusList) {
        super(context, resource);
        this.statusList = statusList;
        Context context1 = context;
    }

    public StatusSpinnerAdapter(@NonNull Context context, int resource, int textViewResourceId, ArrayList<String> statusList) {
        super(context, resource, textViewResourceId);
        this.statusList = statusList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        String status = statusList.get(position);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_spinner_status, parent, false);
        TextView statusTxt = view.findViewById(R.id.status);
        statusTxt.setText(status);

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String status = statusList.get(position);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_spinner_status, parent, false);
        TextView statusTxt = view.findViewById(R.id.status);
        statusTxt.setText(status);

        return view;
    }

    @Override
    public int getCount() {
        return statusList.size();
    }

}
