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


public class ViewStrategyList extends Activity {

    private Realm realm;
    private RecyclerView recyclerView;
    private Menu menu;
    private ViewStrategyListAdapter adapter;


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
            ViewStrategyListDataHelper.deleteItemAsync(realm, viewHolder.getItemId());
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.strategylist);
        realm = Realm.getDefaultInstance();
        recyclerView = (RecyclerView) findViewById(R.id.strategy_recycler_view);
        Intent intent = getIntent();
        int SymIndex = intent.getIntExtra("SYMBOL_INDEX", 0);
        RealmList sl = realm.where(SymbolList.class).findFirst().getSymbol(SymIndex).getStrategyList();

        adapter = new ViewStrategyListAdapter(sl);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = recyclerView.indexOfChild(v);
                ViewStrategyListAdapter.MyViewHolder holder= (ViewStrategyListAdapter.MyViewHolder) recyclerView.findViewHolderForLayoutPosition(selectedPosition);
                Strategy strat = holder.data;
                // symbol is selected, so start new activity
                Intent it = new Intent(ViewStrategyList.super.getBaseContext(), ViewStrategyList.class);
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
