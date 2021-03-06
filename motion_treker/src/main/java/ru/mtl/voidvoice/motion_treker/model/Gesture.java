package ru.mtl.voidvoice.motion_treker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Gesture {
    @JsonIgnore
    private long id;

    private List<KeyPoint> keyPointList;

    private String meaning;

    public Gesture() {

    }

    @JsonIgnore
    public Long getPrimaryKey(){
        return this.id;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<KeyPoint> getKeyPointList() {
        return keyPointList;
    }

    public void setKeyPointList(List<KeyPoint> keyPointList) {
        this.keyPointList = keyPointList;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }
}
