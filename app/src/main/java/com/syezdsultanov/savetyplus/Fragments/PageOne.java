package com.syezdsultanov.savetyplus.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.syezdsultanov.savetyplus.R;

public class PageOne extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentActivity fa = super.getActivity();
        return inflater.inflate(R.layout.span_one, container, false);
    }

}
