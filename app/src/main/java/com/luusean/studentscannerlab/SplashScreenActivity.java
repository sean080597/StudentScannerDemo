package com.luusean.studentscannerlab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.luusean.studentscannerlab.database.DaoMaster;
import com.luusean.studentscannerlab.database.DaoSession;
import com.luusean.studentscannerlab.database.StudentObject;
import com.luusean.studentscannerlab.database.StudentObjectDao;

import java.util.List;

import javax.annotation.Nullable;

public class SplashScreenActivity extends AppCompatActivity {

    private StudentObjectDao studentObjectDao;
    private StudentObject studentObject;

    private SharedPreferences sharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //declare sharedPreferences
        sharedPreferences = getSharedPreferences("com.luusean.studentscannerlab", MODE_PRIVATE);
        //check if is firstRun & set true to "firstRun" if not exists
        if (sharedPreferences.getBoolean("firstRun", true)) {
            //check if network is available to sync offline data & go to MainActivity inside
            if(isNetworkAvailable()){
                SyncDataOfflineAsyncTasks asyncTasks = new SyncDataOfflineAsyncTasks();
                asyncTasks.execute();
                //This will call only first time
                //set false if isn't firstRun && not yet sync
                sharedPreferences.edit().putBoolean("firstRun", false).apply();
            }else{
                Toast.makeText(SplashScreenActivity.this, R.string.internet_not_connected, Toast.LENGTH_SHORT).show();
                Toast.makeText(SplashScreenActivity.this, "Cannot sync data from Server for first run", Toast.LENGTH_SHORT).show();
                Toast.makeText(SplashScreenActivity.this, "Will finish here", Toast.LENGTH_SHORT).show();
                Toast.makeText(SplashScreenActivity.this, "Bye Bye ahihi =))", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }
                }, 8000);
            }
        }else{
            //set delay times
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(isNetworkAvailable()){
                        SyncDataOfflineAsyncTasks asyncTasks = new SyncDataOfflineAsyncTasks();
                        asyncTasks.execute();
                    }else{
                        Toast.makeText(SplashScreenActivity.this, R.string.internet_not_connected, Toast.LENGTH_SHORT).show();
                        Toast.makeText(SplashScreenActivity.this, "Will use old Database", Toast.LENGTH_SHORT).show();
                        gotoMainActivity();
                    }
                }
            }, 3000);
        }

    }

    //move to MainActivity
    private void gotoMainActivity(){
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //init spin progress bar
    private void initProgressBar(){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
    }
    //check if network is available
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    //initiate Student DB
    private StudentObjectDao initStudentObjectDb() {
        //create db file if not exist
        String DB_NAME = "student_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getStudentObjectDao();
    }

    //check if list contains student
    private boolean isLsStudentsContains(List<StudentObject> ls_students, String stuid) {
        for (StudentObject s : ls_students) {
            if(s.getId().equals(stuid)) return true;
        }
        return false;
    }

    //AsyncTask for syncing Data on FireBase FireStore to Offline
    @SuppressLint("StaticFieldLeak")
    private class SyncDataOfflineAsyncTasks extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //init progressbar
            initProgressBar();

            //Initialise DAO
            studentObjectDao = initStudentObjectDb();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //get list students offline
            List<StudentObject> ls_so = studentObjectDao.queryBuilder().orderAsc(StudentObjectDao.Properties.Lname).build().list();
            //start syncing to offline from FireStore
            syncDataToGreenDao(ls_so);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(SplashScreenActivity.this, "Synced Successfully", Toast.LENGTH_SHORT).show();
            //set delay times
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    gotoMainActivity();
                }
            }, 1000);
        }
    }

    //sync data to offline GreenDao
    private void syncDataToGreenDao(final List<StudentObject> ls_so){
        //FireBase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.collection("student").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(SplashScreenActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }else{
                    assert queryDocumentSnapshots != null;
                    //check if not have student & update offline
                    for (DocumentSnapshot doc : queryDocumentSnapshots){
                        if(!isLsStudentsContains(ls_so, doc.getId())){
                            studentObject = new StudentObject(
                                    null, doc.getId(), doc.getString("fname"),
                                    doc.getString("lname"), doc.getString("class")
                            );
                            studentObjectDao.insert(studentObject);
                        }
                    }
                    //check if offline have redundant student & delete it
                    for(StudentObject so : ls_so){
                        Boolean isExists = false;
                        for (DocumentSnapshot doc : queryDocumentSnapshots){
                            if(so.getId().equals(doc.getId())){
                                isExists = true;
                                break;
                            }
                        }
                        if(!isExists) {
                            //get student to delete
                            StudentObject delete_so = studentObjectDao.queryBuilder()
                                    .where(StudentObjectDao.Properties.Id.eq(so.getId()))
                                    .limit(1).list().get(0);
                            studentObjectDao.delete(delete_so);
                        }
                    }
                    //get list students offline after updating
//                    ls_so = studentObjectDao.queryBuilder().orderAsc(StudentObjectDao.Properties.Lname).build().list();
//                    StudentAdapter adapter = new StudentAdapter(MainActivity.this, ls_so);
//                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }
}
