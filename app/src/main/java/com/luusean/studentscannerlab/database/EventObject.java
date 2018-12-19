package com.luusean.studentscannerlab.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class EventObject {
    @org.greenrobot.greendao.annotation.Id (autoincrement = true)
    private Long Id;
    private String name;
    private String venue;
    @Generated(hash = 1383779442)
    public EventObject(Long Id, String name, String venue) {
        this.Id = Id;
        this.name = name;
        this.venue = venue;
    }
    @Generated(hash = 1839419078)
    public EventObject() {
    }
    public Long getId() {
        return this.Id;
    }
    public void setId(Long Id) {
        this.Id = Id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVenue() {
        return this.venue;
    }
    public void setVenue(String venue) {
        this.venue = venue;
    }
}
