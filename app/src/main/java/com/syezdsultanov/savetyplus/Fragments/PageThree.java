package com.syezdsultanov.savetyplus.Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.syezdsultanov.savetyplus.Activities.MainActivity;
import com.syezdsultanov.savetyplus.R;

public class PageThree extends Fragment {
    private FragmentActivity fa;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fa = super.getActivity();
        View three = inflater.inflate(R.layout.span_three, container, false);

        Button validerInscription = three.findViewById(R.id.startButton);
        validerInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(fa, MainActivity.class);
                startActivity(intent);
            }
        });

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        return three;
    }

}