package com.cgt.socialnetwork.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.SortedList;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
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
import com.cgt.socialnetwork.common.DateTimeUtil;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.controller.FeedController;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.event.EventDeleteComment;
import com.cgt.socialnetwork.event.EventFetchedComment;
import com.cgt.socialnetwork.event.EventNewComment;
import com.cgt.socialnetwork.model.Comment;
import com.cgt.socialnetwork.model.FeedBean;
import com.cgt.socialnetwork.model.User;
import com.cgt.socialnetwork.task.AutoCancelAsyncTask;
import com.cgt.socialnetwork.ui.ActivityUserProfile;
import com.cgt.socialnetwork.utils.AlertDialogFactory;
import com.cgt.socialnetwork.utils.CircleTransform;
import com.cgt.socialnetwork.utils.Utils;
import com.cgt.socialnetwork.common.MyVolleyHelper;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringEscapeUtils;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by CGTechnosoft
 */
public class FragmentComments extends BaseFragment implements OnScrollListener {
    @Bind(R.id.recycle_post_list)
    RecyclerView recycle_post_list;
    @Bind(R.id.tv_empty)
    TextView tv_empty;

    //@Bind(R.id.iv_user_dp)
    //ImageView iv_user_dp;
    @Bind(R.id.et_message)
    EditText et_message;
    @Bind(R.id.iv_send)
    ImageView iv_send;

    private AppModuleManager manager;
    private User mUser;

    protected FeedAdapter mFeedAdapter;
    protected boolean isLoading = false;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Lock to avoid creating multiple refresh jobs
    private boolean mPendingRefresh = false;

    // Set when Activity starts. Since activity does not listen for events after being stopped, we
    // need to do a full sync on return. Event cycle can be moved between onCreate/onDestroy to
    // avoid this but that will require additional complexity of checking when to update the views.
    private boolean mRefreshFull;

    private int postId, postOwnerId;
    private FeedController feedController = null;
    private FeedBean feedBean = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        postId = bundle.getInt(com.cgt.socialnetwork.common.Constants.KEY_POST_ID, -1);
        postOwnerId = bundle.getInt(com.cgt.socialnetwork.common.Constants.KEY_USER_ID, -1);
        feedBean = (FeedBean) bundle.getSerializable(com.cgt.socialnetwork.common.Constants.KEY_DATA);

        boolean hasPendingComments = DBHelper.getInstance(getActivity()).hasPendingComments("" + postId);
        if (!hasPendingComments) {
            Preference.getInstance(getActivity()).clearCommentsPreferences();
            DBHelper.getInstance(getActivity()).clearCommentsData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_comment, container, false);
        ButterKnife.bind(this, mView);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        manager = AppModuleManager.getInstance(getActivity());
        mUser = manager.getUser();
        mPicasso = manager.getPicasso();
        feedController = manager.getFeedController();

        mFeedAdapter = new FeedAdapter(getActivity());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recycle_post_list.setLayoutManager(mLayoutManager);
        recycle_post_list.setItemAnimator(new DefaultItemAnimator());
        recycle_post_list.setAdapter(mFeedAdapter);

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
                            feedController.loadMoreCommentsAsync(postId, true);
                        }
                    }
                }
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                mPendingRefresh = true;
                feedController.fetchCommentsAsync(postId, true);
            }
        });
    }

    @OnClick(R.id.iv_send)
    void onSendClick() {
        if (!TextUtils.isEmpty(et_message.getText().toString().trim())) {
            Comment comment = new Comment();
            double latitude = 0, longitude = 0;
            long currentTime = System.currentTimeMillis();
            int commentId = (int) (currentTime / 1000);
            comment.setMessage(et_message.getText().toString().trim());
            comment.setPostId(postId);
            comment.setUserId(mUser.getUserId());
            comment.setUserName(mUser.getUserName());
            comment.setUserPhoto(mUser.getUserPhoto());
            comment.setCreatedDate(currentTime);
            comment.setPending(1);
            comment.setClientId(UUID.randomUUID().toString());

            if (!TextUtils.isEmpty(Preference.getInstance(getActivity()).getString(com.cgt.socialnetwork.common.Constants.KEY_CURRENT_LAT)))
                latitude = Double.parseDouble(Preference.getInstance(getActivity()).getString(com.cgt.socialnetwork.common.Constants.KEY_CURRENT_LAT));
            if (!TextUtils.isEmpty(Preference.getInstance(getActivity()).getString(com.cgt.socialnetwork.common.Constants.KEY_CURRENT_LONG)))
                longitude = Double.parseDouble(Preference.getInstance(getActivity()).getString(com.cgt.socialnetwork.common.Constants.KEY_CURRENT_LONG));

            comment.setLatitude(latitude);
            comment.setLongitude(longitude);

            et_message.setText("");
            mFeedAdapter.insert(comment);

            // set this later due to maintaining key
            comment.setId(commentId);

            // insert into comment table with status posting
            DBHelper.getInstance(getActivity()).addComment(comment);

            feedController.sendCommentAsync(comment);
            recycle_post_list.smoothScrollToPosition(0);
            hideKeyboard();
        }
    }

    //Bus Register and unRegister
    @Override
    public void onStart() {
        super.onStart();
        eventBus.register(this);
        mRefreshFull = true;
        refresh(null);
        callWs();
    }

    @Override
    public void onStop() {
        super.onStop();
        eventBus.unregister(this);
        mRefreshFull = false;
    }

    @Override
    public void showLoader() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideLoader() {
        getView().post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void callWs() {
        showLoader();
        feedController.fetchCommentsAsync(postId, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.MyViewHolder> {
        private Picasso mPicasso;
        private Context context;

        SortedList<Comment> mList;

        final Map<String, Comment> mUniqueMapping = new HashMap<>();

        public FeedAdapter(Context mContext) {
            super();
            this.context = mContext;
            mPicasso = AppModuleManager.getInstance(mContext).getPicasso();
            mList = new SortedList<>(Comment.class,
                    new SortedListAdapterCallback<Comment>(this) {
                        @Override
                        public int compare(Comment p1, Comment p2) {
                            if (p1.isPending() != p2.isPending()) {
                                return p1.isPending() ? -1 : 1;
                            }

                            return (int) (p2.getCreatedDate() - p1.getCreatedDate());
                        }

                        @SuppressWarnings("SimplifiableIfStatement")
                        @Override
                        public boolean areContentsTheSame(Comment oldPost,
                                                          Comment newPost) {
                            if (oldPost.getId() != newPost.getId()) {
                                return false;
                            }

                            if (!oldPost.getMessage().equals(newPost.getMessage())) {
                                return false;
                            }

                            return oldPost.isPending() == newPost.isPending();
                        }

                        @Override
                        public boolean areItemsTheSame(Comment item1, Comment item2) {
                            return item1.getId() == item2.getId();
                        }
                    });
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvUserName, tvTime;
            public ImageView ivUserPhoto, iv_delete, iv_posting;
            public TextView tvDescription;

            public MyViewHolder(View view) {
                super(view);
                ivUserPhoto = (ImageView) view.findViewById(R.id.userPhoto);
                tvUserName = (TextView) view.findViewById(R.id.tv_userName);
                tvTime = (TextView) view.findViewById(R.id.tv_time);
                tvDescription = (TextView) view.findViewById(R.id.tv_description);
                iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
                iv_posting = (ImageView) view.findViewById(R.id.postingFlag);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_row_comments, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Comment comment = mList.get(position);

            if (postOwnerId == mUser.getUserId() || comment.getUserId() == mUser.getUserId()) {
                if (comment.isPending()) {
                    holder.iv_posting.setVisibility(View.VISIBLE);
                    holder.iv_delete.setVisibility(View.GONE);
                } else {
                    holder.iv_posting.setVisibility(View.GONE);
                    holder.iv_delete.setVisibility(View.VISIBLE);
                }
            } else {
                holder.iv_posting.setVisibility(View.GONE);
                holder.iv_delete.setVisibility(View.GONE);
            }

            holder.tvDescription.setText(StringEscapeUtils.unescapeJava(comment.getMessage()));
            holder.tvDescription.setTag(comment);
            holder.tvDescription.setMaxLines(4);

            holder.tvTime.setText(Utils.getSinceOrDate(System.currentTimeMillis(), comment.getCreatedDate()));
            holder.ivUserPhoto.setImageResource(R.drawable.avatar_);

            if (!TextUtils.isEmpty(comment.getUserPhoto())) {
                mPicasso.load(comment.getUserPhoto()).transform(new CircleTransform(getActivity())).placeholder(R.drawable.avatar_).into(holder.ivUserPhoto);
            }

            holder.tvUserName.setText(comment.getUserName());
            holder.ivUserPhoto.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent in = new Intent(getActivity(), ActivityUserProfile.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    in.putExtra(com.cgt.socialnetwork.common.Constants.KEY_USER_ID, comment.getUserId());
                    in.putExtra(com.cgt.socialnetwork.common.Constants.KEY_USER_NAME, comment.getUserName());
                    in.putExtra(com.cgt.socialnetwork.common.Constants.KEY_PHOTO, comment.getUserPhoto());
                    startActivity(in);
                }
            });

            holder.tvDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.tvDescription.setMaxLines(15);
                    //holder.tvDescription.setText(comment.getMessage());
                }
            });

            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialogFactory.alertBox(getActivity(), "", "Are you sure you want to delete this comment?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteCommentApi(comment);
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }, false);
                }
            });
        }

        public void clear() {
            mList.clear();
        }

        public void insert(Comment item) {
            Comment existing = mUniqueMapping.put(item.getKey(), item);
            if (existing == null) {
                mList.add(item);
            } else {
                int pos = mList.indexOf(existing);
                mList.updateItemAt(pos, item);
            }
        }

        public void updatePost(Comment comment) {
            Comment existing = mUniqueMapping.get(comment.getKey());
            if (existing == null) {
                return;
            }

            int pos = mList.indexOf(existing);
            mUniqueMapping.put(comment.getKey(), comment);
            mList.updateItemAt(pos, comment);
        }

        public void deleteComment(Comment comment) {
            Comment existing = mUniqueMapping.get(comment.getKey());
            if (existing == null) {
                return;
            }

            int pos = mList.indexOf(existing);
            mUniqueMapping.remove(comment.getKey());
            mList.removeItemAt(pos);
        }

        public void insertAll(List<Comment> items) {
            for (Comment item : items) {
                insert(item);
            }
        }

        public void swapList(List<Comment> items) {
            Set<String> newListKeys = new HashSet<>();
            for (Comment item : items) {
                newListKeys.add(item.getKey());
            }

            for (int i = mList.size() - 1; i >= 0; i--) {
                Comment item = mList.get(i);
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

            return mList.get(0).getCreatedDate();
        }

        public long getLastRecordTimeStamp() {
            if (mList.size() == 0) {
                return 0;
            }

            return mList.get(mList.size() - 1).getCreatedDate();
        }

        public void removePost(Comment post) {
            Comment model = mUniqueMapping.remove(post.getKey());
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

        private void deleteCommentApi(Comment comment) {
            showLoader();
            StringRequest request = new StringRequest(Request.Method.POST, Constants.DELETE_COMMENT, new Response.Listener<String>() {

                @Override
                public void onResponse(String s) {
                    hideLoader();
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        switch (jsonObject.getInt(com.cgt.socialnetwork.common.Constants.KEY_CODE)) {
                            case com.cgt.socialnetwork.common.Constants.SUCCESS:
                                DBHelper.getInstance(getActivity()).deleteComment(comment.getId());
                                deleteComment(comment);
                                mFeedAdapter.notifyDataSetChanged();
                                break;
                            case com.cgt.socialnetwork.common.Constants.INVALID_REQUEST:
                                break;
                            case com.cgt.socialnetwork.common.Constants.TOKEN_EXPIRED:
                            case com.cgt.socialnetwork.common.Constants.BLANK_TOKEN:
                                logout();
                                break;
                            case com.cgt.socialnetwork.common.Constants.DATA_NOT_FOUND:
                                break;
                            default:
                                if (jsonObject.has(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE) && !jsonObject.isNull(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE)) {
                                    showPrompt(jsonObject.getString(com.cgt.socialnetwork.common.Constants.KEY_MESSAGE));
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
                    showPrompt(Util.getErrorMsg(error, getActivity()));
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(com.cgt.socialnetwork.common.Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(getActivity()).getUser().getToken());
                    return params;
                }

                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    AppModuleManager manager = AppModuleManager.getInstance(getActivity());
                    return manager.getRequestBuilder().deleteComment(comment.getId());
                }
            };

            MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
        }
    }

    @Subscribe
    public void onEvent(EventFetchedComment event) {
        isLoading = false;
        if (event.isSuccess()) {
            refresh(event);
        } else {
            hideLoader();
        }
    }

    @Subscribe
    public void onEvent(EventNewComment event) {
        if (event.isSuccess()) {
            recycle_post_list.post(new Runnable() {
                @Override
                public void run() {
                    mFeedAdapter.updatePost(event.getComment());
                    mFeedAdapter.notifyDataSetChanged();
                    recycle_post_list.smoothScrollToPosition(0);
                }
            });
        }
    }

    @Subscribe
    public void onEvent(EventDeleteComment event) {
        if (event.isSuccess()) {
            recycle_post_list.post(new Runnable() {
                @Override
                public void run() {
                    //DBHelper.getInstance(getActivity()).deleteComment(event.getComment().getId());
                    mFeedAdapter.deleteComment(event.getComment());
                    mFeedAdapter.notifyDataSetChanged();
                    showPrompt("unable to send message.");
                }
            });
        }
    }

    private void refresh(final EventFetchedComment event) {
        final boolean swapList = mRefreshFull;
        mRefreshFull = false;
        new AutoCancelAsyncTask<Void, List<Comment>>(this) {

            @Override
            protected void onResult(List<Comment> feedItems) {
                //L.d("feed model returned with %s items", feedItems.size());
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

                hideLoader();
            }

            @Override
            protected List<Comment> onDoInBackground(Void... params) {
                //L.d("time to query feed model");
                List<Comment> lstComment = new ArrayList<Comment>();
                if (swapList) {
                    lstComment = DBHelper.getInstance(getActivity()).getCommentsSince(postId, 0);

                    if (lstComment.size() > 0) {
                        String sinceDate = DateTimeUtil.getDateFromTimeStamp(lstComment.get(0).getCreatedDate(), DateTimeUtil.DATE_FORMAT_SERVER);
                        String utilDate = DateTimeUtil.getDateFromTimeStamp(lstComment.get(lstComment.size() - 1).getCreatedDate(), DateTimeUtil.DATE_FORMAT_SERVER);

                        Preference.getInstance(getActivity()).put(com.cgt.socialnetwork.common.Constants.PREF_KEY_COMMENT_SINCE_DATE, sinceDate);
                        Preference.getInstance(getActivity()).put(com.cgt.socialnetwork.common.Constants.PREF_KEY_COMMENT_UNTIL_DATE, utilDate);
                    }
                } else if (event.isReloadRequest()) {
                    lstComment = DBHelper.getInstance(getActivity()).getCommentsSince(postId, mFeedAdapter.getReferenceTimestamp());
                } else if (event.isLoadMoreRequest()) {
                    lstComment = DBHelper.getInstance(getActivity()).getCommentsUntil(postId, mFeedAdapter.getLastRecordTimeStamp());
                }

                return lstComment;
            }
        }.execute();
    }

}
