package com.beappclootp.lmt.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.beappclootp.lmt.R;
import com.beappclootp.lmt.util.UnCaughtException;
import com.beappclootp.lmt.util.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BiClooListActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private Context mContext;

    private static final int PERMISSION_REQ = 0;
    private LatLng mUserLatestLocation;

    @BindView(R.id.navigation)
    BottomNavigationView navigation;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private ActionBar actionBar;

    @BindString(R.string.title_home)
    String titleHome;
    @BindString(R.string.title_map)
    String titleMap;
    @BindString(R.string.title_favorites)
    String titleFav;
    @BindString(R.string.title_setting)
    String titleSetting;

    @BindString(R.string.permission_explanation)
    String textPermissions;
    @BindString(R.string.permission_explanation_action)
    String textPermissionsAction;

    private String fragmentTitle = "";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            return navBottomBarAction(item);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bicloo);
        ButterKnife.bind(this);

        mContext = this;

        // catch error
        Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
                BiClooListActivity.this));

        checkPermissions(savedInstanceState);
        initToolBar();
        initNavBottomBar();

    }

    private void checkPermissions(Bundle savedInstanceState) {
        // Check fine location permission has been granted
        if (!Utils.checkFineLocationPermission(this)) {
            // See if user has denied permission in the past
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show a simple snackbar explaining the request instead
                showPermissionSnackbar();
            } else {
                // Otherwise request permission from user
                if (savedInstanceState == null) {
                    requestFineLocationPermission();
                }
            }
        } else {
            // Otherwise permission is granted (which is always the case on pre-M devices)
            fineLocationPermissionGranted();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_reload:
                //implemented in Fragment
                return false;
            case R.id.toolbar_filter:
                //implemented in Fragment
                return false;

        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Utils.detailBackPressed = true;
        finish();
    }

    private void initNavBottomBar() {
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    private boolean navBottomBarAction(@NonNull MenuItem item) {
        boolean fragmentTransaction = false;
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new BiClooListFragment();
                fragmentTransaction = true;
                fragmentTitle = titleHome;
                Utils.isFragmenMapVisible = false;
                break;
            case R.id.navigation_map:
                if (Utils.isFragmenMapVisible) {
                    fragmentTransaction = false;
                } else {
                    fragment = new BiClooMapFragment();
                    fragmentTransaction = true;
                    fragmentTitle = titleMap;
                    Utils.isFragmenMapVisible = true;
                }
                break;
            case R.id.navigation_favorites:
                fragment = new BiClooFavFragment();
                fragmentTransaction = true;
                fragmentTitle = titleFav;
                Utils.isFragmenMapVisible = false;
                break;
            case R.id.navigation_setting:
                fragment = new BiClooSettingFragment();
                fragmentTransaction = true;
                fragmentTitle = titleSetting;
                Utils.isFragmenMapVisible = false;
                break;
        }

        return commitFragment(fragmentTransaction, fragment);
    }

    private boolean commitFragment(boolean fragmentTransaction, Fragment fragment) {
        if (fragmentTransaction) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, fragment, fragmentTitle)
                    .commit();
            actionBar.setTitle(fragmentTitle);
            return true;
        }
        return false;
    }

    private void initToolBar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
    }

    /**
     * Permissions request result callback
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fineLocationPermissionGranted();
                }
        }
    }

    /**
     * Show a permission explanation snackbar
     */
    private void showPermissionSnackbar() {
        Snackbar.make(
                findViewById(R.id.container), textPermissions, Snackbar.LENGTH_LONG)
                .setAction(textPermissionsAction, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestFineLocationPermission();
                    }
                })
                .show();
    }

    /**
     * Request the fine location permission from the user
     */
    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ);
    }

    /**
     * Run when fine location permission has been granted
     */
    private void fineLocationPermissionGranted() {
        //TODO use a Service instead
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (Utils.checkFineLocationPermission(this)) {
            fusedLocationClient.getLastLocation()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            mUserLatestLocation = Utils.getLocation(mContext) != null ? Utils.getLocation(mContext) : new LatLng(Utils.LATITUD_DEFAULT, Utils.LONGITUD_DEFAULT);
                        }
                    })
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                mUserLatestLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                Utils.storeLocation(mContext, mUserLatestLocation);
                            } else {
                                mUserLatestLocation = Utils.getLocation(mContext) != null ? Utils.getLocation(mContext) : new LatLng(Utils.LATITUD_DEFAULT, Utils.LONGITUD_DEFAULT);
                            }
                        }
                    });

        }


    }

}
