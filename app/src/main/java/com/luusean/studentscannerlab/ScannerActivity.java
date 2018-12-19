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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.luusean.studentscannerlab.database.DaoMaster;
import com.luusean.studentscannerlab.database.DaoSession;
import com.luusean.studentscannerlab.database.EventObject;
import com.luusean.studentscannerlab.database.EventObjectDao;
import com.luusean.studentscannerlab.database.EventStudentObject;
import com.luusean.studentscannerlab.database.EventStudentObjectDao;
import com.luusean.studentscannerlab.student.Student;
import com.luusean.studentscannerlab.student.StudentAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    //DAO --> Data Access Object
    private EventObjectDao eventObjectDao;//sql access object
    private EventObject eventObject;
    private EventStudentObject eventStudentObject;
    private EventStudentObjectDao eventStudentObjectDao;

    private ArrayList<Student> ls_students;
    private ArrayList<Student> ls_stored_students;
    private RecyclerView recyclerView;

    private Long event_id; //to save scanned student to this event id
//    private String excelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        //mapping & initiating
        recyclerView = findViewById(R.id.rev_stored_students);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ls_stored_students = new ArrayList<>();

        //receive ls_r
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        ls_students = bundle.getParcelableArrayList("list");

        //[GreenDAO] initiate
        eventObjectDao = initEventObjectDb();
        eventStudentObjectDao = initEventStudentDb();

        event_id = getMaxEventId(eventObjectDao).getId();
    }

    private void initCameraScan(){
        IntentIntegrator integrator = new IntentIntegrator(ScannerActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan Student ID");
        integrator.setCameraId(0);
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                boolean isFound = false;//check if found student
                for (Student s : ls_students) {
                    if (s.getId().equals(intentResult.getContents())) {
                        ls_stored_students.add(new Student(
                                s.getId(),
                                s.getFname(),
                                s.getLname(),
                                s.getClassroom()
                        ));

                        //save to DB
                        eventStudentObject = new EventStudentObject(null, event_id, s.getId());
                        eventStudentObjectDao.insert(eventStudentObject);

                        //remove student from origin list
                        ls_students.remove(s);
                        //set found to check if found student
                        isFound = true;
                        Toast.makeText(this, "Found: " + intentResult.getContents(), Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                if (!isFound) {
                    if(isLsStudentsContains(ls_stored_students, intentResult.getContents())){
                        Toast.makeText(this, "Scanned: " + intentResult.getContents(), Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(this, "Not found: " + intentResult.getContents(), Toast.LENGTH_LONG).show();
                    }
                }

                Collections.sort(ls_stored_students, StudentsAscComparator);
                StudentAdapter adapter = new StudentAdapter(ScannerActivity.this, ls_stored_students);
                recyclerView.setAdapter(adapter);
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        return true;
    }

    public void onExportAction(MenuItem mi) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScannerActivity.this);
        builder.setTitle("Export .xlsx");
        builder.setMessage("Enter name to export(.xlsx)");
        builder.setCancelable(false);

        final EditText edtNameExcel = new EditText(ScannerActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtNameExcel.setLayoutParams(lp);
        builder.setView(edtNameExcel);
        builder.setIcon(R.drawable.ic_export_excel_black);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onContinuousScanAction(MenuItem mi) {
        initCameraScan();
    }

    private boolean isLsStudentsContains(ArrayList<Student> ls_students, String stuid) {
        for (Student s : ls_students) {
            if(s.getId().equals(stuid)) return true;
        }
        return false;
    }
    //detect click back button
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard Scanned");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });
        builder.setNeutralButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //send Result_ok to reload list events in MainActivity
                setResult(RESULT_OK, new Intent());
                finish();
            }
        });
        builder.setMessage("Do you wanna discard this scanned list?");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get list of students of event id
                List<EventStudentObject> ls_es = eventStudentObjectDao.queryBuilder()
                        .where(EventStudentObjectDao.Properties.Event_id.eq(event_id))
                        .list();
                //delete all of list
                eventStudentObjectDao.deleteInTx(ls_es);
                //delete created event
                eventObjectDao.deleteByKey(event_id);

                //send Result_ok to reload list events in MainActivity
                setResult(RESULT_OK, new Intent());

                //finish & back to MainActivity
                finish();
            }
        });
    }

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

    //initiate EventObject DB
    private EventStudentObjectDao initEventStudentDb() {
        //create db file if not exist
        String DB_NAME = "event_student_db";
        DaoMaster.DevOpenHelper masterHelper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
        //get the created db file
        SQLiteDatabase db = masterHelper.getWritableDatabase();
        DaoMaster master = new DaoMaster(db);//create masterDao
        DaoSession masterSession = master.newSession();//create session
        return masterSession.getEventStudentObjectDao();
    }

    //get max event id
    private EventObject getMaxEventId(EventObjectDao eventObjectDao){
        List<EventObject> ls_es = eventObjectDao.queryBuilder().orderDesc(EventObjectDao.Properties.Id).limit(1).list();
        return ls_es.get(0);
    }
}
