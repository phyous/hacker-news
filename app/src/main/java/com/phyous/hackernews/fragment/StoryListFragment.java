package com.phyous.hackernews.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.phyous.hackernews.R;
import com.phyous.hackernews.adapter.StoryAdapter;
import com.phyous.hackernews.data.loader.StoryLoader;
import com.phyous.hackernews.data.model.Comment;
import com.phyous.hackernews.data.model.Page;
import com.phyous.hackernews.data.model.Request;
import com.phyous.hackernews.data.model.Result;
import com.phyous.hackernews.data.parser.StoryParser.StoryResponse;


public class StoryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<StoryResponse> {
    private Page mPage = Page.FRONT;
    private Request mRequest = Request.NEW;
    private Result mLastResult = Result.EMPTY;
    private ListView mList;
    private StoryAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);
        mList = (ListView) rootView.findViewById(R.id.story_list);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new StoryAdapter(getActivity());
        mList.setAdapter(mAdapter);
    }

    @Override
    public Loader<StoryResponse> onCreateLoader(int id, Bundle args) {
        return new StoryLoader(getActivity(), mPage, mRequest);
    }

    @Override
    public void onLoadFinished(Loader<StoryResponse> loader, StoryResponse response) {
        mLastResult = response.result;
        Toast msg;

        switch (mLastResult) {

            case SUCCESS: // first page
                mAdapter.clear();
                mAdapter.addAll(response.stories);
                break;

            case MORE: // new data from web
                mAdapter.addAll(response.stories);
                break;

            case FNID_EXPIRED: // the link was expired - refresh the page
                msg = Toast.makeText(getActivity(), getActivity().getString(R.string.link_expired), Toast.LENGTH_SHORT);
                msg.show();

                // start loader with refresh request
                mRequest = Request.REFRESH;
                getLoaderManager().restartLoader(0, null, this);
                break;

            case FAILURE: // Show error message
                msg = Toast.makeText(getActivity(), getActivity().getString(R.string.content_download_error), Toast.LENGTH_SHORT);
                msg.show();
                break;

            default:
                break;
        }

        checkCacheExpiry(response);
    }

    @Override
    public void onLoaderReset(Loader<StoryResponse> loader) {

    }

    private void checkCacheExpiry(StoryResponse response) {
        if (response.timestamp != null && response.timestamp.time > Comment.JAN_1_2012
                && System.currentTimeMillis() - response.timestamp.time > Comment.CACHE_EXPIRATION) {
            // still display cached values & need to start refresh
            mRequest = Request.REFRESH;
            getLoaderManager().restartLoader(0, null, this);
        }
    }
}
