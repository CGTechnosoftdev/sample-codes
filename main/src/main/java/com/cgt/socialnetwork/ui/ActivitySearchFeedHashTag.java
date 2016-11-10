package com.cgt.socialnetwork.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.MyVolleyHelper;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.RequestBuilder;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.controller.FeedController;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.event.EventAddVote;
import com.cgt.socialnetwork.event.EditPostEvent;
import com.cgt.socialnetwork.event.EventFetchedHashTagFeed;
import com.cgt.socialnetwork.model.FeedBean;
import com.cgt.socialnetwork.task.AutoCancelAsyncTask;
import com.cgt.socialnetwork.utils.AlertDialogFactory;
import com.cgt.socialnetwork.utils.CircleTransform;
import com.cgt.socialnetwork.utils.LifecycleListener;
import com.cgt.socialnetwork.utils.LifecycleProvider;
import com.cgt.socialnetwork.utils.Utils;
import com.cgt.socialnetwork.widget.ThresholdEditText;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by CGTechnosoft
 */
public class ActivitySearchFeedHashTag extends ActivityBase implements AbsListView.OnScrollListener, LifecycleProvider {

    private ImageView ic_back;
    private ThresholdEditText et_search = null;
    private RecyclerView recycle_post_list;
    private TextView tv_empty;

    private FeedController feedController = null;
    private EventBus eventBus;

    protected FeedAdapter mFeedAdapter;
    protected boolean isLoading = false;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private AppModuleManager manager = AppModuleManager.getInstance(ActivitySearchFeedHashTag.this);

    // Set when Activity starts. Since activity does not listen for events after being stopped, we
    // need to do a full sync on return. Event cycle can be moved between onCreate/onDestroy to
    // avoid this but that will require additional complexity of checking when to update the views.
    private boolean mRefreshFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppModuleManager manager = AppModuleManager.getInstance(this);
        feedController = manager.getFeedController();
        eventBus = manager.getEventBus();

        // Clear search feed table and preferences
        Preference.getInstance(ActivitySearchFeedHashTag.this).clearHashTagFeedsPreferences();
        DBHelper.getInstance(ActivitySearchFeedHashTag.this).clearHashTagFeedsData();

        ic_back = (ImageView) findViewById(R.id.ic_back);
        et_search = (ThresholdEditText) findViewById(R.id.et_search);
        ImageButton ic_close = (ImageButton) findViewById(R.id.ic_close);

        ic_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard(et_search);
                ActivitySearchFeedHashTag.this.finish();
            }
        });

        recycle_post_list = (RecyclerView) findViewById(R.id.recycle_post_list);
        tv_empty = (TextView) findViewById(R.id.tv_empty);

        mFeedAdapter = new FeedAdapter(ActivitySearchFeedHashTag.this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(ActivitySearchFeedHashTag.this);
        recycle_post_list.setLayoutManager(mLayoutManager);
        recycle_post_list.setItemAnimator(new DefaultItemAnimator());
        recycle_post_list.setAdapter(mFeedAdapter);

        et_search.setThresholdMillis(500);
        et_search.setHint("Search hashtag...");
        et_search.setOnThresholdTextChanged(new ThresholdEditText.TextChanged() {

            @Override
            public void onThersholdTextChanged(Editable text) {
                // Clear search feed table and preferences
                Preference.getInstance(ActivitySearchFeedHashTag.this).clearHashTagFeedsPreferences();
                DBHelper.getInstance(ActivitySearchFeedHashTag.this).clearHashTagFeedsData();

                mFeedAdapter.clear();
                mFeedAdapter.notifyDataSetChanged();
                recycle_post_list.smoothScrollToPosition(0);

                if (text.toString().trim().length() > 0) {
                    showLoader();
                    feedController.fetchHashTagFeedsAsync(text.toString().trim(), true);
                }
            }
        });

        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard(et_search);
                ActivitySearchFeedHashTag.this.finish();
            }
        });

        String search = getIntent().getStringExtra(com.cgt.socialnetwork.common.Constants.KEY_DATA);
        et_search.setText(search);

        recycle_post_list.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        if (!isLoading) {
                            isLoading = true;

                            showLoader();
                            feedController.loadMoreHashTagFeedsAsync(et_search.getText().toString().trim(), true);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_search_feed;
    }

    private boolean isHashTag(char ch) {
        Pattern pattern = Pattern.compile("[##]([A-Za-z0-9-_]+)");
        Matcher matcher = pattern.matcher("" + ch);
        while (matcher.find()) {
            return true;
        }
        return false;
    }

    //Bus Register and unRegister
    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
        mRefreshFull = true;
        refresh(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.unregister(this);
        mRefreshFull = false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void showLoader() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {

    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {

    }

    public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHolder> {

        private final int color2;
        private Picasso mPicasso;
        private Context context;

        int selection = -1;
        SortedList<FeedBean> mList;

        final Map<String, FeedBean> mUniqueMapping = new HashMap<>();

        public FeedAdapter(Context mContext) {
            super();
            this.context = mContext;
            mPicasso = AppModuleManager.getInstance(mContext).getPicasso();
            color2 = ContextCompat.getColor(mContext, R.color.colorPrimary);
            mList = new SortedList<>(FeedBean.class,
                    new SortedListAdapterCallback<FeedBean>(this) {
                        @Override
                        public int compare(FeedBean p1, FeedBean p2) {
                            if (p1.isPending() != p2.isPending()) {
                                return p1.isPending() ? -1 : 1;
                            }

                            if (p2.getModifiedDate() < p1.getModifiedDate()) {
                                return -1;
                            } else if (p2.getModifiedDate() > p1.getModifiedDate()) {
                                return 1;
                            }
                            return 0;

                            //return (int) (p2.getModifiedDate() - p1.getModifiedDate());
                        }

                        @SuppressWarnings("SimplifiableIfStatement")
                        @Override
                        public boolean areContentsTheSame(FeedBean oldPost,
                                                          FeedBean newPost) {
                            if (oldPost.getPId() != newPost.getPId()) {
                                return false;
                            }

                            if (!oldPost.getPContent().equals(newPost.getPContent())) {
                                return false;
                            }

                            return oldPost.isPending() == newPost.isPending();
                        }

                        @Override
                        public boolean areItemsTheSame(FeedBean item1, FeedBean item2) {
                            return item1.getPId() == item2.getPId();
                        }
                    });
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvUserName, tvTime, tvNumComment, tvTotalHugs, tvLocation;
            public ImageView ivUserPhoto, ivPostImage, img_hug, img_comment, iv_arrow, iv_posting;
            public TextView tvDescription;

            public MyViewHolder(View view) {
                super(view);
                tvUserName = (TextView) view.findViewById(R.id.tv_userName);
                tvTime = (TextView) view.findViewById(R.id.tv_time);
                tvNumComment = (TextView) view.findViewById(R.id.tv_numComment);
                tvTotalHugs = (TextView) view.findViewById(R.id.tv_totalHugs);
                tvLocation = (TextView) view.findViewById(R.id.tvLocation);
                ivUserPhoto = (ImageView) view.findViewById(R.id.userPhoto);
                ivPostImage = (ImageView) view.findViewById(R.id.iv_post_image);
                tvDescription = (TextView) view.findViewById(R.id.tv_description);
                img_hug = (ImageView) view.findViewById(R.id.img_hug);
                img_comment = (ImageView) view.findViewById(R.id.img_comment);
                iv_arrow = (ImageView) view.findViewById(R.id.iv_arrow);
                iv_posting = (ImageView) view.findViewById(R.id.postingFlag);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_row_profile_feeds, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            FeedBean feedBean = mList.get(position);
            holder.ivPostImage.setVisibility(View.GONE);
            holder.tvTotalHugs.setText(feedBean.getVotes() + "");

            if (feedBean.getUserLike() == 1)
                holder.img_hug.setSelected(true);
            else
                holder.img_hug.setSelected(false);

            if (feedBean.isPending()) {
                holder.iv_arrow.setVisibility(View.GONE);
                holder.iv_posting.setVisibility(View.VISIBLE);
            } else {
                holder.iv_posting.setVisibility(View.GONE);
                if (Integer.parseInt(feedBean.getUId()) == manager.getUser().getUserId()) {
                    holder.iv_arrow.setVisibility(View.VISIBLE);
                } else {
                    holder.iv_arrow.setVisibility(View.GONE);
                }
            }

            if (!TextUtils.isEmpty(feedBean.getImage())) {
                holder.ivPostImage.setTag(feedBean);
                holder.ivPostImage.setVisibility(View.VISIBLE);
                mPicasso.load(feedBean.getImage()).error(R.drawable.trasperent_white).placeholder(R.drawable.text_box_with_border).into(holder.ivPostImage);
                holder.ivPostImage.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            FeedBean mFeedBean = (FeedBean) v.getTag();

                            if (!TextUtils.isEmpty(mFeedBean.getImage())) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                Uri imgUri = Uri.parse(mFeedBean.getImage());
                                intent.setDataAndType(imgUri, "image/*");
                                context.startActivity(intent);
                            }
                        } catch (Exception e) {

                        }
                    }
                });
            }

            holder.tvTotalHugs.setText(feedBean.getVotes() + "");

            holder.img_hug.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (feedBean.getUserLike() != 1) {
                        ((ImageView) v).setSelected(true);
                        feedBean.setUserLike(1);
                        feedBean.setVotes(feedBean.getVotes() + 1);
                        holder.tvTotalHugs.setText(feedBean.getVotes() + "");

                        feedController.sendLike(feedBean);
                    }
                }
            });

            holder.img_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent in = new Intent(ActivitySearchFeedHashTag.this, ActivityComment.class);
                    in.putExtra(Constants.KEY_POST_ID, Integer.parseInt(feedBean.getPId()));
                    in.putExtra(Constants.KEY_USER_ID, Integer.parseInt(feedBean.getUId()));
                    in.putExtra(com.cgt.socialnetwork.common.Constants.KEY_DATA, feedBean);
                    startActivity(in);
                }
            });

            holder.iv_arrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFeedOptions(v, feedBean);
                }
            });

            holder.ivUserPhoto.setImageResource(R.drawable.avatar_);
            if (!TextUtils.isEmpty(feedBean.getUPhoto())) {
                mPicasso.load(feedBean.getUPhoto()).transform(new CircleTransform(ActivitySearchFeedHashTag.this))
                        .placeholder(R.drawable.avatar_)
                        .into(holder.ivUserPhoto);

                holder.ivUserPhoto.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent in = new Intent(ActivitySearchFeedHashTag.this, ActivityUserProfile.class);
                        in.putExtra(Constants.KEY_USER_ID, Integer.parseInt(feedBean.getUId()));
                        in.putExtra(Constants.KEY_USER_NAME, feedBean.getUName());
                        in.putExtra(Constants.KEY_PHOTO, feedBean.getUPhoto());
                        in.putExtra(Constants.KEY_LOCATION, feedBean.getCity());
                        startActivity(in);
                    }
                });
            }

            holder.tvUserName.setText(feedBean.getUName());
            if (!TextUtils.isEmpty(feedBean.getCountry())) {
                holder.tvLocation.setVisibility(View.VISIBLE);
                holder.tvLocation.setText(feedBean.getCountry());
            } else {
                holder.tvLocation.setVisibility(View.GONE);
            }

            holder.tvTime.setText(Utils.getSinceOrDate(System.currentTimeMillis(), feedBean.getModifiedDate()));

            if (TextUtils.isEmpty(feedBean.getPContent())) {
                holder.tvDescription.setVisibility(View.GONE);
            } else {
                holder.tvDescription.setVisibility(View.VISIBLE);
            }

            holder.tvDescription.setText(StringEscapeUtils.unescapeJava(feedBean.getPContent()));
            holder.tvDescription.setTag(feedBean);
            holder.tvDescription.setMaxLines(4);

            holder.tvDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.tvDescription.setMaxLines(15);
                }
            });

            holder.tvNumComment.setText(feedBean.getComments() + "");
            holder.tvTotalHugs.setText(feedBean.getVotes() + "");
        }

        public void clear() {
            mList.clear();
            mUniqueMapping.clear();
        }

        public void insert(FeedBean item) {
            FeedBean existing = mUniqueMapping.put(item.getKey(), item);
            if (existing == null) {
                mList.add(item);
            } else {
                int pos = mList.indexOf(existing);
                mList.updateItemAt(pos, item);
            }
        }

        public void updatePost(FeedBean post) {
            FeedBean existing = mUniqueMapping.get(post.getKey());
            if (existing == null) {
                return;
            }

            int pos = mList.indexOf(existing);
            mUniqueMapping.put(post.getKey(), post);
            mList.updateItemAt(pos, post);
        }

        public void deletePost(String key) {
            FeedBean existing = mUniqueMapping.get(key);
            if (existing == null) {
                return;
            }

            int pos = mList.indexOf(existing);
            mUniqueMapping.remove(key);
            mList.removeItemAt(pos);
        }

        public void insertAll(List<FeedBean> items) {
            for (FeedBean item : items) {
                insert(item);
            }
        }

        public void swapList(List<FeedBean> items) {
            Set<String> newListKeys = new HashSet<>();
            for (FeedBean item : items) {
                newListKeys.add(item.getKey());
            }

            for (int i = mList.size() - 1; i >= 0; i--) {
                FeedBean item = mList.get(i);
                if (!newListKeys.contains(item.getKey())) {
                    mUniqueMapping.remove(item.getKey());
                    mList.removeItemAt(i);
                }
            }
            insertAll(items);
        }

        public long getReferenceTimestamp() {
            int size = mList.size();
            if (size == 0) {
                return 0;
            }

            return mList.get(0).getModifiedDate();
        }

        public long getLastRecordTimeStamp() {
            if (mList.size() == 0) {
                return 0;
            }

            return mList.get(mList.size() - 1).getModifiedDate();
        }

        public void removePost(FeedBean post) {
            FeedBean model = mUniqueMapping.remove(post.getKey());
            if (model != null) {
                mList.remove(model);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        private void showFeedOptions(View view, FeedBean mFeedBean) {
            PopupMenu menu = new PopupMenu(ActivitySearchFeedHashTag.this, view);
            menu.inflate(R.menu.menu_feed_options);

            Menu m = menu.getMenu();
            Intent i = new Intent();
            i.putExtra(com.cgt.socialnetwork.common.Constants.KEY_DATA, mFeedBean);
            m.findItem(R.id.actionDeletePost).setIntent(i);
            m.findItem(R.id.actionEditPost).setIntent(i);

            menu.setOnMenuItemClickListener(onFeedItemClickListener);
            menu.setGravity(Gravity.TOP | Gravity.RIGHT);
            menu.show();
        }

        PopupMenu.OnMenuItemClickListener onFeedItemClickListener = new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                FeedBean feedBean = (FeedBean) item.getIntent().getSerializableExtra(com.cgt.socialnetwork.common.Constants.KEY_DATA);
                switch (item.getItemId()) {
                    case R.id.actionDeletePost:
                        AlertDialogFactory.alertBox(ActivitySearchFeedHashTag.this, "", "Are you sure you want to delete this post?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deletePostApi(feedBean.getPId(), feedBean.getKey());
                                    }
                                },
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }, false);
                        break;
                    case R.id.actionEditPost:
                        Intent in = new Intent(ActivitySearchFeedHashTag.this, ActivityEditPost.class);
                        in.putExtra(com.cgt.socialnetwork.common.Constants.KEY_DATA, feedBean);
                        in.putExtra(com.cgt.socialnetwork.common.Constants.KEY_SCREEN, com.cgt.socialnetwork.common.Constants.KEY_SCREEN_SEARCH_FEED);
                        startActivity(in);
                        break;
                    default:
                        break;
                }
                return true;
            }
        };

        private void deletePostApi(String postId, String key) {
            showLoader();
            StringRequest request = new StringRequest(Request.Method.POST, Constants.DELETE_POST, new Response.Listener<String>() {

                @Override
                public void onResponse(String s) {
                    hideLoader();
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        switch (jsonObject.getInt(com.cgt.socialnetwork.common.Constants.KEY_CODE)) {
                            case com.cgt.socialnetwork.common.Constants.SUCCESS:
                                DBHelper.getInstance(ActivitySearchFeedHashTag.this).deleteFeedPost(postId);
                                deletePost(key);
                                break;
                            case com.cgt.socialnetwork.common.Constants.TOKEN_EXPIRED:
                            case com.cgt.socialnetwork.common.Constants.BLANK_TOKEN:
                                //logout();
                                break;
                            default:
                                if (jsonObject.has(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE) && !jsonObject.isNull(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE)) {
                                    showToast(jsonObject.getString(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE));
                                }
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideLoader();
                    showToast(Util.getErrorMsg(error, ActivitySearchFeedHashTag.this));
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(com.cgt.socialnetwork.common.Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(ActivitySearchFeedHashTag.this).getUser().getToken());
                    return params;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    RequestBuilder requestBuilder = AppModuleManager.getInstance(ActivitySearchFeedHashTag.this).getRequestBuilder();
                    Map<String, String> params = requestBuilder.deletePost(postId);
                    return params;
                }
            };

            MyVolleyHelper.getIntance(ActivitySearchFeedHashTag.this).addRequestToQueue(request);
        }
    }

    @Subscribe
    public void onEvent(EventFetchedHashTagFeed event) {
        isLoading = false;
        if (event.isSuccess()) {
            refresh(event);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideLoader();
                }
            });
        }
    }

    @Subscribe
    public void onEvent(EventAddVote event) {
        if (event.isSuccess()) {

        }
    }

    @Subscribe
    public void onEvent(EditPostEvent event) {
        if (event.isSuccess()) {
            recycle_post_list.post(new Runnable() {
                @Override
                public void run() {
                    mFeedAdapter.updatePost(event.getPost());
                    mFeedAdapter.notifyDataSetChanged();
                    recycle_post_list.smoothScrollToPosition(0);
                }
            });
        }
    }

    private void refresh(final EventFetchedHashTagFeed event) {
        final boolean swapList = mRefreshFull;
        mRefreshFull = false;

        new AutoCancelAsyncTask<Void, List<FeedBean>>(this) {

            @Override
            protected void onResult(List<FeedBean> feedItems) {
                if (event != null) {
                    if (event.isReloadRequest()) {

                    } else if (event.isLoadMoreRequest()) {
                        recycle_post_list.scrollToPosition(totalItemCount - 1);
                    }
                }

                if (swapList) {
                    mFeedAdapter.swapList(feedItems);
                } else {
                    mFeedAdapter.insertAll(feedItems);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoader();
                    }
                });
            }

            @Override
            protected List<FeedBean> onDoInBackground(Void... params) {
                List<FeedBean> feeds = new ArrayList<FeedBean>();
                if (swapList) {
                    feeds = DBHelper.getInstance(ActivitySearchFeedHashTag.this).getHashTagFeedsSince(0);
                } else if (event.isReloadRequest()) {
                    feeds = DBHelper.getInstance(ActivitySearchFeedHashTag.this).getHashTagFeedsSince(mFeedAdapter.getReferenceTimestamp());
                } else if (event.isLoadMoreRequest()) {
                    feeds = DBHelper.getInstance(ActivitySearchFeedHashTag.this).getHashTagFeedsUntil(mFeedAdapter.getLastRecordTimeStamp());
                }

                return feeds;
            }
        }.execute();
    }

}