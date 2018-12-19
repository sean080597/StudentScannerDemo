package com.luusean.studentscannerlab.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class EventStudentObject {
    @Id(autoincrement = true) //don't care about value of this field
    private Long idLocal;

    @Index
    private Long event_id;
    private String stu_id;
    @Generated(hash = 1813944676)
    public EventStudentObject(Long idLocal, Long event_id, String stu_id) {
        this.idLocal = idLocal;
        this.event_id = event_id;
        this.stu_id = stu_id;
    }
    @Generated(hash = 1718013192)
    public EventStudentObject() {
    }
    public Long getIdLocal() {
        return this.idLocal;
    }
    public void setIdLocal(Long idLocal) {
        this.idLocal = idLocal;
    }
    public Long getEvent_id() {
        return this.event_id;
    }
    public void setEvent_id(Long event_id) {
        this.event_id = event_id;
    }
    public String getStu_id() {
        return this.stu_id;
    }
    public void setStu_id(String stu_id) {
        this.stu_id = stu_id;
    }
    
}
