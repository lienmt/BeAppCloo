package com.beappclootp.lmt.util;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import com.beappclootp.lmt.model.BiClooData;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;

/**
 * Created by lien.muguercia on 31/03/2018.
 */
public class Utils {

    public static final double LATITUD_DEFAULT = 47.2172628;
    public static final double LONGITUD_DEFAULT = -1.5500204;

    private static final String DISTANCE_KM_POSTFIX = "km";
    private static final String DISTANCE_M_POSTFIX = "m";

    private static final String MAPS_INTENT_URI_GEO = "geo:";
    public static final String MAPS_NAVIGATION_INTENT_URI = "google.navigation:mode=w&q=";

    private static final String PREFERENCES_LAT = "lat";
    private static final String PREFERENCES_LNG = "lng";

    private static final String PREFS_NAME = "BeAppCloo_APP";
    private static final String PREFERENCES_FAVORITES = "pointFavoriteKey";

    private static final String PREFERENCES_FILTER_OPEN = "showAllStationFilterKey";
    private static final String PREFERENCES_FILTER_BICY = "showAllBicyFilterKey";
    private static final String PREFERENCES_FILTER_PARKING = "showAllParkingFilterKey";


    public static boolean isFragmenMapVisible = false;
    public static boolean detailBackPressed = false;

    //dialog
    private static boolean dialog_showing = false;
    private static ProgressDialog dialog;
    private static boolean dialog_timer_run = false;

    public static boolean isNetworkAvailable(Context context) {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
                    || (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED);
        }
        return connected;
    }

    /**
     * Sort list according to user last saved location
     */
    public static List<BiClooData> sortFromLocation(final LatLng curLatLng, List<BiClooData> dataList) {
        List<BiClooData> list = new ArrayList<>(dataList);
        if (curLatLng != null) {
            Collections.sort(list,
                    new Comparator<BiClooData>() {
                        @Override
                        public int compare(BiClooData lhs, BiClooData rhs) {
                            LatLng lhsLoc = new LatLng(lhs.getLatitud(), lhs.getLongitud());
                            LatLng rhsLoc = new LatLng(rhs.getLatitud(), rhs.getLongitud());
                            double lhsDistance = SphericalUtil.computeDistanceBetween(
                                    lhsLoc, curLatLng);
                            double rhsDistance = SphericalUtil.computeDistanceBetween(
                                    rhsLoc, curLatLng);
                            return (int) (lhsDistance - rhsDistance);
                        }
                    }
            );
        }
        return list;
    }

    /**
     * Calculate distance between two LatLng points and format it string for
     * display
     */
    public static String formatDistanceBetween(LatLng point1, LatLng point2) {
        if (point1 == null || point2 == null) {
            return "-";
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        double distance = Math.round(SphericalUtil.computeDistanceBetween(point1, point2));

        // Adjust to KM if M goes over 1000
        if (distance >= 1000) {
            numberFormat.setMaximumFractionDigits(1);
            return numberFormat.format(distance / 1000) + DISTANCE_KM_POSTFIX;
        }
        return numberFormat.format(distance) + DISTANCE_M_POSTFIX;
    }

    public static void openInMapsApp(BiClooData biClooObj, Context activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Utils.MAPS_INTENT_URI_GEO +
                Uri.encode(biClooObj.getLatitud() + ", " + biClooObj.getLongitud()) +
                "?q=" +
                Uri.encode(biClooObj.getAddress())));
        activity.startActivity(intent);
    }

    /**
     * Check if the app has access to fine location permission. On pre-M
     * devices this will always return true.
     */
    public static boolean checkFineLocationPermission(Context context) {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Store the location in the app preferences.
     */
    public static void storeLocation(Context context, LatLng location) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PREFERENCES_LAT, Double.doubleToRawLongBits(location.latitude));
        editor.putLong(PREFERENCES_LNG, Double.doubleToRawLongBits(location.longitude));
        editor.apply();
    }

    /**
     * Fetch the location from app preferences.
     */
    public static LatLng getLocation(Context context) {
        if (!checkFineLocationPermission(context)) {
            return null;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        Long lat = prefs.getLong(PREFERENCES_LAT, Long.MAX_VALUE);
        Long lng = prefs.getLong(PREFERENCES_LNG, Long.MAX_VALUE);
        if (lat != Long.MAX_VALUE && lng != Long.MAX_VALUE) {
            Double latDbl = Double.longBitsToDouble(lat);
            Double lngDbl = Double.longBitsToDouble(lng);
            return new LatLng(latDbl, lngDbl);
        }
        return null;
    }

    /*
     *  Visual related methods
     */

    public static void showAlert(Context _context, String title, String Messange, String Ok_button) {
        AlertDialog.Builder confirm = new AlertDialog.Builder(_context);
        if (title != null && title.length() > 0)
            confirm.setTitle(title);
        confirm.setMessage(Messange);
        confirm.setPositiveButton(Ok_button, null);
        confirm.show();
    }

    public static void showProgrees(Context act, String text) {
        if (dialog_showing)
            dialog.setTitle(text);
        else {
            dialog = ProgressDialog.show(act, null, text);
            dialog_showing = true;
        }

        if (dialog_timer_run) {
            dialog_timer_run = false;
        }

    }

    public static void hideProgrees() {
        if (dialog_showing) {
            dialog.cancel();
            dialog_showing = false;
        }
    }

    /**
     * Favorites preferences related methods
     */
    public static void addFavorite(Context context, String idStation) {
        List<String> favorites = getFavorites(context);
        if (favorites == null)
            favorites = new ArrayList<>();
        favorites.add(idStation);
        saveFavorites(context, favorites);
    }

    public static void removeFavorite(Context context, String idStation) {
        ArrayList<String> favorites = getFavorites(context);
        if (favorites != null) {
            favorites.remove(idStation);
            saveFavorites(context, favorites);
        }
    }

    public static boolean isFavorite(Context context, String idStation) {

        ArrayList<String> favorites = getFavorites(context);
        if (favorites != null) {
            for (String fav : favorites) {
                if (fav.equals(idStation))
                    return true;
            }
        }

        return false;
    }

    private static ArrayList<String> getFavorites(Context context) {
        SharedPreferences settings;
        List<String> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(PREFERENCES_FAVORITES)) {
            String jsonFavorites = settings.getString(PREFERENCES_FAVORITES, null);
            Gson gson = new Gson();
            String[] favoriteItems = gson.fromJson(jsonFavorites,
                    String[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<>(favorites);
        } else
            return null;

        return (ArrayList<String>) favorites;
    }

    private static void saveFavorites(Context context, List<String> favorites) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(PREFERENCES_FAVORITES, jsonFavorites);

        editor.apply();
    }

    /**
     * Filter preferences related methods
     */

    public static void saveFilterOpen(Context context, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCES_FILTER_OPEN, value);
        editor.apply();
    }

    public static boolean getFilterOpen(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFERENCES_FILTER_OPEN, true);
    }

    public static void saveFilterBicy(Context context, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCES_FILTER_BICY, value);
        editor.apply();
    }

    public static boolean getFilterBicy(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFERENCES_FILTER_BICY, true);
    }

    public static void saveFilterParking(Context context, boolean value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREFERENCES_FILTER_PARKING, value);
        editor.apply();
    }

    public static boolean getFilterParking(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        return prefs.getBoolean(PREFERENCES_FILTER_PARKING, true);
    }

}
