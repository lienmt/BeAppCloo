package com.beappclootp.lmt;

import android.app.Application;

import com.beappclootp.lmt.database.DBManager;
import com.beappclootp.lmt.model.DaoMaster;
import com.beappclootp.lmt.model.DaoSession;

import org.greenrobot.greendao.database.Database;

/**
 * Created by lien.muguercia on 31/03/2018.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //init DB using GreenDAO lib
        DBManager.setup(this);
    }

}