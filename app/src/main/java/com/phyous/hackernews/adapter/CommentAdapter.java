package com.phyous.hackernews.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phyous.hackernews.R;
import com.phyous.hackernews.data.model.Comment;
import com.phyous.hackernews.util.LayoutHelper;
import com.phyous.hackernews.util.StringHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommentAdapter extends BaseAdapter {
    private class ViewHolder {
        public Comment comment;
        public TextView commentDescription, commentText;
        public LinearLayout depthLayout;
    }

    private Context mContext;
    private List<Comment> mObjects = new ArrayList<Comment>();

    public CommentAdapter(Context context) {
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
        final Comment comment = (Comment) getItem(position);
        ViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.adapter_comment, parent, false);

            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind data to to ViewHolder views
        bindViews(holder, comment);

        // Generate nesting layout for comment based on depth
        generateDepthLayout(holder);

        return convertView;
    }

    private ViewHolder createViewHolder(View parent) {
        ViewHolder holder = new ViewHolder();
        holder.commentDescription = (TextView) parent.findViewById(R.id.comment_description);
        holder.commentText = (TextView) parent.findViewById(R.id.comment_text);
        holder.depthLayout = (LinearLayout) parent.findViewById(R.id.comment_depth_layout);

        return holder;
    }

    private void bindViews(ViewHolder holder, Comment comment) {
        holder.comment = comment;
        holder.commentDescription.setText(formatDescription(holder.comment));
        holder.commentText.setText(formatComment(holder.comment));
    }

    private void generateDepthLayout(ViewHolder holder) {
        // Remove child views from layout (if it's recycled)
        if(holder.depthLayout.getChildCount() > 0) {
            holder.depthLayout.removeAllViews();
        }

        // Add appropriate number of bars depending on depth
        for (int i = 0; i < holder.comment.depth; i++) {
            holder.depthLayout.addView(createCommentDepthLine());
        }

        // Invalidate the layout so it's re-drawn
        holder.depthLayout.invalidate();
    }

    private LinearLayout createCommentDepthLine() {
        LinearLayout layoutLine = new LinearLayout(mContext);
        layoutLine.setOrientation(LinearLayout.HORIZONTAL);
        layoutLine.setBackgroundColor(mContext.getResources().getColor(R.color.grey_hint_text));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutHelper.dpToPx(mContext, 1), LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(LayoutHelper.dpToPx(mContext, 6), 0, 0, 0);

        layoutLine.setLayoutParams(layoutParams);
        return layoutLine;
    }

    private Spanned formatDescription(Comment comment) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(comment.username).append("</b>");
        sb.append(" <small><font color=\"grey\">").append(comment.ago).append("</small></font>");
        return Html.fromHtml(sb.toString());
    }

    private CharSequence formatComment(Comment comment) {
        return StringHelper.trim(Html.fromHtml(comment.html));
    }

    public void add(Comment object) {
        mObjects.add(object);
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends Comment> coll) {
        mObjects.addAll(coll);
        notifyDataSetChanged();
    }

    public void clear() {
        mObjects.clear();
        notifyDataSetChanged();
    }

    public List<Comment> getArray() {
        return mObjects;
    }
}
