package com.esch.eschfindr;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esch.eschfindr.FriendFragment.OnListFragmentInteractionListener;
import com.esch.eschfindr.data.Friend;
import com.esch.eschfindr.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FriendRecyclerViewAdapter extends RecyclerView.Adapter<FriendRecyclerViewAdapter.ViewHolder> {

    private final List<Friend> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final Context context;

    public FriendRecyclerViewAdapter(Context context,List<Friend> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.friend = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).name);
        holder.mContentView.setText(mValues.get(position).date);
        if(mValues.get(position).allow == Friend.TRUE) {
            holder.mLinearLayout.setBackgroundColor(context.getResources().getColor(R.color.green));
        } else {
            holder.mLinearLayout.setBackgroundColor(context.getResources().getColor(R.color.red));
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.friend);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final LinearLayout mLinearLayout;
        public Friend friend;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.user_name);
            mContentView = (TextView) view.findViewById(R.id.update_date);
            mLinearLayout = (LinearLayout) view.findViewById(R.id.friendBackground);
            }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
