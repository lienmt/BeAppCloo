package com.beappclootp.lmt.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.beappclootp.lmt.R;
import com.beappclootp.lmt.model.BiClooData;
import com.beappclootp.lmt.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by lien.muguercia on 02/04/2018.
 */
public class BiClooAdapter extends RecyclerView.Adapter<ViewHolder>
        implements ItemClickListener, Filterable {

    private List<BiClooData> mDataList;
    private List<BiClooData> mFilteredDataList;
    private Context mContext;

    public BiClooAdapter(Context context, List<BiClooData> biclodata) {
        super();
        mContext = context;
        mDataList = biclodata;
        mFilteredDataList = biclodata;
    }

    public void setDataList(@NonNull List<BiClooData> dataList) {
        mDataList = dataList;
        mFilteredDataList = dataList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.row_bicloo, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final BiClooData biclodata = mFilteredDataList.get(position);

        String freeBicy = biclodata.getAvailableBikes().toString();
        String freeparking = biclodata.getAvailableBikeStands().toString();
        final int bicloNumber = biclodata.getNumber();

        holder.mTitle.setText(extractName(biclodata.getName()));
        holder.mAddress.setText(biclodata.getAddress());
        holder.mFreeBicy.setText(freeBicy);
        holder.mFreeBicy.setTextColor(getTextColor(freeBicy));
        holder.mFreParking.setText(freeparking);
        holder.mFreParking.setTextColor(getTextColor(freeparking));
        holder.mLastUpdate.setText(setDate(biclodata.getLastUpdate().toString()));
        holder.mDistance.setText(biclodata.getDistance());

        //set fav icon
        if (Utils.isFavorite(mContext, String.valueOf(bicloNumber))) {
            Picasso.with(mContext)
                    .load(R.drawable.heart48)
                    .into(holder.mFavorite);
        } else {
            Picasso.with(mContext)
                    .load(R.drawable.heartoutline48)
                    .into(holder.mFavorite);
        }

        //favorites
        holder.mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isFav = Utils.isFavorite(mContext, String.valueOf(bicloNumber));
                if (!isFav) {
                    Picasso.with(mContext)
                            .load(R.drawable.heart48)
                            .into(holder.mFavorite);
                    Utils.addFavorite(mContext, String.valueOf(bicloNumber));
                    //Snackbar.make(parent_view, attraction + " " + getString(R.string.add_favr), Snackbar.LENGTH_SHORT).show();
                } else {
                    Picasso.with(mContext)
                            .load(R.drawable.heartoutline48)
                            .into(holder.mFavorite);
                    Utils.removeFavorite(mContext, String.valueOf(bicloNumber));
                    //Snackbar.make(parent_view, attraction + " " + getString(R.string.add_favr), Snackbar.LENGTH_SHORT).show();
                }


            }
        });

        holder.mNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openInMapsApp(biclodata, mContext);
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

    private String extractName(String name) {
        if (name.contains("-")) {
            int index = name.indexOf("-");
            return name.substring(index + 1);
        } else
            return name;
    }

    private String setDate(String time) {
        Calendar cal = Calendar.getInstance(Locale.FRANCE);
        cal.setTimeInMillis(Long.parseLong(time));

        String date = DateFormat.format("HH:mm", cal).toString();
        if (cal.before(new Date())){
            //for data saved previously today
            date = DateFormat.format("dd/MMM HH:mm", cal).toString();
        }
        return date;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mFilteredDataList == null ? 0 : mFilteredDataList.size();
    }

    @Override
    public void onItemClick(View view, int position) {

        Log.v("Adapter", "position " + position);
        //click in Adapter ???
    }

    @Override
    public Filter getFilter() {
        //https://www.learn2crack.com/2017/03/searchview-with-recyclerview.html
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();

                if (charString.isEmpty()) {
                    mFilteredDataList = mDataList;
                } else {

                    ArrayList<BiClooData> filteredList = new ArrayList<>();
                    for (BiClooData data : mDataList) {
                        if (data.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(data);
                        }
                    }

                    mFilteredDataList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredDataList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredDataList = (ArrayList<BiClooData>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}

class ViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

    TextView mTitle;
    TextView mAddress;
    TextView mFreeBicy;
    TextView mFreParking;
    TextView mDistance;
    TextView mLastUpdate;
    ImageView mFavorite;
    ImageView mNavigate;
    private ItemClickListener mItemClickListener;

    public ViewHolder(View view, ItemClickListener itemClickListener) {
        super(view);
        mTitle = view.findViewById(R.id.textTitle);
        mAddress = view.findViewById(R.id.textAddress);
        mFreeBicy = view.findViewById(R.id.textBicy);
        mFreParking = view.findViewById(R.id.textParking);
        mDistance = view.findViewById(R.id.textDistance);
        mLastUpdate = view.findViewById(R.id.textLastUpdate);
        mFavorite = view.findViewById(R.id.imgFav);
        mNavigate = view.findViewById(R.id.imgNavigate);

        mItemClickListener = itemClickListener;
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mItemClickListener.onItemClick(v, getAdapterPosition());
    }
}

interface ItemClickListener {
    void onItemClick(View view, int position);
}



