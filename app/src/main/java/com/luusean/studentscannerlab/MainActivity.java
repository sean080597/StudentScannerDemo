package com.luusean.studentscannerlab;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.luusean.studentscannerlab.database.DaoMaster;
import com.luusean.studentscannerlab.database.DaoSession;
import com.luusean.studentscannerlab.database.EventObject;
import com.luusean.studentscannerlab.database.EventObjectDao;
import com.luusean.studentscannerlab.event.EventAdapter;
import com.luusean.studentscannerlab.student.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    //DAO --> Data Access Object
    private EventObjectDao eventObjectDao;//sql access object
    private EventObject eventObject;

    private RecyclerView recyclerView;
    private ArrayList<Student> ls_students;

    private final int REQUEST_CODE = 1997;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mapping recyclerView
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Initialise DAO
        eventObjectDao = initEventObjectDb();

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
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }else{
                    ls_students = new ArrayList<>();
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots){
                        ls_students.add(new Student(
                                doc.getId(),
                                doc.getString("fname"),
                                doc.getString("lname"),
                                doc.getString("class")
                        ));
                    }

//                    Collections.sort(ls_students, StudentsAscComparator);
//                    StudentAdapter adapter = new StudentAdapter(MainActivity.this, ls_students);
//                    recyclerView.setAdapter(adapter);
                }
            }
        });

        List<EventObject> ls_es = eventObjectDao.queryBuilder().orderDesc(EventObjectDao.Properties.Id).build().list();
        EventAdapter adapter = new EventAdapter(MainActivity.this, ls_es);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    //action for menu item - add
    public void onAddAction(MenuItem mi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Create Event");
        builder.setMessage("Enter your event name");

        final EditText edtEventName = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtEventName.setLayoutParams(lp);
        builder.setView(edtEventName);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_export_excel_black);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                //check if null input event name
                if(TextUtils.isEmpty(edtEventName.getText().toString())){
                    edtEventName.setError(getString(R.string.invalid_input));
                    edtEventName.requestFocus();
                }else{
                    wantToCloseDialog = true;
                }
                //dismiss dialog
                if(wantToCloseDialog){
                    //save to Table Event DB & reload recyclerView
                    eventObject = new EventObject(null, edtEventName.getText().toString(), null);
                    eventObjectDao.insert(eventObject);

                    //dismiss dialog
                    alertDialog.dismiss();

                    //move to ScannerActivity
                    Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("list", ls_students);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
    }
    //reload list events when back from ScannerActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            //reload list events
            List<EventObject> ls_es = eventObjectDao.queryBuilder().orderDesc(EventObjectDao.Properties.Id).build().list();
            EventAdapter adapter = new EventAdapter(MainActivity.this, ls_es);
            recyclerView.setAdapter(adapter);
        }
    }

    // Comparator for Ascending Order
    public static Comparator<Student> StudentsAscComparator = new Comparator<Student>() {
        public int compare(Student s1, Student s2) {
            String sub1 = s1.getLname();
            String sub2 = s2.getLname();
            return sub1.compareToIgnoreCase(sub2);
        }
    };

    //initiate EventObject DB
    private EventObjectDao initEventObjectDb() {
        //create db file if not exist
        String DB_NAME = "event_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getEventObjectDao();
    }
}
