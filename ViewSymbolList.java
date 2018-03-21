package com.baddog.optionsscanner;

/**
 * Created by Brian on 2018-03-10.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.security.acl.Owner;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;


public class ViewSymbolList extends Activity {

    private Realm realm;
    private RecyclerView recyclerView;
    private Menu menu;
    private ViewSymbolListAdapter adapter;


    private class TouchHelperCallback extends ItemTouchHelper.SimpleCallback {

        TouchHelperCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            ViewSymbolListDataHelper.deleteItemAsync(realm, viewHolder.getItemId());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symbollist);
        realm = Realm.getDefaultInstance();
        recyclerView = (RecyclerView) findViewById(R.id.symbol_recycler_view);
        RealmList sl = realm.where(SymbolList.class).findFirst().getSymbolsList();
        adapter = new ViewSymbolListAdapter(sl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = recyclerView.indexOfChild(v);
                ViewSymbolListAdapter.MyViewHolder holder= (ViewSymbolListAdapter.MyViewHolder) recyclerView.findViewHolderForLayoutPosition(selectedPosition);
                // symbol is selected, so start new activity
                Intent it = new Intent(ViewSymbolList.this, ViewStrategyList.class);
                it.putExtra("SYMBOL_INDEX", selectedPosition);
                startActivity(it);
            }
        };

        adapter.setClickListener(listener);

        TouchHelperCallback touchHelperCallback = new TouchHelperCallback();
        ItemTouchHelper touchHelper = new ItemTouchHelper(touchHelperCallback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    /*
     * It is good practice to null the reference from the view to the adapter when it is no longer needed.
     * Because the <code>RealmRecyclerViewAdapter</code> registers itself as a <code>RealmResult.ChangeListener</code>
     * the view may still be reachable if anybody is still holding a reference to the <code>RealmResult>.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        realm.close();

    }





}
