package com.example.nan.ssprocess.bean.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hu Tong on 5/28/2017.
 */

public class LinkData {

    private int from;

    private int to;

    private String fromPort = "";

    private String toPort = "";

    private List<Float> points = new ArrayList<>();

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
