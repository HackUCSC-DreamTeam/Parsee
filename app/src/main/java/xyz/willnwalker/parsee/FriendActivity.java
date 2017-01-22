package xyz.willnwalker.parsee;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by William on 1/22/2017.
 */

public class FriendActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Query query;
    private FriendAdapter adapter;
    private ArrayList<String> adapterItems;
    private ArrayList<String> adapterKeys;
    private FirebaseUser auth = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference("USERS");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        query = db.child(auth.getUid())
                .child("friends").orderByKey();

        recyclerView = (RecyclerView) findViewById(R.id.friendList);
        adapter = new FriendAdapter(query, String.class, adapterItems, adapterKeys);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public void confirmDelete(final String s){
        new MaterialDialog.Builder(this).title("Delete Friend").content("Are you sure you want to delete this friend?").positiveText("OK").negativeText("Cancel").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            if(s.equals(postSnapshot.getValue())){
                                postSnapshot.getRef().setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                FirebaseDatabase.getInstance().getReference("REVERSE_USERS").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot!=null){
                            Log.d("parsee.friends",dataSnapshot.toString());
                            db.child((String)dataSnapshot.getValue()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                        if(auth.getUid().equals(postSnapshot.getKey())){
                                            postSnapshot.getRef().setValue(null);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }).show();
    }

    private class FriendAdapter extends FirebaseRecyclerAdapter<FriendAdapter.ViewHolder, String> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            public FrameLayout container;
            public TextView textView;
            public ViewHolder(FrameLayout container) {
                super(container);
                this.container=container;
                textView = (TextView) container.findViewById(R.id.textView);
            }
        }

        public FriendAdapter(Query query, Class<String> itemClass, @Nullable ArrayList<String> items,
                             @Nullable ArrayList<String> keys) {
            super(query, itemClass, items, keys);
        }

        @Override public FriendAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            FriendAdapter.ViewHolder vh = new FriendAdapter.ViewHolder((FrameLayout)v);
            return vh;
        }

        @Override public void onBindViewHolder(FriendAdapter.ViewHolder holder, int position) {
            final String item = getItem(position);
            holder.textView.setText(item);
            holder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    confirmDelete(item);
                    return true;
                }
            });
        }

        @Override protected void itemAdded(String item, String key, int position) {
            Log.d("MyAdapter", "Added a new item to the adapter.");
        }

        @Override protected void itemChanged(String oldItem, String newItem, String key, int position) {
            Log.d("MyAdapter", "Changed an item.");
        }

        @Override protected void itemRemoved(String item, String key, int position) {
            Log.d("MyAdapter", "Removed an item from the adapter.");
        }

        @Override protected void itemMoved(String item, String key, int oldPosition, int newPosition) {
            Log.d("MyAdapter", "Moved an item.");
        }
    }
}
