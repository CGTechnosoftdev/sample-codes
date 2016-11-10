package com.cgt.socialnetwork.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cgt.socialnetwork.R;
import com.cgt.socialnetwork.common.AppModuleManager;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.Preference;
import com.cgt.socialnetwork.common.Util;
import com.cgt.socialnetwork.controller.FeedController;
import com.cgt.socialnetwork.db.DBHelper;
import com.cgt.socialnetwork.db.DataProviderContract;
import com.cgt.socialnetwork.ui.ActivityPostDetail;
import com.cgt.socialnetwork.ui.ActivityUserProfile;
import com.cgt.socialnetwork.utils.CircleTransform;
import com.cgt.socialnetwork.utils.NetworkUtil;
import com.cgt.socialnetwork.utils.Utils;
import com.cgt.socialnetwork.common.MyVolleyHelper;
import com.cgt.socialnetwork.widget.SwipeRevealLayout;
import com.cgt.socialnetwork.widget.ViewBinderHelper;
import com.squareup.picasso.RequestCreator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CGTechnosoft
 */
public class FragmentNotification extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView mListView;
    private NotificationAdapter notificationAdapter = null;
    private SwipeRefreshLayout swipeRefreshLayout = null;
    private FeedController feedController = null;
    private INotificationUpdate notificationUpdate = null;

    public FragmentNotification() {
        // Required empty public constructor
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        super.onAttach(context);
        onAttachToContext(context);
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            onAttachToContext(activity);
        }
    }

    /*
     * This method will be called from one of the two previous method
     */
    protected void onAttachToContext(Context context) {
        notificationUpdate = (INotificationUpdate) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppModuleManager manager = AppModuleManager.getInstance(getActivity());
        feedController = manager.getFeedController();

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
            }
        });

        mListView = (ListView) getView().findViewById(R.id.listNotification);
        mListView.setEmptyView(getView().findViewById(android.R.id.empty));

        notificationAdapter = new NotificationAdapter(getActivity());
        mListView.setAdapter(notificationAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(Constants.LOADER_NOTIFICATION, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),                                     // Context
                DataProviderContract.Notification.CONTENT_URI,       // Table to query
                DataProviderContract.Notification.PROJECTION,              // Projection to return
                null,                                              // No selection clause
                null,                                              // No selection arguments
                DataProviderContract.Notification.TIMESTAMP + " DESC"                                              // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            notificationAdapter.changeCursor(data);

            // Reload notification when new push arrives
            boolean reload = Preference.getInstance(getActivity()).getBoolean(Constants.PREF_KEY_RELOAD_NOTIFICATIONS);
            if (reload) {
                showLoader();
                fetchData();
                Preference.getInstance(getActivity()).put(Constants.PREF_KEY_RELOAD_NOTIFICATIONS, false);
            }
        } else {
            showLoader();
            fetchData();
        }

        if (notificationUpdate != null)
            notificationUpdate.updateNotificationCount();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        notificationAdapter.changeCursor(null);
    }

    private void fetchData() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Constants.GET_NOTIFICATIONS, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                hideLoader();
                try {
                    int code = response.getInt(Constants.KEY_CODE);
                    switch (code) {
                        case Constants.SUCCESS:
                            JSONArray data = response.getJSONArray(Constants.KEY_DATA);
                            if (data.length() > 0) {
                                DBHelper.getInstance(getActivity()).clearNotificationData();
                                DBHelper.getInstance(getActivity()).saveNotificationData(data);
                            }
                            break;
                        case Constants.TOKEN_EXPIRED:
                        case Constants.BLANK_TOKEN:
                            logout();
                            break;
                        case Constants.DATA_NOT_FOUND:
                            //DBHelper.getInstance(getActivity()).clearNotificationData();
                            break;
                        default:
                            if (response.has(Constants.KEY_MESSAGE) && !response.isNull(Constants.KEY_MESSAGE)) {
                                if (isVisible())
                                    showPrompt(response.getString(Constants.KEY_MESSAGE));
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isVisible()) {
                    hideLoader();
                    showPrompt(Util.getErrorMsg(error, getActivity()));
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.KEY_HEADER_TOKEN, AppModuleManager.getInstance(getActivity()).getUser().getToken());
                return params;
            }
        };

        MyVolleyHelper.getIntance(getActivity()).addRequestToQueue(request);
    }

    private class NotificationAdapter extends CursorAdapter {
        private LayoutInflater mInflater;
        private final ViewBinderHelper binderHelper;

        public NotificationAdapter(Context context) {
            super(context, null, false);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            binderHelper = new ViewBinderHelper();
            binderHelper.setOpenOnlyOne(true);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.fragment_notfication_item, parent, false);
                holder = new ViewHolder();

                holder.rl_row = (RelativeLayout) view.findViewById(R.id.rl_row);
                holder.tvUserName = (TextView) view.findViewById(R.id.tv_UserName);
                holder.tvUserDetail = (TextView) view.findViewById(R.id.tv_UserDetail);
                holder.tvUserTime = (TextView) view.findViewById(R.id.tv_UserTime);
                holder.deleteView = (View) view.findViewById(R.id.delete_layout);
                holder.swipeLayout = (SwipeRevealLayout) view.findViewById(R.id.swipe_layout);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            Cursor cursor = (Cursor) notificationAdapter.getItem(position);

            String userName = cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.USER_NAME));
            holder.tvUserName.setText(userName);
            String path = cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.USER_PHOTO));
            ((ImageView) view.findViewById(R.id.img_userImage)).setImageResource(R.drawable.avatar_);
            if (!TextUtils.isEmpty(path)) {
                RequestCreator mRequestCreator = mPicasso.load(path).placeholder(R.drawable.avatar_).error(R.drawable.avatar_).
                        transform(new CircleTransform(getActivity()));
                mRequestCreator.into((ImageView) view.findViewById(R.id.img_userImage));
            }

            holder.tvUserDetail.setText(cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.CONTENT)));
            long timestamp = cursor.getLong(cursor.getColumnIndex(DataProviderContract.Notification.TIMESTAMP));
            holder.tvUserTime.setText(Utils.getSinceOrDate(System.currentTimeMillis(), timestamp == 0 ? System.currentTimeMillis() : timestamp));

            if (cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.IS_READ)) == 1) { // read
                holder.rl_row.setBackgroundColor(getResources().getColor(R.color.white));
            } else { // unread
                holder.rl_row.setBackgroundColor(getResources().getColor(R.color.gray));
            }

            holder.deleteView.setTag(position);
            binderHelper.bind(holder.swipeLayout, cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification._ID)));
            holder.deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = Integer.parseInt(v.getTag().toString());
                    Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
                    anim.setDuration(400);
                    holder.swipeLayout.startAnimation(anim);

                    new Handler().postDelayed(new Runnable() {

                        public void run() {
                            Cursor cursor = (Cursor) notificationAdapter.getItem(pos);
                            DBHelper.getInstance(getActivity()).deleteNotificationById("" + cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.NOTIFICATION_ID)));

                            // start new job to read that notification
                            feedController.readNotification(cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.NOTIFICATION_ID)));
                        }

                    }, 300);
                }
            });

            holder.rl_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int conn = NetworkUtil.getConnectivityStatus(getActivity());
                    if (conn == NetworkUtil.TYPE_CONNECTED) {
                        Cursor cursor = (Cursor) notificationAdapter.getItem(position);

                        // start new job to read that notification
                        feedController.readNotification(cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.NOTIFICATION_ID)));

                        int action = cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.ACTION));
                        switch (action) {
                            case Constants.TAG_CREATED:
                            case Constants.TAG_LIKED:
                            case Constants.TAG_REPLY:
                                Intent intent = new Intent(getActivity(), ActivityPostDetail.class);
                                intent.putExtra(Constants.KEY_POST_ID, cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.POST_ID)));
                                startActivity(intent);
                                break;
                            case Constants.TAG_ADDED:
                                Intent in = new Intent(getActivity(), ActivityUserProfile.class);
                                in.putExtra(Constants.KEY_USER_ID, cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.USER_ID)));
                                in.putExtra(Constants.KEY_USER_NAME, cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.USER_NAME)));
                                in.putExtra(Constants.KEY_PHOTO, cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.USER_PHOTO)));
                                startActivity(in);
                                break;
                        }
                    } else {
                        Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_LONG).show();
                    }
                }
            });

            return view;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.fragment_notfication_item, null, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            /*ViewHolder holder = new ViewHolder();
            holder.rl_row = (RelativeLayout) view.findViewById(R.id.rl_row);
            holder.tvUserName = (TextView) view.findViewById(R.id.tv_UserName);
            holder.tvUserDetail = (TextView) view.findViewById(R.id.tv_UserDetail);
            holder.tvUserTime = (TextView) view.findViewById(R.id.tv_UserTime);
            holder.deleteView = (View) view.findViewById(R.id.delete_layout);
            holder.swipeLayout = (SwipeRevealLayout) view.findViewById(R.id.swipe_layout);

            int isPrivate = cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.IS_PRIVATE));
            if (isPrivate == 1) {
                ((ImageView) view.findViewById(R.id.img_userImage)).setImageResource(R.drawable.anonymos);
                holder.tvUserName.setText("Anonymous");
            } else {
                holder.tvUserName.setText(cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.USER_NAME)));
                String path = cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.USER_PHOTO));
                ((ImageView) view.findViewById(R.id.img_userImage)).setImageResource(R.drawable.avatar_);
                if (!TextUtils.isEmpty(path)) {
                    RequestCreator mRequestCreator = mPicasso.load(path).placeholder(R.drawable.avatar_).error(R.drawable.avatar_).
                            transform(new CircleTransform(context));
                    mRequestCreator.into((ImageView) view.findViewById(R.id.img_userImage));
                }
            }

            binderHelper.bind(holder.swipeLayout, cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification._ID)));

            holder.tvUserDetail.setText(cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.CONTENT)));
            long timestamp = cursor.getLong(cursor.getColumnIndex(DataProviderContract.Notification.TIMESTAMP));
            holder.tvUserTime.setText(Utils.getSinceOrDate(System.currentTimeMillis(), timestamp == 0 ? System.currentTimeMillis() : timestamp));
            if (cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.IS_READ)) == 1) { // read
                holder.rl_row.setBackgroundColor(getResources().getColor(R.color.white));
            } else { // unread
                holder.rl_row.setBackgroundColor(getResources().getColor(R.color.gray));
            }

            holder.deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //remove(item);
                }
            });*/
        }

        private class ViewHolder {
            TextView tvUserName, tvUserDetail, tvUserTime;
            RelativeLayout rl_row;
            View deleteView;
            SwipeRevealLayout swipeLayout;
        }


        /**
         * Only if you need to restore open/close state when the orientation is changed.
         * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
         */
        public void saveStates(Bundle outState) {
            binderHelper.saveStates(outState);
        }

        /**
         * Only if you need to restore open/close state when the orientation is changed.
         * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
         */
        public void restoreStates(Bundle inState) {
            binderHelper.restoreStates(inState);
        }
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int conn = NetworkUtil.getConnectivityStatus(getActivity());
            if (conn == NetworkUtil.TYPE_CONNECTED) {
                Cursor cursor = (Cursor) notificationAdapter.getItem(position);

                // start new job to read that notification
                feedController.readNotification(cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.NOTIFICATION_ID)));

                int action = cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.ACTION));
                switch (action) {
                    case Constants.TAG_CREATED:
                    case Constants.TAG_LIKED:
                    case Constants.TAG_REPLY:
                        Intent intent = new Intent(getActivity(), ActivityPostDetail.class);
                        intent.putExtra(Constants.KEY_POST_ID, cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.POST_ID)));
                        startActivity(intent);
                        break;
                    case Constants.TAG_ADDED:
                        Intent in = new Intent(getActivity(), ActivityUserProfile.class);
                        in.putExtra(Constants.KEY_USER_ID, cursor.getInt(cursor.getColumnIndex(DataProviderContract.Notification.USER_ID)));
                        in.putExtra(Constants.KEY_USER_NAME, cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.USER_NAME)));
                        in.putExtra(Constants.KEY_PHOTO, cursor.getString(cursor.getColumnIndex(DataProviderContract.Notification.USER_PHOTO)));
                        startActivity(in);
                        break;
                }
            } else {
                Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void showLoader() {
        View view = getView().findViewById(R.id.progress);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        if (isVisible()) {
            View view = getView().findViewById(R.id.progress);
            view.setVisibility(View.GONE);

            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public interface INotificationUpdate {
        void updateNotificationCount();
    }

}
