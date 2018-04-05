package com.beappclootp.lmt.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.beappclootp.lmt.R;
import com.beappclootp.lmt.database.DBManager;
import com.beappclootp.lmt.model.BiClooData;
import com.beappclootp.lmt.model.directions.DirectionResults;
import com.beappclootp.lmt.model.directions.Route;
import com.beappclootp.lmt.network.NetServiceClient;
import com.beappclootp.lmt.network.NetworkService;
import com.beappclootp.lmt.util.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by lien.muguercia on 02/04/2018.
 */
public class BiClooMapFragment extends Fragment implements OnMapReadyCallback {

    private Context mContext;

    private MapFragment mapFragment;

    private GoogleMap mMap;
    private ArrayList<Marker> mMarkerArray = new ArrayList<>();
    private ArrayList<Marker> mMarkerSearchArray = new ArrayList<>();

    private LatLng mLatestLocation;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    //no internet string
    @BindString(R.string.nointernet_explanation_title)
    String textNoInternetTitle;
    @BindString(R.string.nointernet_explanation_action)
    String textNoInternetAction;
    @BindString(R.string.nointernet_explanation)
    String textNoInternet;

    //address error string
    @BindString(R.string.title_address_title)
    String textAddressTitle;
    @BindString(R.string.title_address_msg)
    String textAddressMsg;
    @BindString(R.string.title_address_action)
    String textAddressAction;

    @BindString(R.string.text_closed_station)
    String textClosedStation;

    private List<BiClooData> dataList;

    //bottom sheet layout
    @BindView(R.id.llBottomSheet)
    LinearLayout layoutBottomSheet;
    private BottomSheetBehavior sheetBehavior;

    //bottom sheet components
    @BindView(R.id.btnNavigate)
    ImageButton btnNavigate;
    @BindView(R.id.textTitle)
    TextView bicloName;
    @BindView(R.id.textAddress)
    TextView bicloAddress;
    @BindView(R.id.textBicy)
    TextView bicloFreeBicy;
    @BindView(R.id.textParking)
    TextView bicloFreeParking;
    @BindView(R.id.textDistance)
    TextView bicloDistance;
    @BindView(R.id.textLastUpdate)
    TextView bicloLastUpdate;
    @BindView(R.id.textExtra)
    TextView bicloExtraInfo;
    @BindView(R.id.imgFav)
    ImageView bicloFav;

    //directions
    @BindView(R.id.btnDirections)
    FloatingActionButton btnDirections;

    @BindView(R.id.llRouteInfo)
    LinearLayout holderRouteInfo;
    @BindView(R.id.textRouteDistance)
    TextView textRouteDistance;
    @BindView(R.id.textRouteTime)
    TextView textRouteTime;

    private Polyline mPolylineRoute;

    private BiClooData biClooObj;

    //filter values
    Boolean showAllStationFilter = true; //open or not
    Boolean showAllBicyFilter = true; //available or not
    Boolean showAllParkingFilter = true; //available or not

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, null);
        ButterKnife.bind(this, view);

        mContext = getContext();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        setLocationCallback();
        getUserLocation();
        initBottomSheet();
        initDirectionsView();
        setupMap();

        return view;
    }


    @Override
    public void onDestroyView() {
        if (!Utils.detailBackPressed) {
            if (mapFragment != null)
                getActivity().getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
        Utils.detailBackPressed = false;

        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.toolbar_search).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_reload:
                Utils.showProgrees(mContext, "Loading...");
                startLocationUpdates();
                return true;
            case R.id.toolbar_filter:
                showFilterDialog();
                Log.v("Map", "filter");
                return true;

        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        initMap();
    }

    private void initBottomSheet() {
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        btnDirections.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        //hide Floating Button
                        btnDirections.setVisibility(View.INVISIBLE);
                        btnNavigate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (biClooObj.getStatus().equals("OPEN")) {
                                    //Utils.openInMapsApp(biClooObj, mContext);

                                    Utils.showProgrees(mContext, "Loading...");
                                    String origin = getLatLngString(mLatestLocation);
                                    String destination = getLatLngString(biClooObj.getLatitud(), biClooObj.getLongitud());
                                    getGoogleDirections(origin, destination, false, true);

                                }/*else {
                                    //nothing happen
                                }*/
                            }
                        });

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //deselect marker
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private String getLatLngString(LatLng position) {
        return position.latitude + "," + position.longitude;
    }

    private String getLatLngString(Double lat, Double lon) {
        return lat + "," + lon;
    }

    private void getUserLocation() {
        mLatestLocation = readUserLatestLocationStored();
    }

    private LatLng readUserLatestLocationStored() {
        return Utils.getLocation(mContext) != null ? Utils.getLocation(mContext) : new LatLng(Utils.LATITUD_DEFAULT, Utils.LONGITUD_DEFAULT);
    }

    private void setLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    Utils.hideProgrees();
                    stopLocationUpdates();
                    Utils.showAlert(mContext, textNoInternetTitle, textNoInternet, textNoInternetAction);
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    mLatestLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    Utils.storeLocation(mContext, mLatestLocation);

                    if (Utils.isNetworkAvailable(mContext)) {
                        getBiClooOnlineData();
                    } else {
                        Utils.hideProgrees();
                        stopLocationUpdates();
                        Utils.showAlert(mContext, textNoInternetTitle, textNoInternet, textNoInternetAction);
                    }
                }

            }
        };
    }

    private void setupMap() {
        if (mapFragment == null) {
            mapFragment = (MapFragment) getActivity().getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private void initDirectionsView() {
        btnDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                erasePolylineRoute();
                showDirectionDialog();
            }
        });
    }

    private void erasePolylineRoute() {
        if (mPolylineRoute != null) {
            mPolylineRoute.remove();
        }
        holderRouteInfo.setVisibility(View.INVISIBLE);

        for (Marker item : mMarkerSearchArray) {
            item.setVisible(false);
        }
        mMarkerSearchArray.clear();
    }

    private void onMarkerClick() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String bicloNumber = (String) marker.getTag();
                biClooObj = findBiClooByNumber(bicloNumber);

                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                erasePolylineRoute();

                for (Marker item : mMarkerArray) {
                    item.setVisible(true);
                }
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                holderRouteInfo.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void onWindowClick() {
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final String bicloNumber = (String) marker.getTag();
                biClooObj = findBiClooByNumber(bicloNumber);

                if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    if (biClooObj != null) {
                        String freeBicy = biClooObj.getAvailableBikes().toString();
                        String freeParking = biClooObj.getAvailableBikeStands().toString();

                        bicloName.setText(extractName(biClooObj.getName()));
                        bicloAddress.setText(biClooObj.getAddress());
                        bicloFreeBicy.setText(freeBicy);
                        bicloFreeBicy.setTextColor(getTextColor(freeBicy));
                        bicloFreeParking.setText(freeParking);
                        bicloFreeParking.setTextColor(getTextColor(freeParking));
                        bicloDistance.setText(biClooObj.getDistance());
                        bicloLastUpdate.setText(setDate(biClooObj.getLastUpdate().toString()));

                        //extra info
                        if (!biClooObj.getStatus().equals("OPEN")) {
                            bicloExtraInfo.setVisibility(View.VISIBLE);
                            bicloExtraInfo.setText(textClosedStation);
                            btnNavigate.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
                        } else {
                            bicloExtraInfo.setVisibility(View.INVISIBLE);
                            btnNavigate.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        }

                        //set fav icon
                        if (Utils.isFavorite(mContext, String.valueOf(bicloNumber))) {
                            Picasso.with(mContext)
                                    .load(R.drawable.heart48)
                                    .into(bicloFav);
                        } else {
                            Picasso.with(mContext)
                                    .load(R.drawable.heartoutline48)
                                    .into(bicloFav);
                        }

                        //add & remove from fav
                        bicloFav.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean isFav = Utils.isFavorite(mContext, String.valueOf(bicloNumber));
                                if (!isFav) {
                                    Picasso.with(mContext)
                                            .load(R.drawable.heart48)
                                            .into(bicloFav);
                                    Utils.addFavorite(mContext, String.valueOf(bicloNumber));
                                    //Snackbar.make(parent_view, attraction + " " + getString(R.string.add_favr), Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Picasso.with(mContext)
                                            .load(R.drawable.heartoutline48)
                                            .into(bicloFav);
                                    Utils.removeFavorite(mContext, String.valueOf(bicloNumber));
                                    //Snackbar.make(parent_view, attraction + " " + getString(R.string.add_favr), Snackbar.LENGTH_SHORT).show();
                                }


                            }
                        });
                    }


                } else {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });
    }

    private int getTextColor(String text) {
        int color;
        switch (text) {
            case "0":
                color = mContext.getResources().getColor(android.R.color.holo_red_dark);
                break;
            case "1":
                color = mContext.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "2":
                color = mContext.getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "3":
                color = mContext.getResources().getColor(android.R.color.holo_orange_dark);
                break;

            default:
                color = mContext.getResources().getColor(android.R.color.holo_green_dark);
                break;
        }

        return color;
    }

    private String setDate(String time) {
        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.setTimeInMillis(Long.parseLong(time));
        return DateFormat.format("HH:mm", cal).toString();
    }

    private void loadData() {
        //init data list
        dataList = DBManager.getAllBiClooData().list();
    }

    private void filterData() {
        readFilterPreferences();
        if (!isDefaultValueActive()) {

            for (Marker marker : mMarkerArray) {
                String bicloNumber = (String) marker.getTag();
                BiClooData obj = findBiClooByNumber(bicloNumber);
                marker.setVisible(true);

                if (obj != null) {
                    if (!showAllStationFilter && !showAllBicyFilter && !showAllParkingFilter) {
                        //show only points with status==open && available bicycles && available parking
                        if (!obj.getStatus().equals("OPEN") || obj.getAvailableBikes() == 0 || obj.getAvailableBikeStands() == 0) {
                            marker.setVisible(false);
                        }
                    } else if (!showAllStationFilter && !showAllBicyFilter) {
                        //show only points with status==open && available bicycles && all parking status
                        if (!obj.getStatus().equals("OPEN") || obj.getAvailableBikes() == 0) {
                            marker.setVisible(false);
                        }
                    } else if (!showAllStationFilter && !showAllParkingFilter) {
                        //show only points with status==open && all bicycles status && available parking
                        if (!obj.getStatus().equals("OPEN") || obj.getAvailableBikeStands() == 0) {
                            marker.setVisible(false);
                        }
                    } else if (!showAllBicyFilter && !showAllParkingFilter) {
                        //show all points wheter open or not && available bicycles && available parking
                        if (obj.getAvailableBikes() == 0 || obj.getAvailableBikeStands() == 0) {
                            marker.setVisible(false);
                        }
                    } else if (!showAllBicyFilter) {
                        //show all points wheter open or not && available bicycles && all parking status
                        if (obj.getAvailableBikes() == 0) {
                            marker.setVisible(false);
                        }
                    } else if (!showAllParkingFilter) {
                        //show all points wheter open or not && all bicycles status && available parking
                        if (obj.getAvailableBikeStands() == 0) {
                            marker.setVisible(false);
                        }
                    } else if (!showAllStationFilter) {
                        //show only points with status==open && all bicycles status && all parking status
                        if (!obj.getStatus().equals("OPEN")) {
                            marker.setVisible(false);
                        }
                    } else {
                        marker.setVisible(true);
                    }
                }

            }

            Log.v("Map Filter", "size " + mMarkerArray.size());

        } else {
            //show all marker on Map
            for (Marker marker : mMarkerArray) {
                marker.setVisible(true);
            }
        }

        Utils.hideProgrees();
    }

    private void centerCamera() {
        //Move the camera
        if (mLatestLocation != null) {
            // set true user location
            LatLng userPos = new LatLng(mLatestLocation.latitude, mLatestLocation.longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPos, 15));

            if (Utils.checkFineLocationPermission(getActivity())) {
                mMap.setMyLocationEnabled(true);
            }

        } else {
            //Nantes Commerce
            LatLng marker = new LatLng(47.2172628, -1.5500204);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 12));
        }
    }

    private void setMarkers() {
        if (dataList != null && dataList.size() > 0) {
            BitmapDescriptor markerColor;
            for (BiClooData data : dataList) {

                int numberBicy = data.getAvailableBikes();
                if (numberBicy == 0) {
                    markerColor = BitmapDescriptorFactory.fromResource(R.drawable.marker03_red);
                } else if (numberBicy == 1 || numberBicy == 2 || numberBicy == 3) {
                    markerColor = BitmapDescriptorFactory.fromResource(R.drawable.marker03_orange);
                } else {
                    markerColor = BitmapDescriptorFactory.fromResource(R.drawable.marker03_green);
                }

                LatLng coord = new LatLng(data.getLatitud(), data.getLongitud());
                String descBicy = (numberBicy == 0) ? "aucun velo disponible" : data.getAvailableBikes() + " velos disponibles";

                Marker mMarker = mMap.addMarker(new MarkerOptions()
                        .position(coord)
                        .title(extractName(data.getName()))
                        .snippet(descBicy + " à " + data.getDistance())
                        .icon(markerColor)
                );
                mMarker.setTag(data.getNumber().toString());
                mMarkerArray.add(mMarker);
            }

        }
    }

    private void initMap() {
        loadData();
        centerCamera();
        setMarkers();
        filterData();
        onMarkerClick();
        onWindowClick();
    }

    private void updateMap() {
        mMap.clear();
        mMarkerArray.clear();
        loadData();
        setMarkers();
        filterData();
        centerCamera();

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

    /*
     *  Webservice related methods
     *
     **/
    private void getBiClooOnlineData() {
        NetworkService networkService = NetServiceClient.setupClient(true).create(NetworkService.class);
        Call<List<BiClooData>> call = networkService.getAllData();
        call.enqueue(new Callback<List<BiClooData>>() {
            @Override
            public void onResponse(@NotNull Call<List<BiClooData>> call, @NotNull Response<List<BiClooData>> response) {
                if (response.body() != null) {
                    saveIntoDB(response.body());
                }
                stopLocationUpdates();
            }

            @Override
            public void onFailure(@NotNull Call<List<BiClooData>> call, @NotNull Throwable t) {
                t.printStackTrace();

                Utils.hideProgrees();
                stopLocationUpdates();
            }
        });
    }

    public void saveIntoDB(List<BiClooData> listResponse) {
        DBManager.saveBiClooList(listResponse, mContext);
        updateMap();
    }

    /*
     *  General methods
     *
     **/
    private String extractName(String name) {
        if (name.contains("-")) {
            int index = name.indexOf("-");
            return name.substring(index + 1);
        } else
            return name;
    }

    private BiClooData findBiClooByNumber(String number) {
        if (dataList != null && !number.equals("-1")) {
            for (BiClooData biclo : dataList) {
                if (biclo.getNumber() == Integer.parseInt(number)) {
                    return biclo;
                }
            }
        }
        return null;
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
                filterData();
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

    /*
     *  Direction dialog manager
     *
     * */
    private void showDirectionDialog() {
        LayoutInflater li = LayoutInflater.from(mContext);
        View promptsView = li.inflate(R.layout.layout_directions, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setView(promptsView);
        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        ImageView btnCloseDialog = promptsView.findViewById(R.id.btnCloseDialog);
        Button btnConfirmDialog = promptsView.findViewById(R.id.btnConfirmDialog);
        final EditText textOrigin = promptsView.findViewById(R.id.textOrigin);
        final EditText textDestination = promptsView.findViewById(R.id.textDestination);

        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        btnConfirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addressFrom = textOrigin.getText().toString();
                String addressTo = textDestination.getText().toString();

                //TODO improve user address typing
                Utils.showProgrees(mContext, "Loading...");

                //getting coord
                if (!addressFrom.isEmpty() && !addressTo.isEmpty()) {
                    LatLng originUser = getCoordFromAddress(addressFrom);
                    LatLng destUser = getCoordFromAddress(addressTo);

                    //looking for nearest station & check availability
                    if (originUser != null && destUser != null) {
                        originUser = getNearestStation(originUser, true);
                        destUser = getNearestStation(destUser, false);

                        //draw route
                        if (originUser != null && destUser != null) {
                            String origin = getLatLngString(originUser);
                            String dest = getLatLngString(destUser);

                            getGoogleDirections(origin, dest, true, false);
                        }

                    } else {
                        Utils.hideProgrees();
                        Utils.showAlert(mContext, textAddressTitle, textAddressMsg, textAddressAction);
                    }

                } else {
                    Utils.hideProgrees();
                    Utils.showAlert(mContext, textAddressTitle, textAddressMsg, textAddressAction);
                }

                alertDialog.cancel();
            }
        });


        alertDialog.show();
    }

    private LatLng getCoordFromAddress(String value) {
        try {
            Geocoder geocoder = new Geocoder(mContext);
            List<Address> addresses;
            addresses = geocoder.getFromLocationName(value, 1);
            if (addresses.size() > 0) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();

                return new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private LatLng getNearestStation(LatLng position, boolean origin) {

        List<BiClooData> list = Utils.sortFromLocation(position, DBManager.getAllBiClooData().list());

        for (BiClooData data : list) {
            if (origin) {
                if (data.getAvailableBikes() > 0) {
                    return new LatLng(data.getLatitud(), data.getLongitud());
                }
            } else {
                if (data.getAvailableBikeStands() > 0) {
                    return new LatLng(data.getLatitud(), data.getLongitud());
                }
            }
        }
        return null;
    }

    private void getGoogleDirections(String origin, String destination, final boolean hideMarkers, boolean walking) {
        //TODO let user select transport mode
        NetworkService networkService = NetServiceClient.setupClient(false).create(NetworkService.class);
        final String mode = walking ? "walking" : "bicycling";
        Call<DirectionResults> call = networkService.getGoogleDirections(origin, destination, mode);
        call.enqueue(new Callback<DirectionResults>() {
            @Override
            public void onResponse(@NotNull Call<DirectionResults> call, @NotNull Response<DirectionResults> response) {
                if (response.body() != null) {
                    DirectionResults directionResults = response.body();
                    ArrayList<LatLng> routelist = new ArrayList<>();

                    if (directionResults != null && directionResults.getRoutes().size() > 0) {
                        Route routeA = directionResults.getRoutes().get(0);
                        routelist.addAll(PolyUtil.decode(routeA.getOverviewPolyline().getPoints()));

                        textRouteDistance.setText(routeA.getLegs().get(0).getDistance().getText());
                        textRouteTime.setText(routeA.getLegs().get(0).getDuration().getText() + " by "+mode);
                    }
                    if (routelist.size() > 0) {
                        PolylineOptions rectLine = new PolylineOptions()
                                .width(10)
                                .color(Color.RED)
                                .addAll(routelist);

                        mPolylineRoute = mMap.addPolyline(rectLine);
                        holderRouteInfo.setVisibility(View.VISIBLE);

                        if (hideMarkers) {
                            mMarkerSearchArray.clear();
                            for (Marker marker : mMarkerArray) {
                                marker.setVisible(false);
                            }
                            BitmapDescriptor markerColor = BitmapDescriptorFactory.fromResource(R.drawable.marker03_green);
                            //set new markers for user research
                            Marker origin = mMap.addMarker(new MarkerOptions()
                                    .position(routelist.get(0))
                                    .icon(markerColor)
                            );
                            origin.setTag("-1");

                            Marker dest = mMap.addMarker(new MarkerOptions()
                                    .position(routelist.get(routelist.size() - 1))
                                    .icon(markerColor)
                            );
                            dest.setTag("-1");

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routelist.get(0), 15));
                            mMarkerSearchArray.add(origin);
                            mMarkerSearchArray.add(dest);
                        }
                    }
                }
                Utils.hideProgrees();
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }

            @Override
            public void onFailure(@NotNull Call<DirectionResults> call, @NotNull Throwable t) {
                t.printStackTrace();
                Utils.hideProgrees();
            }
        });
    }

}
