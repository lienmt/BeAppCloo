package com.beappclootp.lmt.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.beappclootp.lmt.R;
import com.beappclootp.lmt.database.DBManager;
import com.beappclootp.lmt.model.BiClooData;
import com.beappclootp.lmt.network.NetServiceClient;
import com.beappclootp.lmt.network.NetworkService;
import com.beappclootp.lmt.util.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by lien.muguercia on 30/03/2018.
 */

public class SplashActivity extends AppCompatActivity {

    private Context mContext;
    @BindView(R.id.imageViewLogo)
    ImageView imageSplashLogo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        setup();
        bindLogo();

        Thread welcomeThread = new Thread() {

            @Override
            public void run() {
                try {
                    super.run();
                    if (Utils.isNetworkAvailable(SplashActivity.this)) {
                        getBiClooOnlineData();
                    } else {
                        sleep(2000);
                        navigateToHome();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    finish();
                }
            }
        };
        welcomeThread.start();
    }

    private void setup() {
        mContext = getApplicationContext();
        DBManager.init();
    }

    private void getBiClooOnlineData() {

        NetworkService networkService = NetServiceClient.setupClient(true).create(NetworkService.class);
        Call<List<BiClooData>> call = networkService.getAllData();
        call.enqueue(new Callback<List<BiClooData>>() {
            @Override
            public void onResponse(Call<List<BiClooData>> call, Response<List<BiClooData>> response) {
                if (response.body() != null) {
                    saveIntoDB(response.body());
                } else {
                    navigateToHome();
                }
            }

            @Override
            public void onFailure(Call<List<BiClooData>> call, Throwable t) {
                t.printStackTrace();
                navigateToHome();
            }
        });

    }

    public void navigateToHome() {
        Intent intent = new Intent(SplashActivity.this, BiClooListActivity.class);
        startActivity(intent);
        finish();
    }

    public void saveIntoDB(List<BiClooData> listResponse) {
        DBManager.saveBiClooList(listResponse, mContext);
        navigateToHome();
    }

    private void bindLogo() {
        // Start animating the image
        final AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
        animation1.setDuration(500);
        final AlphaAnimation animation2 = new AlphaAnimation(1.0f, 0.2f);
        animation2.setDuration(500);
        //animation1 AnimationListener
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                // start animation2 when animation1 ends (continue)
                imageSplashLogo.startAnimation(animation2);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        //animation2 AnimationListener
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                // start animation1 when animation2 ends (repeat)
                imageSplashLogo.startAnimation(animation1);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        imageSplashLogo.startAnimation(animation1);
    }

}
