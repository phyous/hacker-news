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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_comment, parent, false);

        final Comment comment = (Comment) getItem(position);
        final TextView commentDescription = (TextView) view.findViewById(R.id.comment_description);
        commentDescription.setText(formatDescription(comment));
        final TextView commentText = (TextView) view.findViewById(R.id.comment_text);
        commentText.setText(formatComment(comment));

        final LinearLayout depthLayout =
                (LinearLayout) view.findViewById(R.id.comment_depth_layout);
        for (int i = 0; i < comment.depth; i++) {
            depthLayout.addView(createCommentDepthLine());
        }
        depthLayout.invalidate();

        return view;
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
