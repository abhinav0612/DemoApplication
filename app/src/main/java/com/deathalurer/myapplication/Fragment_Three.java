package com.deathalurer.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by Abhinav Singh on 26,April,2020
 */
public class Fragment_Three extends Fragment {
    TextView textView;
    SharedPreferences preferences;
    Button button;
    CountDownTimer timer;
    private boolean isAvailable = false;
    static String TAG = "Fragment_Three";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.layout_fragment_three,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = view.findViewById(R.id.walletCoins);
        button = view.findViewById(R.id.button);
        preferences = this.getActivity().getSharedPreferences("WalletPoints", Context.MODE_PRIVATE);
        textView.setText(0+"");

//        if(preferences.contains("Coins")){
//            int coins = preferences.getInt("Coins",1);
//            textView.setText(coins+"");
//        }
        timer = new CountDownTimer(22000,1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                refreshScore();
            }
        };
        checkAvailability();
        Log.e(TAG, "onViewCreated: " + isAvailable );
        if(isAvailable){
            timer.start();
        }
        else{
            textView.setText("Please select your home location to start earning coins");
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(preferences.contains("Coins")){
                    int coins = preferences.getInt("Coins",1);
                    textView.setText(coins+"");
                }
            }
        });




    }
    void refreshScore(){
        timer.start();
        if(preferences.contains("Coins")){
            int coins = preferences.getInt("Coins",1);
            Log.e(TAG, "refreshScore: " + coins );
            textView.setText(coins+"");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(preferences.contains("Coins")){
            int coins = preferences.getInt("Coins",1);
            textView.setText(coins+"");
        }
    }
    void checkAvailability(){
        preferences = this.getActivity().
                getSharedPreferences("WalletPoints", Context.MODE_PRIVATE);
        isAvailable = preferences.getBoolean("LocationSelected",false);
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: ");
        checkAvailability();
    }
}
