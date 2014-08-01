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
import java.util.HashSet;
import java.util.List;

public class StoryAdapter extends BaseAdapter {
    private Context mContext;

    // Holds stories currently held by the adapter
    private List<Story> mObjects = new ArrayList<Story>();

    // Used to ensure duplicate stories arent added to the list
    private HashSet<Long> mObjectSet = new HashSet<Long>();

    private class ViewHolder {
        public Story story;
        public TextView storyTitle, voteCount, storyAge, storyComments;
        public RelativeLayout storyInfoLayout, commentsLayout;
    }

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
        final Story story = (Story) getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.adapter_story, parent, false);

            // Create ViewHolder for various widgets in this view we care about
            holder = createViewHolder(convertView);

            // Attach click listeners and tags holding click listener data
            attachClickListeners(holder);
            attachTags(convertView, holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind data to to ViewHolder views
        bindViews(holder, story);

        return convertView;
    }

    private ViewHolder createViewHolder(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.storyTitle = (TextView) convertView.findViewById(R.id.story_title);
        holder.voteCount = (TextView) convertView.findViewById(R.id.vote_count);
        holder.storyAge = (TextView) convertView.findViewById(R.id.story_age);
        holder.storyComments = (TextView) convertView.findViewById(R.id.story_comments);
        holder.storyInfoLayout = (RelativeLayout) convertView.findViewById(R.id.view_layout);
        holder.commentsLayout = (RelativeLayout) convertView.findViewById(R.id.story_info_layout);
        return holder;
    }

    private void bindViews(ViewHolder holder, Story story) {
        holder.story = story;
        holder.storyTitle.setText(generateTitleText(holder.story));
        holder.voteCount.setText(Integer.toString(holder.story.numPoints));
        holder.storyAge.setText(holder.story.ago);
        holder.storyComments.setText(Integer.toString(holder.story.numComments));
    }

    private void attachClickListeners(ViewHolder holder) {
        holder.storyInfoLayout.setOnClickListener(mStoryClickListener);
        holder.commentsLayout.setOnClickListener(mCommentsClickListener);
    }

    private void attachTags(View convertView, ViewHolder holder) {
        convertView.setTag(holder);
        holder.storyInfoLayout.setTag(holder);
        holder.commentsLayout.setTag(holder);
    }

    private Spanned generateTitleText(Story story) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(story.title).append("</b>");;
        sb.append(" <small><font color=\"grey\"> (").append(story.domain).append(")</small></font>");
        return Html.fromHtml(sb.toString());
    }

    public void add(Story object) {
        conditionalAdd(object);
        notifyDataSetChanged();
    }

    public void replaceAll(Collection<? extends Story> coll) {
        clearObjects();
        for(Story story: coll) {
            conditionalAdd(story);
        }

        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends Story> coll) {
        for(Story story: coll) {
            conditionalAdd(story);
        }

        notifyDataSetChanged();
    }

    // We don't want to add stories that already exist, so we keep a record of existing story IDs in
    // mObjectSet.
    private void conditionalAdd(Story story) {
        if (!mObjectSet.contains(story.id)) {
            mObjects.add(story);
            mObjectSet.add(story.id);
        }
    }

    private void clearObjects() {
        mObjects.clear();
        mObjectSet.clear();
    }

    public void clear() {
        clearObjects();
        notifyDataSetChanged();
    }

    public List<Story> getArray() {
        return mObjects;
    }

    private View.OnClickListener mStoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(holder.story.url));
            mContext.startActivity(browserIntent);
        }
    };

    private View.OnClickListener mCommentsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();

            Intent commentsIntent = new Intent(mContext, CommentActivity.class);
            commentsIntent.putExtra(CommentListFragment.STORY, holder.story);
            mContext.startActivity(commentsIntent);
        }
    };
}
