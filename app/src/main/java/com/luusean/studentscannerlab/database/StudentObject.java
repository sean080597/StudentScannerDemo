package com.luusean.studentscannerlab.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class StudentObject {
    @Id(autoincrement = true) //don't care about value of this field
    private Long idLocal;

    @Index
    private String id;
    private String fname;
    private String lname;
    private String classroom;
    @Generated(hash = 1978206283)
    public StudentObject(Long idLocal, String id, String fname, String lname,
            String classroom) {
        this.idLocal = idLocal;
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.classroom = classroom;
    }
    @Generated(hash = 127358214)
    public StudentObject() {
    }
    public Long getIdLocal() {
        return this.idLocal;
    }
    public void setIdLocal(Long idLocal) {
        this.idLocal = idLocal;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getFname() {
        return this.fname;
    }
    public void setFname(String fname) {
        this.fname = fname;
    }
    public String getLname() {
        return this.lname;
    }
    public void setLname(String lname) {
        this.lname = lname;
    }
    public String getClassroom() {
        return this.classroom;
    }
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
}
