package com.example.nan.ssprocess.bean.basic;

/**
 * Created by nan on 2017/12/22.
 */

public class AttendanceData {
    private int id;
    private int userId;
    private int installGroupId;
    private String attendanceMember;
    private String overtimeMember;
    private String absenceMember;
    private String attendanceTomorrow;


    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getInstallGroupId() {
        return installGroupId;
    }

    public void setInstallGroupId(int installGroupId) {
        this.installGroupId = installGroupId;
    }

    public String getAttendanceMember() {
        return attendanceMember;
    }

    public void setAttendanceMember(String attendanceMember) {
        this.attendanceMember = attendanceMember;
    }

    public String getOvertimeMember() {
        return overtimeMember;
    }

    public void setOvertimeMember(String overtimeMember) {
        this.overtimeMember = overtimeMember;
    }

    public String getAbsenceMember() {
        return absenceMember;
    }

    public void setAbsenceMember(String absenceMember) {
        this.absenceMember = absenceMember;
    }

    public String getAttendanceTomorrow() {
        return attendanceTomorrow;
    }

    public void setAttendanceTomorrow(String attendanceTomorrow) {
        this.attendanceTomorrow = attendanceTomorrow;
    }
}
