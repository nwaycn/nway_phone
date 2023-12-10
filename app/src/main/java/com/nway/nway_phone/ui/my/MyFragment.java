package com.nway.nway_phone.ui.my;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nway.nway_phone.AhListener;
import com.nway.nway_phone.R;
import com.nway.nway_phone.databinding.FragmentMyBinding;
import com.nway.nway_phone.linphone.SipPhone;

public class MyFragment extends Fragment implements View.OnClickListener {

    private FragmentMyBinding binding;
    private AhListener ahListener;
    private final static String TAG="myFragment";

    public static MyFragment newInstance() {
        return new MyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        MyViewModel myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        binding =  FragmentMyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textMy;
        binding.tvAbout.setOnClickListener(this);
        binding.tvUserAgreement.setOnClickListener(this);
        binding.tvUpdate.setOnClickListener(this);

        return root;
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
    public void onResume() {
        super.onResume();
        ahListener.setTitleBarTitle("我");
        SipPhone sp = SipPhone.getInstance();
        sp.setLeftAction(requireContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int item = 0;
        switch (v.getId()){
            case R.id.tv_about:
                item = 1;
                break;
            case R.id.tv_user_agreement:
                item = 2;
                break;
            case R.id.tv_update:
                item = 3;
                break;
        }
        Bundle bundle = new Bundle();
        bundle.putInt("item",item);
        NavController navController = Navigation.findNavController(binding.getRoot());
        navController.navigate(R.id.action_navigation_my_to_navigation_my_detail,bundle);
    }
}