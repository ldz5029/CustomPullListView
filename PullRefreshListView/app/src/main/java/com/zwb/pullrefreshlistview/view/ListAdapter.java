package com.zwb.pullrefreshlistview.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zwb.pullrefreshlistview.R;

import java.util.List;

/**
 * Created by zwb
 * Description
 * Date 2017/5/15.
 */

public class ListAdapter extends BaseAdapter {
    private List<String> mdatas;
    private Context mContext;

    public ListAdapter(Context mContext, List<String> mdatas) {
        this.mContext = mContext;
        this.mdatas = mdatas;
    }

    @Override
    public int getCount() {
        return mdatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mdatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_list, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvContent.setText(mdatas.get(position));
        return convertView;
    }

    static class ViewHolder {
        public TextView tvContent;
        private View view;

        public ViewHolder(View view) {
            this.view = view;
            tvContent = (TextView) view.findViewById(R.id.tv_content);
        }
    }

    public void notifyData(){
        notifyDataSetChanged();
    }

}
