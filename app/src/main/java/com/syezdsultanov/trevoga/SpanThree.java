package com.syezdsultanov.trevoga;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SpanThree extends Fragment {
    private FragmentActivity fa;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fa = super.getActivity();
        View three = inflater.inflate(R.layout.span_three, container, false);

        Button validerInscription = three.findViewById(R.id.valider);
        validerInscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(fa, MainActivity.class);
                startActivity(intent);
            }
        });

        return three;
    }

}