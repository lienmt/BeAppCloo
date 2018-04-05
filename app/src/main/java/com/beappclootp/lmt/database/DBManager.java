package com.beappclootp.lmt.database;

import android.content.Context;

import com.beappclootp.lmt.model.BiClooData;
import com.beappclootp.lmt.model.BiClooDataDao;
import com.beappclootp.lmt.model.DaoMaster;
import com.beappclootp.lmt.model.DaoSession;
import com.beappclootp.lmt.util.Utils;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;

import java.util.List;

/**
 * Created by lien.muguercia on 04/04/2018.
 */
public class DBManager {

    private static final boolean ENCRYPTED = false;
    private static DaoSession daoSession;
    private static BiClooDataDao biClooDataDao;

    public static void setup(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, ENCRYPTED ? "bicloo-db-encrypted" : "bicloo-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("biclopassword") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public static void init() {
        biClooDataDao = daoSession.getBiClooDataDao();
    }

    private static long getBiClooDataDAOSize() {
        return biClooDataDao.count();
    }

    private static void clearBiClooDataDAO() {
        biClooDataDao.deleteAll();
    }

    private static void saveBiClooData(BiClooData biclo) {
        biClooDataDao.save(biclo);
    }

    public static void saveBiClooList(List<BiClooData> listResponse, Context context) {
        if (listResponse.size() > 0 && getBiClooDataDAOSize() > 0)
            clearBiClooDataDAO();

        for (BiClooData biclo : listResponse) {
            biclo.setLatitud(biclo.getPosition().getLat());
            biclo.setLongitud(biclo.getPosition().getLng());

            //favorite
            biclo.setFavorite(Utils.isFavorite(context, String.valueOf(biclo.getNumber())));

            //compute & save distance from user current location
            LatLng bicloLoc = new LatLng(biclo.getLatitud(), biclo.getLongitud());
            LatLng userLatestLocation = getUserLatestLocation(context);
            String distance = Utils.formatDistanceBetween(userLatestLocation, bicloLoc);
            biclo.setDistance(distance);

            saveBiClooData(biclo);
        }
    }

    private static LatLng getUserLatestLocation(Context context) {
        return Utils.getLocation(context) != null ? Utils.getLocation(context) : new LatLng(Utils.LATITUD_DEFAULT, Utils.LONGITUD_DEFAULT);
    }

    public static Query<BiClooData> getAllBiClooData() {
        return biClooDataDao.queryBuilder().orderAsc(BiClooDataDao.Properties.Number).build();
    }


}
