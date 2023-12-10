package com.nway.nway_phone.ui.call;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nway.nway_phone.R;
import com.nway.nway_phone.common.MyUtils;

import java.util.List;

public class CallHistoryDetailAdapter extends RecyclerView.Adapter<CallHistoryDetailAdapter.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    private Context context;
    private List<CallHistory> callHistoryList;
    private ItemOnClickListener itemOnClickListener;

    @Override
    public int getItemViewType(int position) {
        return callHistoryList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == VIEW_TYPE_LOADING) {
//            View view = LayoutInflater.from(parent.getContext()).inflate()
//        }

//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_call_history,null);
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_call_history_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        holder.tvCallDate.setText(callHistoryList.get(position).getCallDate());
        holder.tvCallDirection.setText(callDirectionConvert(callHistoryList.get(position).getCallDirection()));

        holder.tvCallBill.setText(MyUtils.secToTime(callHistoryList.get(position).getCallBill()));
        holder.tvCallHangupCause.setText(hangupCauseConvert(callHistoryList.get(position).getCallHangupCause()));

        if(callHistoryList.get(position).getCallBill() != 0){
            holder.ibPlayer.setVisibility(View.VISIBLE);
            if (itemOnClickListener != null){
                holder.ibPlayer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemOnClickListener.onPlayerClick(v,holder.getAbsoluteAdapterPosition());
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return callHistoryList.size();
    }


    public void addOnHangup(CallHistory callHistory){

        callHistoryList.add(0,callHistory);
        notifyItemInserted(0);
    }
    public void add (List<CallHistory> addList){
        //增加数据
        int position = callHistoryList.size();
        callHistoryList.addAll(position, addList);
        notifyItemInserted(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(List<CallHistory> newList) {
        //刷新数据
//        callHistoryList.removeAll(callHistoryList);
        callHistoryList.clear();
        callHistoryList.addAll(newList);
        notifyDataSetChanged();
    }

    public String callDirectionConvert(String direction){
        String callDirection;
        switch (direction){
            case "Outgoing":
                callDirection = "呼出";
                break;
            case "Incoming":
                callDirection = "呼入";
                break;
            case "Missed":
                callDirection = "未接";
                break;
            case "Rejected":
                callDirection = "拒接";
                break;
            default:
                callDirection = direction;
                break;
        }
        return callDirection;
    }

    public String hangupCauseConvert(String hangupCause){
        String status;
        switch (hangupCause){
            case "Aborted":
                status = "未接通";
                break;
            case "Success":
                status = "";
                break;
            case "Missed":
                status = "未接";
                break;
            case "Rejected":
                status = "拒接";
                break;
            default:
                status = hangupCause;
                break;
        }
        return status;
    }



    public interface ItemOnClickListener{
        public void onPlayerClick(View view,int i);
    }
    public void setItemOnClickListener(ItemOnClickListener itemOnClickListener){
        this.itemOnClickListener = itemOnClickListener;
    }

    public CallHistoryDetailAdapter(Context context, List<CallHistory> callHistoryList){
        this.context = context;
        this.callHistoryList = callHistoryList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout detailCallHistory;
        TextView tvCallDate;
        TextView tvCallDirection;
        TextView tvCallBill;
        TextView tvCallHangupCause;
        ImageButton ibPlayer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            detailCallHistory = itemView.findViewById(R.id.detail_call_history);
            tvCallDate = itemView.findViewById(R.id.tv_call_date);
            tvCallDirection = itemView.findViewById(R.id.tv_call_direction);
            tvCallBill = itemView.findViewById(R.id.tv_call_bill);
            tvCallHangupCause = itemView.findViewById(R.id.tv_call_hangup_cause);
            ibPlayer = itemView.findViewById(R.id.ib_player);
        }
    }

}
