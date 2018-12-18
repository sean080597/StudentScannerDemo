package com.luusean.studentscannerlab.student;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luusean.studentscannerlab.R;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<Student> listStudents;
    private LayoutInflater inflater;

    public StudentAdapter(Context context, List<Student> listStudents) {
        this.listStudents = listStudents;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override

    public StudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.lvi_student, parent, false);
        return new StudentAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.ViewHolder holder, int position) {
        final Student stu = listStudents.get(position);
        String s = stu.getFname() + " " + stu.getLname();
        holder.txtName.setText(s);
        s = stu.getClassroom() + " - " + stu.getId();
        holder.txtClassStuid.setText(s);
    }

    @Override
    public int getItemCount() {
        return listStudents.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtClassStuid;
        TextView txtName;
        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtClassStuid = itemView.findViewById(R.id.txt_class_stuid);
        }
    }
}
