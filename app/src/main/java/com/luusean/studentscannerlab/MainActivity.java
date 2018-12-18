package com.luusean.studentscannerlab;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.luusean.studentscannerlab.blog.BlogItem;
import com.luusean.studentscannerlab.blog.CustomAdapter;
import com.luusean.studentscannerlab.student.Student;
import com.luusean.studentscannerlab.student.StudentAdapter;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Student> ls_students;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                    ls_students = new ArrayList<Student>();
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots){
                        ls_students.add(new Student(
                                doc.getId(),
                                doc.getString("fname"),
                                doc.getString("lname"),
                                doc.getString("class")
                        ));
                    }

                    Collections.sort(ls_students, StudentsAscComparator);
                    StudentAdapter adapter = new StudentAdapter(MainActivity.this, ls_students);
                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    //action for menu item - add
    public void onAddAction(MenuItem mi) {
        Intent intent = new Intent(MainActivity.this, ScannerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("list", ls_students);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // Comparator for Ascending Order
    public static Comparator<Student> StudentsAscComparator = new Comparator<Student>() {
        public int compare(Student s1, Student s2) {
            String sub1 = s1.getLname();
            String sub2 = s2.getLname();
            return sub1.compareToIgnoreCase(sub2);
        }
    };
}
