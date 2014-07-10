package com.phyous.hackernews.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
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
        TextView storyTitle = (TextView) view.findViewById(R.id.story_title);
        storyTitle.setText(generateTitleText(story));
        TextView voteCount = (TextView) view.findViewById(R.id.vote_count);
        voteCount.setText(Integer.toString(story.numPoints));
        TextView storyAge = (TextView) view.findViewById(R.id.story_age);
        storyAge.setText(story.ago);
        TextView storyComments = (TextView) view.findViewById(R.id.story_comments);
        storyComments.setText(Integer.toString(story.numComments));

        return view;
    }

    private Spanned generateTitleText(Story story) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>");
        sb.append(story.title);
        sb.append("</b>");
        sb.append(" <small><font color=\"grey\"> (");
        sb.append(story.domain);
        sb.append(")</small></font>");
        return Html.fromHtml(sb.toString());
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
