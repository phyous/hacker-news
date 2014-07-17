package com.phyous.hackernews.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.phyous.hackernews.R;
import com.phyous.hackernews.adapter.CommentAdapter;
import com.phyous.hackernews.data.loader.CommentsLoader;
import com.phyous.hackernews.data.model.Comment;
import com.phyous.hackernews.data.model.Request;
import com.phyous.hackernews.data.model.Result;
import com.phyous.hackernews.data.model.Story;
import com.phyous.hackernews.data.parser.CommentsParser.CommentsResponse;

import java.io.Serializable;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class CommentListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<CommentsResponse> {
    public static final String STORY = CommentListFragment.class.getSimpleName() + ".story";
    public static final int NO_STORY_ID = -1;
    public static final String COMMENTS = CommentListFragment.class.getSimpleName() + ".comments";

    private static final long CACHE_EXPIRATION = 1000 * 60 * 5; // 5 minutes

    private Story mStory;

    /** The backing array of comments stored in onSaveInstanceState and restored in onActivityCreated **/
    private List<Comment> mTempComments;

    private Result mLastResult = Result.EMPTY;
    private ListView mList;
    private CommentAdapter mAdapter;
    private Request mRequest = Request.NEW;
    private PullToRefreshLayout mPullToRefreshLayout;

    /* Create fragment using this method to ensure arguments are properly passed */
    public static CommentListFragment newInstance(Story story) {
        CommentListFragment myFragment = new CommentListFragment();

        Bundle args = new Bundle();
        args.putSerializable(STORY, story);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStory = (Story) getArguments().getSerializable(STORY);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_comment_list, container, false);
        mList = (ListView) rootView.findViewById(R.id.comment_list);
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //setStory((Story) savedInstanceState.getSerializable(STORY));
            // restore the List<Comment> that backs the ListAdapter (can't use the automatic one from the
            // loader.onLoadFinished because it breaks comment folding)
            mTempComments = (List<Comment>) savedInstanceState.getSerializable(COMMENTS);
        }

        // start loading
        getLoaderManager().initLoader(0, null, this);

        // find all the views
        //findViews(savedInstanceState);

        // setup adapter
        if (mAdapter == null) {
            mAdapter = new CommentAdapter(getActivity());
            mList.setAdapter(mAdapter);

            if (mTempComments != null) mAdapter.addAll(mTempComments);
        }

        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        mRequest = Request.REFRESH;
                        getLoaderManager().restartLoader(0, null, CommentListFragment.this);
                    }
                })
        .setup(mPullToRefreshLayout);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STORY, mStory);
        if (mAdapter != null) outState.putSerializable(COMMENTS, (Serializable) mAdapter.getArray());
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<CommentsResponse> onCreateLoader(int id, Bundle args) {
        return new CommentsLoader(getActivity(), mRequest, mStory.storyId);
    }

    @Override
    public void onLoadFinished(Loader<CommentsResponse> loader, CommentsResponse response) {
        Story story = response.story;
        mLastResult = response.result;

        mPullToRefreshLayout.setRefreshComplete();

        if (response.result == Result.SUCCESS) {

            if (mTempComments == null) {
                // setup adapter & list
                mAdapter.clear();
                mAdapter.addAll(response.comments);

                // if it was a new request, we should scroll to the top of the page
                if (mRequest == Request.NEW) {
                    mList.setSelection(0);
                }

            } else {
                // Comments have already been restored from List<Comment> in onActivityCreated() and onCreateView()
                // this fixes the bug where Comments that have been folded would get duplicated
                // onLoadFinished gets called twice on orientation change (seems like a bug in Fragment.performStart())
            }

            // check for cache expiration
            if (System.currentTimeMillis() - response.timestamp.time > CACHE_EXPIRATION) {
                // still show stuff, but restart loading
                mRequest = Request.REFRESH;
                getLoaderManager().restartLoader(0, null, this);
            }

            // setup story header
            mStory = story;
            bindStoryHeader();
        }
    }

    @Override
    public void onLoaderReset(Loader<CommentsResponse> loader) {

    }

    /** Bind data from mStory to views in mHeader **/
    private void bindStoryHeader() {
        //TODO: Fill in info about story in top of fragment
    }
}
