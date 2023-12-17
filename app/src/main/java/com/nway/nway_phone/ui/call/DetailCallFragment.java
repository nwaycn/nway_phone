package com.nway.nway_phone.ui.call;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nway.nway_phone.AhListener;
import com.nway.nway_phone.databinding.FragmentCallDetailBinding;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.nway.nway_phone.R;
import com.nway.nway_phone.common.CallLogDatabaseHelper;
import com.nway.nway_phone.common.RecordingPlayer;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

public class DetailCallFragment extends Fragment {

    private static final String TAG="detailCallFragment";
    private FragmentCallDetailBinding binding;
    private AhListener ahListener;
    private String callee;
    private List<CallHistory> callHistoryList;
    private int page = 1;
    private CallHistoryDetailAdapter callHistoryDetailAdapter;

    private RecordingPlayer recordingPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        binding = FragmentCallDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ahListener.setTitleBarTitle("通话详情");
        ahListener.setLeftTitle("返回","");
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.callee = getArguments().getString("callee","");
        binding.callee.setText(callee);
        initCallHistory();
        refresh();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AhListener) {
            ahListener = (AhListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListener");
        }
    }

    public void initCallHistory(){
        this.callHistoryList = getListData(callee);
        RecyclerView recyclerView = binding.recyclerCallHistory;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);

        this.callHistoryDetailAdapter = new CallHistoryDetailAdapter(requireContext(),callHistoryList);
        callHistoryDetailAdapter.setItemOnClickListener(new CallHistoryDetailAdapter.ItemOnClickListener() {
            @Override
            public void onPlayerClick(View view, int i) {
                playRecording(callHistoryList.get(i));
            }


        });
        recyclerView.setAdapter(callHistoryDetailAdapter);
    }

    public List<CallHistory> getListData(String number){
        List<CallHistory> callHistoryList = new ArrayList<>();
        int pageLimit = 10;
        callHistoryList = CallLogDatabaseHelper.getInstance(getContext()).queryByPage(number,page, pageLimit);
        return callHistoryList;
    }

    public void refresh(){
        //下拉刷新和上拉加载
        RefreshLayout refreshLayout = binding.refreshLayout;
        refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                callHistoryDetailAdapter.refresh(getListData(callee));
                refreshLayout.finishRefresh();
//                refreshLayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                page += 1;
                List<CallHistory> listData = getListData(callee);
                callHistoryDetailAdapter.add(listData);
                refreshLayout.setNoMoreData(listData.size() == 0);
                refreshLayout.finishLoadMore();
//                refreshLayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            }
        });
    }

    public void playRecording(CallHistory callHistory){
        Log.e(TAG,"录音路径："+callHistory.getRecordFile());
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_recording_player,
                null);
        new MaterialDialog.Builder(requireContext())
                .customView(layout, true)
                .title(R.string.recording_player)
                .showListener(dialog -> {
                    if(callHistory.getCallBill() != 0){
                        ImageButton imageButton = layout.findViewById(R.id.ib_player);
                        SeekBar seekBar = layout.findViewById(R.id.sb_progress);
                        seekBar.setMax(callHistory.getCallBill());
                        recordingPlayer = new RecordingPlayer(requireContext(),callHistory.getRecordFile());
                        if (recordingPlayer.getMediaPlayer() != null){
                            Handler handler = new Handler();
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if(recordingPlayer.playStatus() != 3){
                                        int currentProgress = recordingPlayer.getCurrentPosition() /1000;
                                        seekBar.setProgress(currentProgress);
                                        handler.postDelayed(this,500);
                                        if(currentProgress == callHistory.getCallBill()){
                                            recordingPlayer.stop();
                                            imageButton.setImageResource(R.drawable.ic_baseline_pause_24);
                                        }
                                    }
                                }
                            };
                            recordingPlayer.start();
                            handler.postDelayed(runnable,500);

                            imageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(recordingPlayer.playStatus() == 1){
                                        recordingPlayer.pause();
                                        imageButton.setImageResource(R.drawable.ic_baseline_pause_24);
                                    }else if (recordingPlayer.playStatus() == 3){
                                        recordingPlayer.reset();
                                        recordingPlayer.start();
                                        imageButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                                        handler.postDelayed(runnable,500);
                                    }else{
                                        recordingPlayer.start();
                                        imageButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                                    }
                                }
                            });
                        }
                    }

                })
                .dismissListener(dialog -> {
                    recordingPlayer.stop();
                })

                .show();

    }
}
