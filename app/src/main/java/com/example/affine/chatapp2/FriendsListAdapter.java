package com.example.affine.chatapp2;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder> implements Filterable{
    private List<UserInfo> mDataset = Lists.newArrayList();
    private List<UserInfo> mFilteredDataset = Lists.newArrayList();
    private RecyclerViewClickListener mListener;

    public FriendsListAdapter() {

    }

    public interface RecyclerViewClickListener {
        void onClick(UserInfo userInfo);
         void onProfileClick(UserInfo userInfo);
    }



    public void setOnItemClickListener(RecyclerViewClickListener mListener) {
        this.mListener = mListener;
    }

    public FriendsListAdapter(List<UserInfo> myDataset) {
        mDataset = myDataset;
    }

    public void setmDataset(List<UserInfo> myDataset) {
        mDataset = myDataset;
        getFilter().filter("");
        //notifyDataSetChanged();
    }

    @Override
    public FriendsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final UserInfo dataModel = mFilteredDataset.get(position);
        holder.message.setText(dataModel.getName());
        if(dataModel.getLastMessage() != null) {
        holder.lastMessage.setText(dataModel.getLastMessage()); }
        if(dataModel.getLastChatTime() != null) {
        holder.lastChatTime.setText(dataModel.getLastChatTime()); }
        /*if(dataModel.getUnreadMessageCount() != 0) {
        holder.unreadMessageCount.setText(String.valueOf(dataModel.getUnreadMessageCount())); }*/
        holder.unreadMessageCount.setText(String.valueOf(dataModel.getUnreadMessageCount()));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(dataModel);
                }
            }
        });
        holder.clientPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onProfileClick(dataModel);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        //return mDataset.size();
        return mFilteredDataset.size();
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                FilterResults filterResults = new FilterResults();
                if (charString.isEmpty()) {
                    filterResults.values = mDataset;
                } else {

                    List<UserInfo> filteredList = new ArrayList<>();

                    for (UserInfo userInfo : mDataset) {

                        if (userInfo.getName().toLowerCase().contains(charString) || userInfo.getName().contains(charString)) {
                            Log.e("Filtering", "something found");
                            filteredList.add(userInfo);
                        }
                    }

                    filterResults.values = filteredList;
                }

                Log.e("Filtering", "message filtered");
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                Log.e("Filtering", "message published");
                mFilteredDataset = (ArrayList<UserInfo>) filterResults.values;
                FriendsListAdapter.this.notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public TextView lastChatTime;
        public TextView lastMessage;
        public TextView unreadMessageCount;
        public LinearLayout layout;
        public de.hdodenhof.circleimageview.CircleImageView clientPicture;

        public ViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.channelName);
            lastChatTime = v.findViewById(R.id.lastChatTime);
            lastMessage = v.findViewById(R.id.lastMessage);
            unreadMessageCount = v.findViewById(R.id.unreadMessageCount);
            layout = v.findViewById(R.id.fitemLayout);
            clientPicture = v.findViewById(R.id.clientdp);
        }
    }
}