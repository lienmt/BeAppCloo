package com.beappclootp.lmt.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.beappclootp.lmt.R;
import com.beappclootp.lmt.adapter.BiClooAdapter;
import com.beappclootp.lmt.adapter.BiClooRecyclerView;
import com.beappclootp.lmt.database.DBManager;
import com.beappclootp.lmt.model.BiClooData;
import com.beappclootp.lmt.network.NetServiceClient;
import com.beappclootp.lmt.network.NetworkService;
import com.beappclootp.lmt.util.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by lien.muguercia on 31/03/2018.
 */
public class BiClooListFragment extends Fragment {

    private LatLng mUserLatestLocation;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    private BiClooAdapter mAdapter;
    private List<BiClooData> dataList;

    @BindView(android.R.id.list)
    BiClooRecyclerView recyclerView;
    @BindView(R.id.dataEmpty)
    TextView mEmptyList;

    @BindString(R.string.nointernet_explanation_title)
    String textNoInternetTitle;
    @BindString(R.string.nointernet_explanation_action)
    String textNoInternetAction;
    @BindString(R.string.nointernet_explanation)
    String textNoInternet;

    //filter values
    Boolean showAllStationFilter = true; //open or not
    Boolean showAllBicyFilter = true; //available or not
    Boolean showAllParkingFilter = true; //available or not

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bicloo, container, false);
        ButterKnife.bind(this, view);

        mContext = getContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        setLocationCallback();
        getUserLocation();
        initRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.showProgrees(mContext, "Loading...");
        updateList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_search:
                SearchView searchView = (SearchView) item.getActionView();
                searchActiion(searchView);
                break;
            case R.id.toolbar_reload:
                Utils.showProgrees(mContext, "Loading...");
                startLocationUpdates();
                return true;
            case R.id.toolbar_filter:
                showFilterDialog();
                return true;

        }
        return false;
    }

    private void setLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Utils.hideProgrees();
                setLocationFromCallback(locationResult);
                stopLocationUpdates();
            }
        };
    }

    private void initRecyclerView() {
        mAdapter = new BiClooAdapter(getActivity(), dataList);
        recyclerView.setEmptyView(mEmptyList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        dataList = DBManager.getAllBiClooData().list();
    }

    private void getUserLocation() {
        readUserLatestLocationStored();
    }

    private void readUserLatestLocationStored() {
        mUserLatestLocation = Utils.getLocation(mContext) != null ? Utils.getLocation(mContext) : new LatLng(Utils.LATITUD_DEFAULT, Utils.LONGITUD_DEFAULT);
    }

    private void updateList() {
        loadData();
        dataList = Utils.sortFromLocation(mUserLatestLocation, DBManager.getAllBiClooData().list());

        //compute & save distance, set Fav
        for (BiClooData biclo : dataList) {
            LatLng bicloLoc = new LatLng(biclo.getLatitud(), biclo.getLongitud());
            String distance =
                    Utils.formatDistanceBetween(mUserLatestLocation, bicloLoc);
            biclo.setDistance(distance);
            biclo.setFavorite(Utils.isFavorite(mContext, String.valueOf(biclo.getNumber())));
        }

        filterData();
        mAdapter.setDataList(dataList);
        Utils.hideProgrees();
    }

    /*
     *  Location update related methods
     *
     **/
    private void startLocationUpdates() {
        if (Utils.checkFineLocationPermission(getActivity())) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setFastestInterval(0);
            locationRequest.setInterval(0).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void setLocationFromCallback(LocationResult locationResult) {
        if (locationResult == null) {
            Utils.showAlert(mContext, textNoInternetTitle, textNoInternet, textNoInternetAction);
            return;
        }
        for (Location location : locationResult.getLocations()) {
            mUserLatestLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Utils.storeLocation(mContext, mUserLatestLocation);

            if (Utils.isNetworkAvailable(mContext)) {
                getBiClooOnlineData();
            } else {
                Utils.showAlert(mContext, textNoInternetTitle, textNoInternet, textNoInternetAction);
            }
        }
    }

    /*
     *  Webservice related methods
     *
     **/
    private void getBiClooOnlineData() {
        NetworkService networkService = NetServiceClient.setupClient(true).create(NetworkService.class);
        Call<List<BiClooData>> call = networkService.getAllData();
        call.enqueue(new Callback<List<BiClooData>>() {
            @Override
            public void onResponse(@NonNull Call<List<BiClooData>> call, @NonNull Response<List<BiClooData>> response) {
                if (response.body() != null) {
                    saveIntoDB(response.body());
                } else {
                    updateList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BiClooData>> call, @NonNull Throwable t) {
                t.printStackTrace();
                updateList();
            }
        });
    }

    public void saveIntoDB(List<BiClooData> listResponse) {
        DBManager.saveBiClooList(listResponse, mContext);
        updateList();
    }

    /*
     *  Filter dialog manager
     *
     * */
    private void showFilterDialog() {
        LayoutInflater li = LayoutInflater.from(mContext);
        View promptsView = li.inflate(R.layout.layout_filter, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setView(promptsView);
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        ImageView btnCloseDialog = promptsView.findViewById(R.id.btnCloseDialog);
        TextView btnResetDialog = promptsView.findViewById(R.id.btnResetDialog);
        Switch switchOpenStationDialog = promptsView.findViewById(R.id.switchOpenStationDialog);
        final ImageView btnFreeBicyDialog = promptsView.findViewById(R.id.btnFreeBicy);
        final ImageView btnFreeParkingDialog = promptsView.findViewById(R.id.btnFreeParking);
        Button btnConfirmDialog = promptsView.findViewById(R.id.btnConfirmDialog);

        //set values according to previous filters
        //TODO add Fav to Filter | externalise Dialog
        readFilterPreferences();
        setFilterValues(switchOpenStationDialog, btnFreeBicyDialog, btnFreeParkingDialog);

        //actions
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        btnConfirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showProgrees(mContext, "Loading...");
                updateList();
                alertDialog.cancel();
            }
        });
        btnResetDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFilterDefaultValues();
                alertDialog.cancel();
            }
        });
        switchOpenStationDialog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                showAllStationFilter = !isChecked;
                Utils.saveFilterOpen(mContext, showAllStationFilter);

            }
        });

        btnFreeBicyDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterImage(btnFreeBicyDialog, true);

            }
        });

        btnFreeParkingDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFilterImage(btnFreeParkingDialog, false);
            }
        });

        alertDialog.show();
    }

    private void changeFilterImage(ImageView imageView, boolean isBicy) {
        Drawable.ConstantState getIcon = imageView.getDrawable().getConstantState();
        if (isBicy) {
            //filtering free bicy
            Drawable.ConstantState drawable = mContext.getResources().getDrawable(R.drawable.bike_normal).getConstantState();
            if (getIcon == drawable) {
                Picasso.with(mContext)
                        .load(R.drawable.bike_pressed)
                        .into(imageView);
                showAllBicyFilter = false;
            } else {
                Picasso.with(mContext)
                        .load(R.drawable.bike_normal)
                        .into(imageView);
                showAllBicyFilter = true;
            }
            Utils.saveFilterBicy(mContext, showAllBicyFilter);
        } else {
            //filtering free parking
            Drawable.ConstantState drawable = mContext.getResources().getDrawable(R.drawable.parking_normal).getConstantState();
            if (getIcon == drawable) {
                Picasso.with(mContext)
                        .load(R.drawable.parking_pressed)
                        .into(imageView);
                showAllParkingFilter = false;
            } else {
                Picasso.with(mContext)
                        .load(R.drawable.parking_normal)
                        .into(imageView);
                showAllParkingFilter = true;
            }
            Utils.saveFilterParking(mContext, showAllParkingFilter);
        }
    }

    private void setFilterValues(Switch switchOpenStationDialog, ImageView btnFreeBicyDialog, ImageView btnFreeParkingDialog) {
        if (!showAllStationFilter) {
            switchOpenStationDialog.setChecked(true);
        } else switchOpenStationDialog.setChecked(false);

        if (showAllBicyFilter) {
            btnFreeBicyDialog.setImageDrawable(getResources().getDrawable(R.drawable.bike_normal));
        } else {
            btnFreeBicyDialog.setImageDrawable(getResources().getDrawable(R.drawable.bike_pressed));
        }

        if (showAllParkingFilter) {
            btnFreeParkingDialog.setImageDrawable(getResources().getDrawable(R.drawable.parking_normal));
        } else {
            btnFreeParkingDialog.setImageDrawable(getResources().getDrawable(R.drawable.parking_pressed));
        }
    }

    private void readFilterPreferences() {
        showAllStationFilter = Utils.getFilterOpen(mContext);
        showAllBicyFilter = Utils.getFilterBicy(mContext);
        showAllParkingFilter = Utils.getFilterParking(mContext);
    }

    private void setFilterDefaultValues() {
        Utils.saveFilterOpen(mContext, true);
        Utils.saveFilterBicy(mContext, true);
        Utils.saveFilterParking(mContext, true);

        readFilterPreferences();
    }

    private boolean isDefaultValueActive() {
        return showAllStationFilter && showAllBicyFilter && showAllParkingFilter;
    }

    private void filterData() {
        readFilterPreferences();
        if (!isDefaultValueActive()) {
            List<BiClooData> filteredList = new ArrayList<>(dataList);

            for (BiClooData obj : dataList) {
                if (!showAllStationFilter && !showAllBicyFilter && !showAllParkingFilter) {
                    //show only points with status==open && available bicycles && available parking
                    if (!obj.getStatus().equals("OPEN") || obj.getAvailableBikes() == 0 || obj.getAvailableBikeStands() == 0) {
                        filteredList.remove(obj);
                    }
                } else if (!showAllStationFilter && !showAllBicyFilter) {
                    //show only points with status==open && available bicycles && all parking status
                    if (!obj.getStatus().equals("OPEN") || obj.getAvailableBikes() == 0) {
                        filteredList.remove(obj);
                    }
                } else if (!showAllStationFilter && !showAllParkingFilter) {
                    //show only points with status==open && all bicycles status && available parking
                    if (!obj.getStatus().equals("OPEN") || obj.getAvailableBikeStands() == 0) {
                        filteredList.remove(obj);
                    }
                } else if (!showAllBicyFilter && !showAllParkingFilter) {
                    //show all points wheter open or not && available bicycles && available parking
                    if (obj.getAvailableBikes() == 0 || obj.getAvailableBikeStands() == 0) {
                        filteredList.remove(obj);
                    }
                } else if (!showAllBicyFilter) {
                    //show all points wheter open or not && available bicycles && all parking status
                    if (obj.getAvailableBikes() == 0) {
                        filteredList.remove(obj);
                    }
                } else if (!showAllParkingFilter) {
                    //show all points wheter open or not && all bicycles status && available parking
                    if (obj.getAvailableBikeStands() == 0) {
                        filteredList.remove(obj);
                    }
                } else if (!showAllStationFilter) {
                    //show only points with status==open && all bicycles status && all parking status
                    if (!obj.getStatus().equals("OPEN")) {
                        filteredList.remove(obj);
                    }
                }
            }

            Log.v("List Filter", "size " + filteredList.size());
            dataList = filteredList;
        }
    }

    //recycler view filter
    private void searchActiion(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }
}
