package com.luusean.studentscannerlab.student;

import android.os.Parcel;
import android.os.Parcelable;

public class Student implements Parcelable{
    private String id;
    private String fname;
    private String lname;
    private String classroom;

    public Student(Parcel p){
        id = p.readString();
        fname = p.readString();
        lname = p.readString();
        classroom = p.readString();
    }

    public Student(String id, String fname, String lname, String classroom) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.classroom = classroom;
    }

    public static final Creator<Student> CREATOR = new Creator<Student>() {
        @Override
        public Student createFromParcel(Parcel in) {
            return new Student(in);
        }

        @Override
        public Student[] newArray(int size) {
            return new Student[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(fname);
        dest.writeString(lname);
        dest.writeString(classroom);
    }
}
