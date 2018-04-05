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

    //private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        //init DB using GreenDAO lib
        DBManager.setup(this);
       /* DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "bicloo-db-encrypted" : "bicloo-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("biclopassword") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();*/
    }

   /* public DaoSession getDaoSession() {
        return daoSession;
    }*/
}