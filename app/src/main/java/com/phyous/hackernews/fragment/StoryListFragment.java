package com.phyous.hackernews.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
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

import com.todddavies.components.progressbar.ProgressWheel;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class StoryListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<StoryResponse>, ListView.OnScrollListener {
    public static final String TAG = StoryListFragment.class.getSimpleName();

    private Page mPage = Page.FRONT;
    private Request mRequest = Request.NEW;
    private Result mLastResult = Result.EMPTY;
    private ListView mList;
    private View mListviewFooter;
    private StoryAdapter mAdapter;
    private PullToRefreshLayout mPullToRefreshLayout;
    private ProgressWheel mProgressWheelLoading;
    private ProgressWheel mProgressWheelMore;

    // Used in conjunction with OnScrollListener to determine when to load more data in the feed
    private boolean mUserScrolled = false;

    // Stores when the loader first started (so we can ensure a consistent load experience)
    private long mLoaderStartTime = 0;

    // Loader ID
    private static final int STORY_LOADER_ID = 63531;

    // Duration which listview fades in and out
    private static final long FADE_ANIMATION_DURATION = 500;

    // Minimum time which the loader will display (so UI doesn't flicker during quick load times)
    private static final long MIN_LOADING_ANIMATION = 1500;

    public static StoryListFragment newInstance() {
        return new StoryListFragment();
    }

    public StoryListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(STORY_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);

        mListviewFooter = inflater.inflate(R.layout.adapter_story_footer, null, false);
        mList = (ListView) rootView.findViewById(R.id.story_list);
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);
        mProgressWheelLoading = (ProgressWheel) rootView.findViewById(R.id.spinner);
        mProgressWheelMore = (ProgressWheel) mListviewFooter.findViewById(R.id.loading_spinner);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up footer for infinite scroll
        mListviewFooter.setVisibility(View.GONE);
        mList.addFooterView(mListviewFooter);
        mList.setOnScrollListener(this);

        // Set up listview adapter
        mAdapter = new StoryAdapter(getActivity());
        mList.setAdapter(mAdapter);

        // Set up pull to refresh
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        refreshContent();
                    }
                })
                .setup(mPullToRefreshLayout);

        // Ensure animation is displayed if we're grabbing new data
        if (isLoading()) {
            startRefreshAnimation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Pause animations when fragment not visible
        hideSpinner(mProgressWheelLoading);
        hideSpinner(mProgressWheelMore);
        mPullToRefreshLayout.setRefreshComplete();
    }

    private void refreshContent() {
        mRequest = Request.REFRESH;
        getLoaderManager().restartLoader(STORY_LOADER_ID, null, StoryListFragment.this);
        startRefreshAnimation();
    }

    private void startRefreshAnimation() {
        if (mList == null || mProgressWheelLoading == null || !isLoading() || mRequest == Request.MORE)
            return;

        // Set initial view state visibility
        mProgressWheelLoading.setAlpha(0f);
        showSpinner(mProgressWheelLoading);

        // Fade in progress wheel
        mProgressWheelLoading.animate().alpha(1f).setDuration(FADE_ANIMATION_DURATION).setListener(null);

        // Fade out listview
        mList.animate().alpha(0f).setDuration(FADE_ANIMATION_DURATION).setListener(null);
    }

    private void finishRefreshAnimation() {
        if (mList == null || mProgressWheelLoading == null) return;

        // Use a layout listener to determine when the listview starts a new layout cycle
        // (and thus is ready to start fading in)
        mList.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mList.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                        // Get rid of Progresswheel
                        hideSpinner(mProgressWheelLoading);

                        // Fade in listview
                        mList.animate().alpha(1f).setDuration(FADE_ANIMATION_DURATION).setListener(null);
                    }
                }
        );
    }

    @Override
    public Loader<StoryResponse> onCreateLoader(int id, Bundle args) {
        setLoading(true);
        return new StoryLoader(getActivity(), mPage, mRequest);
    }

    @Override
    public void onLoadFinished(Loader<StoryResponse> loader, final StoryResponse response) {
        mLastResult = response.result;

        switch (mLastResult) {

            case SUCCESS: // first page
                final long loadingTime = System.currentTimeMillis() - mLoaderStartTime;
                final long animationDelay = loadingTime > MIN_LOADING_ANIMATION ?
                        0 : MIN_LOADING_ANIMATION - loadingTime;
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshLayout.setRefreshComplete();
                        finishRefreshAnimation();
                        mAdapter.replaceAll(response.stories);
                    }
                }, animationDelay);

                break;

            case MORE: // new data from web
                hideSpinner(mProgressWheelMore);
                mAdapter.addAll(response.stories);
                break;

            case FNID_EXPIRED: // the link was expired - refresh the page
                refreshContent();
                break;

            case FAILURE: // Show error message
                Toast.makeText(getActivity(),
                        getActivity().getString(R.string.content_download_error),
                        Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }

        // Set correct loading state
        setLoading(false);

        // If the cache is expired, do a refresh
        checkCacheExpiry(response);

        // Get rid of loader now that we're done with it
        getLoaderManager().destroyLoader(STORY_LOADER_ID);
    }

    @Override
    public void onLoaderReset(Loader<StoryResponse> loader) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            mUserScrolled = true;
        }
    }

    // Trick to figure out when last element in the list is visible. We use this a cue to grab more
    // data in the feed.
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount && mUserScrolled;

        if (loadMore) {
            // Start loading animation
            showSpinner(mProgressWheelMore);
            mListviewFooter.setVisibility(View.VISIBLE);

            // Grab more data
            mRequest = Request.MORE;
            mUserScrolled = false;
            getLoaderManager().restartLoader(STORY_LOADER_ID, null, this);
        }
    }

    // Make ProgressWheel visible and start spinning animation
    private void showSpinner(ProgressWheel wheel) {
        wheel.setVisibility(View.VISIBLE);
        wheel.spin();
    }

    // Hide ProgressWheel and stop spinning animation
    private void hideSpinner(ProgressWheel wheel) {
        wheel.setVisibility(View.GONE);
        wheel.stopSpinning();
    }

    // Set loading state of the StoryLoader
    private void setLoading(boolean state) {
        if(state) {
            mLoaderStartTime = System.currentTimeMillis();
        } else {
            mLoaderStartTime = 0;
        }
    }

    // Returns loading state of StoryLoader
    private boolean isLoading() {
        if (mLoaderStartTime == 0) {
            return false;
        } else {
            return true;
        }
    }

    private void checkCacheExpiry(StoryResponse response) {
        if (response.timestamp != null &&
                response.timestamp.time > Comment.JAN_1_2012 &&
                System.currentTimeMillis() - response.timestamp.time > Comment.CACHE_EXPIRATION) {
            refreshContent();
        }
    }
}
