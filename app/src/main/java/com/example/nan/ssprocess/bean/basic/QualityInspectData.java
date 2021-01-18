package com.example.nan.ssprocess.bean.basic;

import java.io.Serializable;

public class QualityInspectData implements Serializable {
    private int id;
    private String inspectName;
    private String inspectContent;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInspectName() {
        return inspectName;
    }

    public void setInspectName(String inspectName) {
        this.inspectName = inspectName;
    }

    public String getInspectContent() {
        return inspectContent;
    }

    public void setInspectContent(String inspectContent) {
        this.inspectContent = inspectContent;
    }
}
