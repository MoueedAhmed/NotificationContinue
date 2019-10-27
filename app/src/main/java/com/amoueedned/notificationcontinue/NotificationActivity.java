package com.amoueedned.notificationcontinue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.amoueedned.notificationcontinue.adapter.LocalNotificationDataAdapter;
import com.amoueedned.notificationcontinue.database.AppDatabase;
import com.amoueedned.notificationcontinue.database.AppExecutors;
import com.amoueedned.notificationcontinue.database.LocalNotificationDataEntry;
import com.amoueedned.notificationcontinue.database.LocalNotificationViewModel;

import java.util.Date;
import java.util.List;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;

public class NotificationActivity extends AppCompatActivity implements LocalNotificationDataAdapter.ItemClickListener {

    private RecyclerView mRecyclerView;
    private LocalNotificationDataAdapter mAdapter;
    private AppDatabase mDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mDb = AppDatabase.getInstance(getApplicationContext());

        addLocalNotificationDataEntryToDB();

        mRecyclerView = findViewById(R.id.recyclerViewLocalNotificationData);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new LocalNotificationDataAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<LocalNotificationDataEntry> tasks = mAdapter.getEntries();
                        //Call deleteTask in the taskDao with the task at that position
                        mDb.localNotificationDataDao().deleteEntry(tasks.get(position));
                        //Call retrieveTasks method to refresh the UI
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        setupLocalNotificationViewModel();
    }


    private void setupLocalNotificationViewModel() {
        //read from db and display on RecyclerView
        LocalNotificationViewModel viewModel = ViewModelProviders.of(this).get(LocalNotificationViewModel.class);

        viewModel.getEntries().observe(this, new Observer<List<LocalNotificationDataEntry>>() {
            @Override
            public void onChanged(List<LocalNotificationDataEntry> localNotificationDataEntries) {
                mAdapter.setEntries(localNotificationDataEntries);
            }
        });


    }

    //create one row of LocalNotificationDataEntry in db
    private void addLocalNotificationDataEntryToDB(){
        Date date = new Date();
        final LocalNotificationDataEntry entry = new LocalNotificationDataEntry
                ("Reminder", "This is content","0.txt", date,0);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.localNotificationDataDao().insertEntry(entry);
            }
        });
    }

    @Override
    public void onItemClickListener(int itemId) {

    }
}
