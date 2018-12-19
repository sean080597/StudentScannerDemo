package com.luusean.studentscannerlab.event;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.luusean.studentscannerlab.R;
import com.luusean.studentscannerlab.database.DaoMaster;
import com.luusean.studentscannerlab.database.DaoSession;
import com.luusean.studentscannerlab.database.EventStudentObject;
import com.luusean.studentscannerlab.database.EventStudentObjectDao;
import com.luusean.studentscannerlab.database.StudentObject;
import com.luusean.studentscannerlab.database.StudentObjectDao;
import com.luusean.studentscannerlab.student.StudentAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventShowActivity extends AppCompatActivity {

    //DAO --> Data Access Object
    private EventStudentObjectDao eventStudentObjectDao;
    private StudentObjectDao studentObjectDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_show);

        //mapping recyclerView
        RecyclerView recyclerView = findViewById(R.id.rev_show_stu_of_event);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView txtEmpty = findViewById(R.id.empty_view);

        //Initialise DAO
        eventStudentObjectDao = initEventStudentDb();
        studentObjectDao = initStudentObjectDb();

        //get event id from intent
        Long event_id = Objects.requireNonNull(getIntent().getExtras()).getLong("event_id");

        //list id of student to lockup on listStudents
        List<EventStudentObject> ls_es = eventStudentObjectDao.queryBuilder()
                .where(EventStudentObjectDao.Properties.Event_id.eq(event_id))
                .list();
        List<StudentObject> ls_so = studentObjectDao.queryBuilder().build().list();

        List<StudentObject> lsToShow = new ArrayList<>();
        for(EventStudentObject es : ls_es){
            for(StudentObject so : ls_so){
                if(so.getId().equals(es.getStu_id())){
                    lsToShow.add(so);
                    break;
                }
            }
        }

        if(!lsToShow.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            txtEmpty.setVisibility(View.GONE);

            //set adapter
            StudentAdapter adapter = new StudentAdapter(this, lsToShow);
            recyclerView.setAdapter(adapter);
        }else{
            recyclerView.setVisibility(View.GONE);
            txtEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_show_menu, menu);
        return true;
    }

    //initiate EventStudentObject DB
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

    //MenuItem - export to excel
    public void onExportAction(MenuItem mi) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_LONG).show();
    }
}
