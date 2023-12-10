package com.nway.nway_phone.ui.my;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nway.nway_phone.AhListener;
import com.nway.nway_phone.R;
import com.nway.nway_phone.common.MyUtils;
import com.nway.nway_phone.databinding.FragmentMyDetailBinding;

public class DetailMyFragment extends Fragment {

    private FragmentMyDetailBinding binding;
    private AhListener ahListener;
    private int item;
    private final static String TAG="detailMyFragment";

    public static DetailMyFragment newInstance() {
        return new DetailMyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentMyDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ahListener.setLeftTitle("返回","");

        return root;
    }

    @SuppressLint({"SetTextI18n", "SetJavaScriptEnabled"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert getArguments() != null;
        this.item = getArguments().getInt("item",0);
        switch (item){
            case 1:
                binding.tvContent.setVisibility(View.VISIBLE);
                binding.tvContent.setText(R.string.about_detail);
                ahListener.setTitleBarTitle("关于我们");
                break;
            case 2:
                ahListener.setTitleBarTitle("用户协议");
                WebView webView = binding.wbContent;
                webView.setVisibility(View.VISIBLE);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadUrl("file:///android_asset/index.html");
                break;
            case 3:
                binding.tvContent.setVisibility(View.VISIBLE);
                ahListener.setTitleBarTitle("检查更新");
                String versionName = MyUtils.getAppVersion(requireContext());
                binding.tvContent.setText("当前版本："+versionName+" 无需更新");
                break;
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}