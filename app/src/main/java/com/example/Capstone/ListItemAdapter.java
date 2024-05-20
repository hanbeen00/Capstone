package com.example.Capstone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter {
    ArrayList<ListItem> items = new ArrayList<ListItem>();
    Context context;

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        ListItem listItem = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        TextView nameText = convertView.findViewById(R.id.name);
        TextView addressText = convertView.findViewById(R.id.address);
        TextView progressText = convertView.findViewById(R.id.progress);
        TextView textText = convertView.findViewById(R.id.text);
        TextView timeText = convertView.findViewById(R.id.time);

        nameText.setText(listItem.getName());
        addressText.setText(listItem.getAddress());
        progressText.setText(listItem.getProgress());
        textText.setText(listItem.getText());
        timeText.setText(listItem.getTime());

        return convertView;
    }

    public void addItem(ListItem item) {
        items.add(item);
    }
}
