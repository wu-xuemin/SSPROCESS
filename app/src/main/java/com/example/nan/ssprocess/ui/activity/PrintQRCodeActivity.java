package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dothantech.lpapi.LPAPI;
import com.dothantech.printer.IDzPrinter;
import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.basic.MachineData;
import com.example.nan.ssprocess.bean.basic.TaskRecordMachineListData;
import com.example.nan.ssprocess.net.Network;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class PrintQRCodeActivity extends AppCompatActivity {

    /**
     *     用于处理各种通知消息，刷新界面的handler
     */
    private final Handler mHandler = new Handler();

    /**
     *     保存各种信息时的名称
     */
    private static final String KeyPrintQuality = "PrintQuality";
    private static final String KeyPrintDensity = "PrintDensity";
    private static final String KeyPrintSpeed = "PrintSpeed";
    private static final String KeyGapType = "GapType";

    private static final String KeyLastPrinterMac = "LastPrinterMac";
    private static final String KeyLastPrinterName = "LastPrinterName";
    private static final String KeyLastPrinterType = "LastPrinterType";

    private static final String KeyDefaultText1 = "DefaultText1";
    private static final String KeyDefaultText2 = "DefaultText2";
    private static final String KeyDefault1dBarcode = "Default1dBarcode";
    private static final String KeyDefault2dBarcode = "Default2dBarcode";

    /**
     *     需要用到的各个控件对象
     */
    private Button btnConnectDevice = null;
//    private Button btnPrintQuality = null;
//    private Button btnPrintDensity = null;
//    private Button btnPrintSpeed = null;
//    private Button btnGapType = null;
    private EditText et1 = null;
    private EditText et2 = null;

    // 打印参数
    private int printQuality = -1;
    private int printDensity = -1;
    private int printSpeed = -1;
    private int gapType = -1;

    // 打印数据
    private String defaultText1 = "";
    private String defaultText2 = "";
    private String default1dBarcode = "";
    private String default2dBarcode = "";

    // 用于填充的数组及集合列表
    private String[] printQualityList = null;
    private String[] printDensityList = null;
    private String[] printSpeedList = null;
    private String[] gapTypeList = null;

    private List<IDzPrinter.PrinterAddress> pairedPrinters = new ArrayList<IDzPrinter.PrinterAddress>();

    private List<Bitmap> printBitmaps = new ArrayList<Bitmap>();

    // 上次连接成功的设备对象
    private IDzPrinter.PrinterAddress mPrinterAddress = null;

    // 状态提示框
    private AlertDialog stateAlertDialog = null;
    private AlertDialog mSearchResultDialog=null;
    private AlertDialog mUpdatingProcessDialog;

    private LPAPI api;
    private static String TAG = "nlgPrintQRCodeActivity";
    private final String IP = SinSimApp.getApp().getServerIP();
    private MachineData mMachineByNameplate;
    private String location;

    private String mSearchContent = "";
    private String mPrintContent = "";
    public static final String PRINT_CODE = "num";
    private TextView mPrintContentTv;
    private EditText locationEt;

    /********************************************************************************************************************************************/
    // DzPrinter连接打印功能相关
    /********************************************************************************************************************************************/

    // LPAPI 打印机操作相关的回调函数。

    private final LPAPI.Callback mCallback = new LPAPI.Callback() {

        /****************************************************************************************************************************************/
        // 所有回调函数都是在打印线程中被调用，因此如果需要刷新界面，需要发送消息给界面主线程，以避免互斥等繁琐操作。
        /****************************************************************************************************************************************/

        // 打印机连接状态发生变化时被调用
        @Override
        public void onStateChange(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterState arg1) {
            final IDzPrinter.PrinterAddress printer = arg0;
            switch (arg1) {
                case Connected:
                case Connected2:
                    // 打印机连接成功，发送通知，刷新界面提示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onPrinterConnected(printer);
                        }
                    });
                    break;

                case Disconnected:
                    // 打印机连接失败、断开连接，发送通知，刷新界面提示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onPrinterDisconnected();
                        }
                    });
                    break;

                default:
                    break;
            }
        }

        // 蓝牙适配器状态发生变化时被调用
        @Override
        public void onProgressInfo(IDzPrinter.ProgressInfo arg0, Object arg1) {
        }

        @Override
        public void onPrinterDiscovery(IDzPrinter.PrinterAddress arg0, IDzPrinter.PrinterInfo arg1) {
        }

        // 打印标签的进度发生变化是被调用
        @Override
        public void onPrintProgress(IDzPrinter.PrinterAddress address, Object bitmapData, IDzPrinter.PrintProgress progress, Object addiInfo) {
            switch (progress) {
                case Success:
                    // 打印标签成功，发送通知，刷新界面提示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onPrintSuccess();
                        }
                    });
                    break;

                case Failed:
                    // 打印标签失败，发送通知，刷新界面提示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onPrintFailed();
                        }
                    });
                    break;

                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_qrcode);

        //返回前页按钮
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mPrintContentTv = findViewById(R.id.tv_print_content);
        AppCompatButton printBtn = findViewById(R.id.btn_print2dbarcode);
        locationEt = findViewById(R.id.location_et);

        //获取搜索内容
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mPrintContent = intent.getStringExtra(SearchManager.QUERY);
            mPrintContentTv.setText(mPrintContent);
            Log.d(TAG, "onCreate: "+mPrintContent);
        }
        fetchProcessData();

        // 初始化界面
        initialView();

        // 调用LPAPI对象的init方法初始化对象
        this.api = LPAPI.Factory.createInstance(mCallback);

        // 尝试连接上次成功连接的打印机
        if (mPrinterAddress != null) {
            if (api.openPrinterByAddress(mPrinterAddress)) {
                // 连接打印机的请求提交成功，刷新界面提示
                onPrinterConnecting(mPrinterAddress, false);
                return;
            }
        }

    }
    private void fetchProcessData() {
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        String fetchProcessRecordUrl = URL.HTTP_HEAD + IP + URL.FETCH_TASK_RECORD_BY_SEARCH_TO_ADMIN;
        mPostValue.put("nameplate", mPrintContent);
        Network.Instance(SinSimApp.getApp()).fetchMachineByNameplate(fetchProcessRecordUrl, mPostValue, new FetchMachineDataHandler());
    }

    @SuppressLint("HandlerLeak")
    private class FetchMachineDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if (msg.what == Network.OK) {
                mMachineByNameplate=(MachineData)msg.obj;
                locationEt.setText(mMachineByNameplate.getLocation());
            } else {
                String errorMsg = (String)msg.obj;
                mSearchResultDialog = new AlertDialog.Builder(PrintQRCodeActivity.this).create();
                mSearchResultDialog.setCancelable(false);
                mSearchResultDialog.setCanceledOnTouchOutside(false);
                mSearchResultDialog.setTitle("搜索失败");
                mSearchResultDialog.setMessage(errorMsg);
                mSearchResultDialog.setButton(AlertDialog.BUTTON_POSITIVE, "重新搜索",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //关闭当前activity
                                finish();
                            }
                        });
                mSearchResultDialog.show();
            }
        }
    }

    public void updateLocationOnClick(View view) {
        //获取dialog的输入信息，并上传到服务器
        if (TextUtils.isEmpty(locationEt.getText())) {
            Toast.makeText(PrintQRCodeActivity.this,"地址不能为空，请确认后重新输入！",Toast.LENGTH_SHORT).show();
        }else {
            location=locationEt.getText().toString();
            if( mUpdatingProcessDialog == null) {
                mUpdatingProcessDialog = new AlertDialog.Builder(PrintQRCodeActivity.this).create();
                mUpdatingProcessDialog.setCancelable(false);
                mUpdatingProcessDialog.setCanceledOnTouchOutside(false);
                mUpdatingProcessDialog.setMessage("上传信息中...");
            }
            mUpdatingProcessDialog.show();
            updateProcessDetailData();
        }
    }
    /**
     * 更新上传机器location
     */
    private void updateProcessDetailData() {
        //更新location状态
        mMachineByNameplate.setLocation(location);
        Gson gson=new Gson();
        String machineDataToJson = gson.toJson(mMachineByNameplate);
        Log.d(TAG, "onItemClick: gson :"+ machineDataToJson);
        LinkedHashMap<String, String> mPostValue = new LinkedHashMap<>();
        mPostValue.put("machine", machineDataToJson);
        String updateProcessRecordUrl = URL.HTTP_HEAD + IP + URL.UPDATE_MACHINE_LOCATION;
        Log.d(TAG, "updateProcessDetailData: "+updateProcessRecordUrl+mPostValue.get("machine"));
        Network.Instance(SinSimApp.getApp()).updateProcessRecordData(updateProcessRecordUrl, mPostValue, new UpdateProcessDetailDataHandler());
    }
    @SuppressLint("HandlerLeak")
    private class UpdateProcessDetailDataHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {

            if(mUpdatingProcessDialog != null && mUpdatingProcessDialog.isShowing()) {
                mUpdatingProcessDialog.dismiss();
            }
            if (msg.what == Network.OK) {
                Toast.makeText(PrintQRCodeActivity.this, "上传位置成功！", Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = (String)msg.obj;
                Log.d(TAG, "handleMessage: "+errorMsg);
                Toast.makeText(PrintQRCodeActivity.this, "上传失败："+errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        // 应用退出时，调用LPAPI对象的quit方法断开打印机连接
        api.quit();

        // 应用退出时需要的操作
        recycle();

        if(mSearchResultDialog != null) {
            mSearchResultDialog.dismiss();
        }
        if(mUpdatingProcessDialog != null) {
            mUpdatingProcessDialog.dismiss();
        }

        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *     打印机列表的每项点击事件
     */
    private class DeviceListItemClicker implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            IDzPrinter.PrinterAddress printer = pairedPrinters.get(which);
            if (printer != null) {
                // 连接选择的打印机
                if (api.openPrinterByAddress(printer)) {
                    // 连接打印机的请求提交成功，刷新界面提示
                    onPrinterConnecting(printer, true);
                    return;
                }
            }

            // 连接打印机失败，刷新界面提示
            onPrinterDisconnected();
        }
    }

    /**
     判断当前打印机是否连接
      */
    private boolean isPrinterConnected() {
        // 调用LPAPI对象的getPrinterState方法获取当前打印机的连接状态
        IDzPrinter.PrinterState state = api.getPrinterState();

        // 打印机未连接
        if (state == null || state.equals(IDzPrinter.PrinterState.Disconnected)) {
            Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.pleaseconnectprinter), Toast.LENGTH_SHORT).show();
            return false;
        }

        // 打印机正在连接
        if (state.equals(IDzPrinter.PrinterState.Connecting)) {
            Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.waitconnectingprinter), Toast.LENGTH_SHORT).show();
            return false;
        }

        // 打印机已连接
        return true;
    }

    /**
     *     获取打印时需要的打印参数
     */
    private Bundle getPrintParam(int copies, int orientation) {
        Bundle param = new Bundle();

        // 打印浓度
        if (printDensity >= 0) {
            param.putInt(IDzPrinter.PrintParamName.PRINT_DENSITY, printDensity);
        }

        // 打印速度
        if (printSpeed >= 0) {
            param.putInt(IDzPrinter.PrintParamName.PRINT_SPEED, printSpeed);
        }

        // 间隔类型
        if (gapType >= 0) {
            param.putInt(IDzPrinter.PrintParamName.GAP_TYPE, gapType);
        }

        // 打印页面旋转角度
        if (orientation != 0) {
            param.putInt(IDzPrinter.PrintParamName.PRINT_DIRECTION, orientation);
        }

        // 打印份数
        if (copies > 1) {
            param.putInt(IDzPrinter.PrintParamName.PRINT_COPIES, copies);
        }

        return param;
    }

    /********************************************************************************************************************************************/
    // LPAPI绘图打印相关
    /********************************************************************************************************************************************/

    // 打印文本
    private boolean printText(String text, Bundle param) {

        // 开始绘图任务，传入参数(页面宽度, 页面高度)
        api.startJob(48, 50, 0);

        // 开始一个页面的绘制，绘制文本字符串
        // 传入参数(需要绘制的文本字符串, 绘制的文本框左上角水平位置, 绘制的文本框左上角垂直位置, 绘制的文本框水平宽度, 绘制的文本框垂直高度, 文字大小, 字体风格)
        api.drawText(text, 4, 5, 40, 40, 4);

        // 结束绘图任务提交打印
        return api.commitJob();
    }

    // 打印文本一维码
    private boolean printText1DBarcode(String text, String onedBarcde, Bundle param) {

        // 开始绘图任务，传入参数(页面宽度, 页面高度)
        api.startJob(48, 48, 90);

        // 开始一个页面的绘制，绘制文本字符串
        // 传入参数(需要绘制的文本字符串, 绘制的文本框左上角水平位置, 绘制的文本框左上角垂直位置, 绘制的文本框水平宽度, 绘制的文本框垂直高度, 文字大小, 字体风格)
        api.drawText(text, 4, 4, 40, 20, 4);

        // 设置之后绘制的对象内容旋转180度
        api.setItemOrientation(180);

        // 绘制一维码，此一维码绘制时内容会旋转180度，
        // 传入参数(需要绘制的一维码的数据, 绘制的一维码左上角水平位置, 绘制的一维码左上角垂直位置, 绘制的一维码水平宽度, 绘制的一维码垂直高度)
        api.draw1DBarcode(onedBarcde, LPAPI.BarcodeType.AUTO, 4, 25, 40, 15, 3);

        // 结束绘图任务提交打印
        return api.commitJob();
    }

    /**
     *     打印二维码
     */
    private boolean print2dBarcode(String content, Bundle param) {
        // 开始绘图任务，传入参数(页面宽度, 页面高度)
        api.startJob(30, 30, 0);

        // 开始一个页面的绘制，绘制二维码
        // 传入参数(需要绘制的二维码的数据, 绘制的二维码左上角水平位置, 绘制的二维码左上角垂直位置, 绘制的二维码的宽度(宽高相同))
        api.draw2DQRCode(content, 3, 2, 22);

        api.drawText(content, 5, 25, 20, 5, 3);

        // 结束绘图任务提交打印
        return api.commitJob();
    }

    // 打印图片
    private boolean printBitmap(Bitmap bitmap, Bundle param) {
        // 打印
        return api.printBitmap(bitmap, param);
    }

    // 初始化界面
    private void initialView() {
        btnConnectDevice = (Button) findViewById(R.id.btn_printer);
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        String lastPrinterMac = sharedPreferences.getString(KeyLastPrinterMac, null);
        String lastPrinterName = sharedPreferences.getString(KeyLastPrinterName, null);
        String lastPrinterType = sharedPreferences.getString(KeyLastPrinterType, null);
        IDzPrinter.AddressType lastAddressType = TextUtils.isEmpty(lastPrinterType) ? null : Enum.valueOf(IDzPrinter.AddressType.class, lastPrinterType);
        if (lastPrinterMac == null || lastPrinterName == null || lastAddressType == null) {
            mPrinterAddress = null;
        } else {
            mPrinterAddress = new IDzPrinter.PrinterAddress(lastPrinterName, lastPrinterMac, lastAddressType);
        }
        printQuality = sharedPreferences.getInt(KeyPrintQuality, -1);
        printDensity = sharedPreferences.getInt(KeyPrintDensity, -1);
        printSpeed = sharedPreferences.getInt(KeyPrintSpeed, -1);
        gapType = sharedPreferences.getInt(KeyGapType, -1);
        defaultText1 = sharedPreferences.getString(KeyDefaultText1, "SINSIM");
        defaultText2 = sharedPreferences.getString(KeyDefaultText2, "SINSIM");
        default1dBarcode = sharedPreferences.getString(KeyDefault1dBarcode, "SINSIM");
        default2dBarcode = sharedPreferences.getString(KeyDefault2dBarcode,"SINSIM");
    }

    // 应用退出时需要的操作
    private void recycle() {
        // 保存相关信息
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KeyPrintQuality, printQuality);
        editor.putInt(KeyPrintDensity, printDensity);
        editor.putInt(KeyPrintSpeed, printSpeed);
        editor.putInt(KeyGapType, gapType);
        if (mPrinterAddress != null) {
            editor.putString(KeyLastPrinterMac, mPrinterAddress.macAddress);
            editor.putString(KeyLastPrinterName, mPrinterAddress.shownName);
            editor.putString(KeyLastPrinterType, mPrinterAddress.addressType.toString());
        }
        if (defaultText1 != null) {
            editor.putString(KeyDefaultText1, defaultText1);
        }
        if (defaultText2 != null) {
            editor.putString(KeyDefaultText2, defaultText2);
        }
        if (default1dBarcode != null) {
            editor.putString(KeyDefault1dBarcode, default1dBarcode);
        }
        if (default2dBarcode != null) {
            editor.putString(KeyDefault2dBarcode, default2dBarcode);
        }
        editor.commit();
    }

    /**
     *     选择打印机的按钮事件
     */
    public void selectPrinterOnClick(View view) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.unsupportedbluetooth), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!btAdapter.isEnabled()) {
            Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.unenablebluetooth), Toast.LENGTH_SHORT).show();
            return;
        }

        pairedPrinters = api.getAllPrinterAddresses(null);
        new AlertDialog.Builder(PrintQRCodeActivity.this).setTitle(R.string.selectbondeddevice).setAdapter(new DeviceListAdapter(), new DeviceListItemClicker()).show();
    }


    /**
     *     打印二维码的按钮事件
     */
    public void print2DBarcodeOnClick(View view) {
        if(mPrintContent.equals("")) {
            Log.d(TAG, "print2DBarcodeOnClick: 打印内容为空");
        } else {
            /// 获取打印数据并进行打印
            if (isPrinterConnected()) {
                if (print2dBarcode(mPrintContent, getPrintParam(1, 0))) {
                    onPrintStart();
                } else {
                    onPrintFailed();
                }
            }
        }
    }

    /**
     *     连接打印机请求成功提交时操作
     */
    private void onPrinterConnecting(IDzPrinter.PrinterAddress printer, boolean showDialog) {
        // 连接打印机请求成功提交，刷新界面提示
        String txt = printer.shownName;
        if (TextUtils.isEmpty(txt)) {
            txt = printer.macAddress;
        }
        txt = getResources().getString(R.string.nowisconnectingprinter) + '[' + txt + ']';
        txt += getResources().getString(R.string.printer);
        if (showDialog) {
            showStateAlertDialog(txt);
        }
        btnConnectDevice.setText(txt);
    }

    // 连接打印机成功时操作
    private void onPrinterConnected(IDzPrinter.PrinterAddress printer) {
        // 连接打印机成功时，刷新界面提示，保存相关信息
        clearAlertDialog();
        Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.connectprintersuccess), Toast.LENGTH_SHORT).show();
        mPrinterAddress = printer;
        // 调用LPAPI对象的getPrinterInfo方法获得当前连接的打印机信息
        String txt = getResources().getString(R.string.printer) + getResources().getString(R.string.chinesecolon);
        txt += api.getPrinterInfo().deviceName + "\n";
        txt += api.getPrinterInfo().deviceAddress;
        btnConnectDevice.setText(txt);
    }

    // 连接打印机操作提交失败、打印机连接失败或连接断开时操作
    private void onPrinterDisconnected() {
        // 连接打印机操作提交失败、打印机连接失败或连接断开时，刷新界面提示
        clearAlertDialog();

        Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.connectprinterfailed), Toast.LENGTH_SHORT).show();
        btnConnectDevice.setText("");
    }

    /**
     * 开始打印标签时操作
     */
    private void onPrintStart() {
        // 开始打印标签时，刷新界面提示
        showStateAlertDialog(R.string.nowisprinting);
    }

    /**
     *     标签打印成功时操作
     */
    private void onPrintSuccess() {
        // 标签打印成功时，刷新界面提示
        clearAlertDialog();
        Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.printsuccess), Toast.LENGTH_SHORT).show();
    }

    /**
     *打印请求失败或标签打印失败时操作
     */
    private void onPrintFailed() {
        // 打印请求失败或标签打印失败时，刷新界面提示
        clearAlertDialog();
        Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.printfailed), Toast.LENGTH_SHORT).show();
    }

    /**
     *     显示连接、打印的状态提示框
     */
    private void showStateAlertDialog(int resId) {
        showStateAlertDialog(getResources().getString(resId));
    }

    /**
     *     显示连接、打印的状态提示框
     */
    private void showStateAlertDialog(String str) {
        if (stateAlertDialog != null && stateAlertDialog.isShowing()) {
            stateAlertDialog.setTitle(str);
        } else {
            stateAlertDialog = new AlertDialog.Builder(PrintQRCodeActivity.this).setCancelable(false).setTitle(str).show();
        }
    }

    /**
     *     清除连接、打印的状态提示框
     */
    private void clearAlertDialog() {
        if (stateAlertDialog != null && stateAlertDialog.isShowing()) {
            stateAlertDialog.dismiss();
        }
        stateAlertDialog = null;
    }

    /**
     *     用于填充打印机列表的Adapter
     */
    private class DeviceListAdapter extends BaseAdapter {
        private TextView tv_name = null;
        private TextView tv_mac = null;

        @Override
        public int getCount() {
            return pairedPrinters.size();
        }

        @Override
        public Object getItem(int position) {
            return pairedPrinters.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(PrintQRCodeActivity.this).inflate(R.layout.printer_item, null);
            }
            tv_name = (TextView) convertView.findViewById(R.id.tv_device_name);
            tv_mac = (TextView) convertView.findViewById(R.id.tv_macaddress);

            if (pairedPrinters != null && pairedPrinters.size() > position) {
                IDzPrinter.PrinterAddress printer = pairedPrinters.get(position);
                tv_name.setText(printer.shownName);
                tv_mac.setText(printer.macAddress);
            }

            return convertView;
        }
    }

    /**
     *     用于填充设置打印参数界面的Adapter
     */
    private class ParamAdapter extends BaseAdapter {
        private TextView tv_param = null;
        private String[] paramArray = null;

        public ParamAdapter(String[] array) {
            this.paramArray = array;
        }

        @Override
        public int getCount() {
            return paramArray.length;
        }

        @Override
        public Object getItem(int position) {
            return paramArray[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(PrintQRCodeActivity.this).inflate(R.layout.param_item, null);
            }
            tv_param = (TextView) convertView.findViewById(R.id.tv_param);
            String text = "";
            if (paramArray != null && paramArray.length > position) {
                text = paramArray[position];
            }
            tv_param.setText(text);

            return convertView;
        }
    }

    /**
     *用于填充打印图片的示例图片列表的Adapter
     */
    private class BitmapListAdapter extends BaseAdapter {
        private ImageView iv_bmp = null;

        @Override
        public int getCount() {
            return printBitmaps.size();
        }

        @Override
        public Object getItem(int position) {
            return printBitmaps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(PrintQRCodeActivity.this).inflate(R.layout.bitmap_item, null);
            }
            iv_bmp = (ImageView) convertView.findViewById(R.id.iv_bmp);
            if (printBitmaps != null && printBitmaps.size() > position) {
                Bitmap bmp = printBitmaps.get(position);
                if (bmp != null) {
                    iv_bmp.setImageBitmap(bmp);
                }
            }

            return convertView;
        }
    }

}
