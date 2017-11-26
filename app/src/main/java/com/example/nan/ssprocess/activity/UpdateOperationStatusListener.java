package com.example.nan.ssprocess.activity;

/**
 * Created by Hu Tong on 2017/8/20.
 */

public interface UpdateOperationStatusListener {

    /**
     *
     * @param success True or false
     */
    void onUpdateOperationStatus(boolean success, String errorMsg);
}
