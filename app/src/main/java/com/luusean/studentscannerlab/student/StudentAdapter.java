package com.luusean.studentscannerlab.student;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.luusean.studentscannerlab.R;
import com.luusean.studentscannerlab.database.StudentObject;
import com.luusean.studentscannerlab.event.EventShowActivity;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<StudentObject> listStudents;
    private LayoutInflater inflater;
    private Context context;

    public StudentAdapter(Context context, List<StudentObject> listStudents) {
        this.listStudents = listStudents;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override

    public StudentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.lvi_student, parent, false);
        return new ViewHolder(v, new MyClickListenerInStudentAdapter() {
            @Override
            public void onDelete(int position) {
                StudentObject so = listStudents.get(position);
                ((EventShowActivity)context).deleteStudent(so);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.ViewHolder holder, int position) {
        final StudentObject stu = listStudents.get(position);
        String s = stu.getFname() + " " + stu.getLname();
        holder.txtName.setText(s);
        s = stu.getClassroom() + " - " + stu.getId();
        holder.txtClassStuid.setText(s);
    }

    @Override
    public int getItemCount() {
        return listStudents.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        MyClickListenerInStudentAdapter listener;

        TextView txtClassStuid;
        TextView txtName;
        ImageButton btnDeleteEvent;
        ViewHolder(View itemView, MyClickListenerInStudentAdapter listener) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtClassStuid = itemView.findViewById(R.id.txt_class_stuid);
            btnDeleteEvent = itemView.findViewById(R.id.btn_delete_student);
            this.listener = listener;

            btnDeleteEvent.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_delete_student:
                    listener.onDelete(this.getLayoutPosition());
                    break;
                default:
                    break;
            }
        }
    }

    public interface MyClickListenerInStudentAdapter {
        void onDelete(int position);
    }
}
