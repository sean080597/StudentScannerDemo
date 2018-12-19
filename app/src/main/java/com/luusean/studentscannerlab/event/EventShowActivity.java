package com.luusean.studentscannerlab.event;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.luusean.studentscannerlab.R;
import com.luusean.studentscannerlab.database.DaoMaster;
import com.luusean.studentscannerlab.database.DaoSession;
import com.luusean.studentscannerlab.database.EventObject;
import com.luusean.studentscannerlab.database.EventObjectDao;
import com.luusean.studentscannerlab.database.EventStudentObject;
import com.luusean.studentscannerlab.database.EventStudentObjectDao;
import com.luusean.studentscannerlab.student.Student;
import com.luusean.studentscannerlab.student.StudentAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventShowActivity extends AppCompatActivity {

    //DAO --> Data Access Object
    private EventStudentObjectDao eventStudentObjectDao;
    
    private RecyclerView recyclerView;

    //to lockup student to get name, class
    private List<Student> listStudents;
    //to know what event to show
    private Long event_id;

    private List<Student> listToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_show);

        //mapping recyclerView
        recyclerView = findViewById(R.id.rev_show_stu_of_event);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Initialise DAO
        eventStudentObjectDao = initEventStudentDb();

        //get intent
        event_id = Objects.requireNonNull(getIntent().getExtras()).getLong("event_id");

        //list id of student to lockup on listStudents
        List<EventStudentObject> ls_es = eventStudentObjectDao.queryBuilder()
                .where(EventStudentObjectDao.Properties.Event_id.eq(event_id))
                .list();
//        for(EventStudentObject es : ls_es){
//            for(Student stu : listStudents){
//                if(stu.getId().equals(es.getStu_id())){
//                    listToShow.add(stu);
//                    Log.d("listToShow", "Id: " + stu.getId());
//                    break;
//                }
//            }
//        }

        //set adapter
//        StudentAdapter adapter = new StudentAdapter(this, listToShow);
//        recyclerView.setAdapter(adapter);
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
}
