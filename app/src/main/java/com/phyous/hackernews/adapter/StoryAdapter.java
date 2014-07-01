package com.phyous.hackernews.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.phyous.hackernews.R;
import com.phyous.hackernews.data.model.Story;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<Story> mObjects = new ArrayList<Story>();

    public StoryAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_story, parent, false);

        Story story = (Story) getItem(position);
        TextView tv = (TextView) view.findViewById(R.id.story_text);
        tv.setText(story.title);

        return view;
    }

    public void add(Story object) {
        mObjects.add(object);
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends Story> coll) {
        mObjects.addAll(coll);
        notifyDataSetChanged();
    }

    public void clear() {
        mObjects.clear();
        notifyDataSetChanged();
    }

    public List<Story> getArray() {
        return mObjects;
    }
}
