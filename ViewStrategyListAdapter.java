package com.baddog.optionsscanner;

/**
 * Created by Brian on 2018-03-08.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;


class ViewStrategyListAdapter extends RealmRecyclerViewAdapter<Strategy, ViewStrategyListAdapter.MyViewHolder> {

    protected View.OnClickListener mClickListener;


    ViewStrategyListAdapter(RealmList<Strategy> data) {
        super((OrderedRealmCollection<Strategy>) data, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#hasStableIds()
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#getItemId(int)
        setHasStableIds(false);
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

        //noinspection ConstantConditions

        holder.title2.setText(Long.toString(obj.getDaysTillExpiration()));
        holder.title.setText(Integer.toString((int)obj.getCallOption().getScore()));
        holder.title1.setText(Integer.toString((int)obj.getPutOption().getScore()));
        holder.title3.setText(Double.toString(obj.getCallPremium()));
        holder.title4.setText(Double.toString(obj.getPutPremium()));
        holder.title5.setText(Double.toString(obj.getCallStrikeprice()));
        holder.title6.setText(Double.toString(obj.getPutStrikeprice()));
        double Volatility = obj.getCallOption().getExpirationDateObject().getUnderlyingSymbolObject().getVolatility(obj.getDaysTillExpiration());
        holder.title7.setText(String.format("%.2f", Volatility ));
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getid();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, title1, title2, title3, title4, title5, title6, title7;
        CheckBox deletedCheckBox;
        public Strategy data;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.callscore);
            title1 = (TextView) view.findViewById(R.id.putscore);
            title2 = (TextView) view.findViewById(R.id.daystillexpiry);
            title3 = (TextView) view.findViewById(R.id.callpremium);
            title4 = (TextView) view.findViewById(R.id.putpremium);
            title5 = (TextView) view.findViewById(R.id.callstrikeprice);
            title6 = (TextView) view.findViewById(R.id.putstrikeprice);
            title7 = (TextView) view.findViewById(R.id.volatility);
        }
    }


    public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }

}

