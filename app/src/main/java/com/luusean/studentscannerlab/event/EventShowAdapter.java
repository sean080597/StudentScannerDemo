package com.luusean.studentscannerlab.event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luusean.studentscannerlab.R;
import com.luusean.studentscannerlab.database.EventStudentObject;
import com.luusean.studentscannerlab.student.Student;

import java.util.List;

public class EventShowAdapter extends RecyclerView.Adapter<EventShowAdapter.ViewHolder> {

    //list students take part in the event
    private List<EventStudentObject> listEventStudents;
    //list students to lockup to get name & class
    private List<Student> listStudents;

    private LayoutInflater inflater;

    EventShowAdapter(Context context, List<EventStudentObject> listEventStudents, List<Student> listStudents) {
        this.listEventStudents = listEventStudents;
        this.listStudents = listStudents;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public EventShowAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.lvi_student, parent, false);
        return new EventShowAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventShowAdapter.ViewHolder holder, int position) {
        EventStudentObject e = listEventStudents.get(position);
        for (Student stu : listStudents) {
            if(stu.getId().equals(e.getStu_id())){
                String s = stu.getFname() + " " + stu.getLname();
                holder.txtName.setText(s);
                s = stu.getClassroom() + " - " + stu.getId();
                holder.txtClassStuid.setText(s);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return listEventStudents.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtClassStuid;
        TextView txtName;
        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtClassStuid = itemView.findViewById(R.id.txt_class_stuid);
        }
    }
}
