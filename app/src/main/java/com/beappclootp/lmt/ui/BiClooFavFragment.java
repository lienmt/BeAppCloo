package com.beappclootp.lmt.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beappclootp.lmt.R;
import com.beappclootp.lmt.adapter.BiClooAdapter;
import com.beappclootp.lmt.adapter.BiClooRecyclerView;
import com.beappclootp.lmt.database.DBManager;
import com.beappclootp.lmt.model.BiClooData;
import com.beappclootp.lmt.util.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lien.muguercia on 31/03/2018.
 */
public class BiClooFavFragment extends Fragment {

    private LatLng mUserLatestLocation;

    private BiClooAdapter mAdapter;
    private List<BiClooData> dataList;

    @BindView(android.R.id.list) BiClooRecyclerView recyclerView;
    @BindView(R.id.dataEmpty) TextView mEmptyList;

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fav, container, false);
        ButterKnife.bind(this, view);

        mContext = getContext();

        getUserLocation();
        initRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }


    private void initRecyclerView(){
        mAdapter = new BiClooAdapter(getActivity(), dataList);
        recyclerView.setEmptyView(mEmptyList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
    }

    private void loadData() {
        dataList = new ArrayList<>();
    }

    private void getUserLocation() {
        mUserLatestLocation = readUserLatestLocationStored();
    }

    private LatLng readUserLatestLocationStored() {
        return Utils.getLocation(mContext) != null ? Utils.getLocation(mContext) : new LatLng(Utils.LATITUD_DEFAULT, Utils.LONGITUD_DEFAULT);
    }

    private void updateList(){
        loadData();
        List<BiClooData> list = Utils.sortFromLocation(mUserLatestLocation, DBManager.getAllBiClooData().list());

        //compute & save distance, set Fav
        for (BiClooData biclo: list){
            boolean isFav = Utils.isFavorite(mContext, String.valueOf(biclo.getNumber()));
            LatLng bicloLoc = new LatLng(biclo.getLatitud(), biclo.getLongitud());
            String distance =
                    Utils.formatDistanceBetween(mUserLatestLocation, bicloLoc);
            biclo.setDistance(distance);
            biclo.setFavorite(isFav);

            if (isFav){
                dataList.add(biclo);
            }
        }

        mAdapter.setDataList(dataList);
        Utils.hideProgrees();
    }
}
