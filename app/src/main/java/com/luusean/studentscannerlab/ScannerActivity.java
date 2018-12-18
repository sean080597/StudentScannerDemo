package com.luusean.studentscannerlab;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.luusean.studentscannerlab.student.Student;
import com.luusean.studentscannerlab.student.StudentAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

public class ScannerActivity extends AppCompatActivity {

    private ArrayList<Student> ls_students;
    private ArrayList<Student> ls_stored_students, ls_removed_students;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        //mapping & initiating
        recyclerView = findViewById(R.id.rev_stored_students);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ls_stored_students = new ArrayList<>();
        ls_removed_students = new ArrayList<>();

        //receive ls_r
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        ls_students = bundle.getParcelableArrayList("list");

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
                Toast.makeText(this, "Found: " + intentResult.getContents(), Toast.LENGTH_LONG).show();
                boolean isFound = false;//check if found student
                for (Student s : ls_students) {
                    if (s.getId().equals(intentResult.getContents())) {
                        ls_stored_students.add(new Student(
                                s.getId(),
                                s.getFname(),
                                s.getLname(),
                                s.getClassroom()
                        ));
                        ls_removed_students.add(s);
                        ls_students.remove(s);
                        isFound = true;//set found to check if found student
                        break;
                    }
                }
                if (!isFound) {
                    if(isLsStudentsContains(ls_removed_students, intentResult.getContents())){
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
        Toast.makeText(this, "Coming Soon", Toast.LENGTH_LONG).show();
    }

    public void onContinuousScanAction(MenuItem mi) {
        IntentIntegrator integrator = new IntentIntegrator(ScannerActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan Student ID");
        integrator.setCameraId(0);
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    private boolean isLsStudentsContains(ArrayList<Student> ls_students, String stuid) {
        for (Student s : ls_students) {
            if(s.getId().equals(stuid)) return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discard Scanned");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });
        builder.setMessage("Do you wanna discard this scanned list?");
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
