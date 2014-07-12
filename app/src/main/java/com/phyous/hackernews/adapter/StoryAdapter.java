package com.phyous.hackernews.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phyous.hackernews.R;
import com.phyous.hackernews.activity.CommentActivity;
import com.phyous.hackernews.data.model.Story;
import com.phyous.hackernews.fragment.CommentListFragment;

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

        final Story story = (Story) getItem(position);
        final TextView storyTitle = (TextView) view.findViewById(R.id.story_title);
        storyTitle.setText(generateTitleText(story));
        final TextView voteCount = (TextView) view.findViewById(R.id.vote_count);
        voteCount.setText(Integer.toString(story.numPoints));
        final TextView storyAge = (TextView) view.findViewById(R.id.story_age);
        storyAge.setText(story.ago);
        final TextView storyComments = (TextView) view.findViewById(R.id.story_comments);
        storyComments.setText(Integer.toString(story.numComments));

        final RelativeLayout storyInfoLayout =
                (RelativeLayout) view.findViewById(R.id.view_layout);
        storyInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = story.url;
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(story.url));
                mContext.startActivity(browserIntent);
            }
        });

        final RelativeLayout commentsLayout =
                (RelativeLayout) view.findViewById(R.id.story_info_layout);
        commentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentsIntent = new Intent(mContext, CommentActivity.class);
                commentsIntent.putExtra(CommentListFragment.STORY, story);
                mContext.startActivity(commentsIntent);
            }
        });

        return view;
    }

    private Spanned generateTitleText(Story story) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(story.title).append("</b>");;
        sb.append(" <small><font color=\"grey\"> (").append(story.domain).append(")</small></font>");
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
