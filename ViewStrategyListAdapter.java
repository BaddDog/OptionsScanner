package com.baddog.optionsscanner;

/**
 * Created by Brian on 2018-03-08.
 */

import android.icu.text.DecimalFormat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;


class ViewStrategyListAdapter extends RealmRecyclerViewAdapter<Strategy, ViewStrategyListAdapter.MyViewHolder> {

    protected View.OnClickListener mClickListener;
    private Realm realm;

    ViewStrategyListAdapter(Realm realm, RealmResults<Strategy> data) {
        super((OrderedRealmCollection<Strategy>) data, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#hasStableIds()
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#getItemId(int)
        setHasStableIds(false);
        this.realm = realm;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewstrategylistrow, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClick(view);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Strategy obj = getItem(position);
        holder.data = obj;

        holder.title2.setText(Long.toString(obj.getDaysTillExpiration(realm)));
        holder.title.setText(Integer.toString((int)obj.getScore()));
        holder.title3.setText(Double.toString(obj.getCallPremium()));
        holder.title4.setText(Double.toString(obj.getPutPremium()));
        holder.title5.setText(Double.toString(obj.getCallStrikeprice()));
        holder.title6.setText(Double.toString(obj.getPutStrikeprice()));
        //double Volatility = obj.getCallOption().getExpirationDateObject().getUnderlyingSymbolObject().getVolatility(obj.getDaysTillExpiration(realm));
        holder.title7.setText(Integer.toString((int)obj.getScore2()));
        holder.title8.setText(obj.getCallOption().getExpirationDateObject().getUnderlyingSymbolObject().getSymbol());
        holder.title9.setText(String.format("%d", obj.getCallOption().getOpenInterest() ));
        holder.title10.setText(String.format("%d", obj.getPutOption().getOpenInterest() ));
        holder.title11.setText(Double.toString(obj.getCallOption().getExpirationDateObject().getUnderlyingSymbolObject().getLastTradePrice()));
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getid();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, title2, title3, title4, title5, title6, title7, title8, title9, title10, title11;
        CheckBox deletedCheckBox;
        public Strategy data;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.score);
            title2 = (TextView) view.findViewById(R.id.daystillexpiry);
            title3 = (TextView) view.findViewById(R.id.callpremium);
            title4 = (TextView) view.findViewById(R.id.putpremium);
            title5 = (TextView) view.findViewById(R.id.callstrikeprice);
            title6 = (TextView) view.findViewById(R.id.putstrikeprice);
            title7 = (TextView) view.findViewById(R.id.score2);
            title8 = (TextView) view.findViewById(R.id.symbol);
            title9 = (TextView) view.findViewById(R.id.CallOpenInterest);
            title10 = (TextView) view.findViewById(R.id.PutOpenInterest);
            title11 = (TextView) view.findViewById(R.id.underlyingPrice);
        }
    }


    public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }

}

