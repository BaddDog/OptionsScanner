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
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;


class ViewSymbolListAdapter extends RealmRecyclerViewAdapter<Symbols, ViewSymbolListAdapter.MyViewHolder> {

    protected View.OnClickListener mClickListener;
    private Realm realm;

    ViewSymbolListAdapter(RealmList<Symbols> data) {
        super((OrderedRealmCollection<Symbols>) data, true);
        // Only set this if the model class has a primary key that is also a integer or long.
        // In that case, {@code getItemId(int)} must also be overridden to return the key.
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#hasStableIds()
        // See https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#getItemId(int)
        setHasStableIds(false);
    }

    ViewSymbolListAdapter(Realm realm, RealmResults<Symbols> data) {
        super((OrderedRealmCollection<Symbols>) data, true);
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
                .inflate(R.layout.viewsymbollistrow, parent, false);
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
        final Symbols obj = getItem(position);
        holder.data = obj;

        //noinspection ConstantConditions
        holder.title.setText(obj.getSymbol());
        holder.title2.setText(Double.toString(obj.getLastTradePrice()));
        holder.title3.setText(Integer.toString(obj.getBestScore()));
    }

    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItem(index).getSymbolID();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, title2, title3;
        CheckBox deletedCheckBox;
        public Symbols data;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.textview);
            title2 = (TextView) view.findViewById(R.id.textview2);
            title3 = (TextView) view.findViewById(R.id.textview3);
        }
    }


    public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }






}


