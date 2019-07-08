package com.esch.eschfindr;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.esch.eschfindr.RequestFragment.OnListFragmentInteractionListener;
import com.esch.eschfindr.data.Request;
import com.esch.eschfindr.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class RequestRecyclerViewAdapter extends RecyclerView.Adapter<RequestRecyclerViewAdapter.ViewHolder> {

    private final List<Request> mValues;
    private final OnListFragmentInteractionListener mListener;

    public RequestRecyclerViewAdapter(List<Request> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.requester.setText(mValues.get(position).name);

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.accept(holder.mItem);
            }
        });

        holder.declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.decline(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView requester;
        public final Button acceptButton;
        public final Button declineButton;
        public Request mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            requester = (TextView) view.findViewById(R.id.requester);
            acceptButton = (Button) view.findViewById(R.id.requestAccept);
            declineButton = (Button) view.findViewById(R.id.requestDecline);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
