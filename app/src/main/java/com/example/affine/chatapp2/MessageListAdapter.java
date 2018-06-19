package com.example.affine.chatapp2;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> implements Filterable {
    private List<DataModel> mDataset = Lists.newArrayList();
    private List<DataModel> mFilteredDataset = Lists.newArrayList();
    private int userId;

    public MessageListAdapter() {

    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setmDataset(List<DataModel> myDataset) {
        mDataset = myDataset;
        getFilter().filter("");
    }

    public void addItem(DataModel dataModel){
        mDataset.add(dataModel);
        notifyDataSetChanged();
        //notifyItemInserted(mDataset.size()-1);
    }

    @Override
    public MessageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataModel dataModel = mFilteredDataset.get(position);
        if (dataModel == null) {
            return;
        }
        holder.message.setText(dataModel.getMessage());
        holder.sentTime.setText(TimeDateUtil.formatTimeOnly(dataModel.getMsgId()));
        if (dataModel.getUserId() != userId) {
            holder.deliveredStatus.setVisibility(View.INVISIBLE);
            holder.messageLayout.setGravity(Gravity.LEFT);
            holder.message.setBackgroundResource(R.drawable.their_message);
        } else {
            holder.messageLayout.setGravity(Gravity.RIGHT);
            holder.deliveredStatus.setVisibility(View.VISIBLE);
            holder.message.setBackgroundResource(R.drawable.my_message);
            if(dataModel.getDeliveredStatus().equals("sent")) {
                holder.deliveredStatus.setBackgroundResource(R.drawable.singletick);
            }
            else if (dataModel.getDeliveredStatus().equals("delivered") ) {
                Log.e("Holder","Double Tick Starting");
                holder.deliveredStatus.setBackgroundResource(R.drawable.doubletick);
                Log.e("Holder","Double Tick Done");
            }
            else if (dataModel.getDeliveredStatus().equals("read") ){
                holder.deliveredStatus.setBackgroundResource(R.drawable.readtick);
            }
        }
    }

    @Override
    public int getItemCount() {
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

                    List<DataModel> filteredList = new ArrayList<>();

                    for (DataModel dataModel : mDataset) {

                        if (dataModel.getMessage().toLowerCase().contains(charString) || dataModel.getMessage().contains(charString)) {
                            Log.e("Filtering", "something found");
                            filteredList.add(dataModel);
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
                mFilteredDataset = (ArrayList<DataModel>) filterResults.values;
                MessageListAdapter.this.notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message;
        public TextView sentTime;
        public LinearLayout messageLayout;
        public ImageView deliveredStatus;


        public ViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.chatMessage);
            sentTime = v.findViewById(R.id.sentTime);
            messageLayout = v.findViewById(R.id.messageParentLayout);
            deliveredStatus = v.findViewById(R.id.delivered);
        }
    }

}