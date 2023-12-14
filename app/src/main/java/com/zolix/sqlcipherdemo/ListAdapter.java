package com.zolix.sqlcipherdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ListAdapter extends BaseAdapter {
    private List<Name> names;
    private Context mContext;
    public ListAdapter(Context context, List<Name> names){
        mContext = context;
        this.names = names;
    }
    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Name getItem(int position) {
        return names.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_name, null);
            viewHolder.nameTV = convertView.findViewById(R.id.name_tv);
            viewHolder.deleteTV = convertView.findViewById(R.id.delete_tv);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.nameTV.setText(names.get(position).name);
        viewHolder.deleteTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NameDao.DeleteNameForID(names.get(position)._id);
                names.remove(position);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    private class ViewHolder{
        public TextView nameTV;
        public TextView deleteTV;
    }
}
