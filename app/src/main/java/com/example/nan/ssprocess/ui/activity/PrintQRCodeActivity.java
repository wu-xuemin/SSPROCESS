package com.example.nan.ssprocess.ui.activity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

import com.dothantech.printer.IDzPrinter;
import com.example.nan.ssprocess.R;
import com.example.nan.ssprocess.app.SinSimApp;
import com.example.nan.ssprocess.app.URL;
import com.example.nan.ssprocess.bean.PrintDeviceInfo;
import com.example.nan.ssprocess.bean.basic.MachineData;
import com.example.nan.ssprocess.net.Network;
import com.example.nan.ssprocess.util.ClsUtils;
import com.example.nan.ssprocess.util.ShowMessage;
import com.gengcon.www.jcprintersdk.JCAPI;
import com.gengcon.www.jcprintersdk.callback.Callback;
import com.gengcon.www.jcprintersdk.callback.PrintCallback;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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

    /**
     * 上一次连接成功设备的位置
     */
    private int mLastConnectSuccessItemPosition = -1;
    /**
     * 上一次连接成功的设备名称
     */
    private String mLastConnectSuccessDeviceName = "";

    private int mLastState = 0;

    /**
     * 设备mac地址
     */
    private List<String> mDeviceAddressList = new ArrayList<>();
    /**
     * 设备信息数据
     */
    private List<PrintDeviceInfo> mPrintDeviceInfoList = new ArrayList<>();

    private DeviceListAdapter mDeviceAdapter;

    /**
     * 创建打印回调
     */
    private PrintCallback mPrintCallback;
    /**
     * 创建打印控件实例
     */
    private JCAPI mPrinter;

    /**
     * 创建蓝牙适配器实例
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * 蓝牙广播意图过滤
     */
    IntentFilter mFilter;
    /**
     * 蓝牙广播
     */
    private BroadcastReceiver mReceiver;


    // 状态提示框
    private AlertDialog stateAlertDialog = null;
    private AlertDialog mSearchResultDialog=null;
    private AlertDialog mUpdatingProcessDialog;

    private static String TAG = "nlgPrintQRCodeActivity";
    private final String IP = SinSimApp.getApp().getServerIP();
    private MachineData mMachineByNameplate;
    private String location;

    private String mSearchContent = "";
    private String mPrintContent = "";
    public static final String PRINT_CODE = "num";
    private TextView mPrintContentTv;
    private EditText locationEt;

    private AlertDialog mPrinterDialog;

    /**
     * 初始化打印控件
     */
    private void initPrint() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //初始化全局回调
        Callback callback = initCallback();
        mPrinter = JCAPI.getInstance(callback);
        //初始化打印回调
        initPrintCallback();
    }

    private Callback initCallback() {
        /*
         * 创建全局回调
         */
        return new Callback() {
            @Override
            public void onConnectSuccess(String s, int i) {
                //打印机连接成功
                if (mDeviceAddressList.contains(s)) {
                    for (PrintDeviceInfo printDeviceInfo : mPrintDeviceInfoList) {
                        if (printDeviceInfo.getDeviceAddress().equals(s)) {
                            if (printDeviceInfo.getDeviceStatus() != 14) {
                                printDeviceInfo.setDeviceStatus(14);
                                updateDeviceList();
                            }
                            return;
                        }
                    }
                }
            }

            @Override
            public void onDisConnect() {
                //打印机断开及连接失败回调 （SDK内部无广播。关闭打印机及关闭蓝牙此处不会回调。需要实时监听状态 需要用到蓝牙广播）
            }

            @Override
            public void onElectricityChange(int i) {
                //打印机电量变化
            }

            @Override
            public void onCoverStatus(int i) {
                //打印机上盖状态监听
            }

            @Override
            public void onPaperStatus(int i) {
                //打印机标签装入状态监听
            }

//            @Override
//            public void onPrinterIsFree(int i) {
//                //打印机忙碌空闲状态监听
//            }

            @Override
            public void onHeartDisConnect() {
                //心跳断开，此处应调用断开打印机
            }
        };
    }

    private void initPrintCallback() {
        mPrintCallback = new PrintCallback() {
            @Override
            public void onPrintProgress(final int i) {
                //打印精度回调
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onPrintPageCompleted() {
                //页打印完成回调
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(PrintQRCodeActivity.this, "打印完成", ShowMessage.MessageDuring.SHORT);
                    }
                });
                mPrinter.endJob();
            }

            @Override
            public void onRibbonUsed(double v) {

            }

            @Override
            public void onPageNumberReceivingTimeout() {

            }

            @Override
            public void onAbnormalResponse(int i) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(PrintQRCodeActivity.this, "打印异常", ShowMessage.MessageDuring.SHORT);
                    }
                });
                mPrinter.endJob();
            }
        };
    }

    private void processBluetoothPair(BluetoothDevice bluetoothDevice) {
        String deviceAddress = bluetoothDevice.getAddress();

        try {
            //3.调用setPin方法进行配对...
            ClsUtils.setPin(bluetoothDevice.getClass(), bluetoothDevice, "0000");
            //1.确认配对
            ClsUtils.setPairingConfirmation(bluetoothDevice.getClass(), bluetoothDevice, true);
            //2.终止有序广播
            mReceiver.abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
            for (PrintDeviceInfo printDeviceInfo : mPrintDeviceInfoList) {
                if (deviceAddress.equals((printDeviceInfo.getDeviceAddress()))) {
                    printDeviceInfo.setDeviceStatus(12);
                }
            }
            updateDeviceList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDeviceList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 处理蓝牙发现广播
     *
     * @param bluetoothDevice 蓝牙设备对象
     */
    private void processBluetoothFound(BluetoothDevice bluetoothDevice) {
        String deviceAddress = bluetoothDevice.getAddress();
        String name = bluetoothDevice.getName();
        //打印机采用双模蓝牙,安卓采用SPP调用，设备类型为1664
        boolean printerType = bluetoothDevice.getBluetoothClass().getDeviceClass() == 1664;

        if (!mDeviceAddressList.contains(deviceAddress) && name != null && printerType) {
            mDeviceAddressList.add(deviceAddress);
            PrintDeviceInfo printDeviceInfo = null;
            //显示已配对设备
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                printDeviceInfo = new PrintDeviceInfo(bluetoothDevice.getName(), deviceAddress, 12);
            } else if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                printDeviceInfo = new PrintDeviceInfo(bluetoothDevice.getName(), deviceAddress, 10);
            }

            mPrintDeviceInfoList.add(printDeviceInfo);
            //刷新列表
            updateDeviceList();

        }
    }


    private void processBluetoothDisconnected(BluetoothDevice bluetoothDevice) {
        String deviceAddress = bluetoothDevice.getAddress();
        if (mDeviceAddressList.contains(deviceAddress)) {
            for (PrintDeviceInfo printDeviceInfo : mPrintDeviceInfoList) {
                if (printDeviceInfo.getDeviceAddress().equals(deviceAddress)) {
                    if (printDeviceInfo.getDeviceStatus() != 12) {
                        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                            printDeviceInfo.setDeviceStatus(12);
                        } else if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                            printDeviceInfo.setDeviceStatus(10);
                        }
                        updateDeviceList();
                    }
                    return;
                }
            }
        }
    }

    private void initBroadCast() {
        mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        mFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        mFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //获取意图
                String action = intent.getAction();
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.d(TAG, "processBluetoothPair: " + action);
                assert bluetoothDevice != null;
                //蓝牙发现
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    processBluetoothFound(bluetoothDevice);
                } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                    processBluetoothPair(bluetoothDevice);
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
//                    processBluetoothConnected(bluetoothDevice);
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    processBluetoothDisconnected(bluetoothDevice);
                }

            }
        };

        //注册广播
        registerReceiver(mReceiver, mFilter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        searchBluetoothDevice();
    }

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
        initBroadCast();
        initCallback();
        initPrint();
        mLastConnectSuccessDeviceName = SinSimApp.getApp().getPrinterName();
        Log.d(TAG,"mLastConnectSuccessDeviceName: " + mLastConnectSuccessDeviceName);

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
        //APP所有页面销毁时，断开打印机
        mPrinter.close();
        unregisterReceiver(mReceiver);
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // 应用退出时需要的操作
        recycle();

        if(mSearchResultDialog != null) {
            mSearchResultDialog.dismiss();
            mSearchResultDialog = null;
        }
        if(mUpdatingProcessDialog != null) {
            mUpdatingProcessDialog.dismiss();
            mUpdatingProcessDialog = null;
        }

        if(mPrinterDialog != null) {
            mPrinterDialog.dismiss();
            mPrinterDialog = null;
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
            connect(which);
        }
    }

    /**
     判断当前打印机是否连接
      */
    private boolean isPrinterConnected() {
        // 打印机已连接
        return mPrinter.isConnection() == 0;
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

//    // 打印文本
//    private boolean printText(String text, Bundle param) {
//
//        // 开始绘图任务，传入参数(页面宽度, 页面高度)
//        api.startJob(48, 50, 0);
//
//        // 开始一个页面的绘制，绘制文本字符串
//        // 传入参数(需要绘制的文本字符串, 绘制的文本框左上角水平位置, 绘制的文本框左上角垂直位置, 绘制的文本框水平宽度, 绘制的文本框垂直高度, 文字大小, 字体风格)
//        api.drawText(text, 4, 5, 40, 40, 4);
//
//        // 结束绘图任务提交打印
//        return api.commitJob();
//    }
//
//    // 打印文本一维码
//    private boolean printText1DBarcode(String text, String onedBarcde, Bundle param) {
//
//        // 开始绘图任务，传入参数(页面宽度, 页面高度)
//        api.startJob(48, 48, 90);
//
//        // 开始一个页面的绘制，绘制文本字符串
//        // 传入参数(需要绘制的文本字符串, 绘制的文本框左上角水平位置, 绘制的文本框左上角垂直位置, 绘制的文本框水平宽度, 绘制的文本框垂直高度, 文字大小, 字体风格)
//        api.drawText(text, 4, 4, 40, 20, 4);
//
//        // 设置之后绘制的对象内容旋转180度
//        api.setItemOrientation(180);
//
//        // 绘制一维码，此一维码绘制时内容会旋转180度，
//        // 传入参数(需要绘制的一维码的数据, 绘制的一维码左上角水平位置, 绘制的一维码左上角垂直位置, 绘制的一维码水平宽度, 绘制的一维码垂直高度)
//        api.draw1DBarcode(onedBarcde, LPAPI.BarcodeType.AUTO, 4, 25, 40, 15, 3);
//
//        // 结束绘图任务提交打印
//        return api.commitJob();
//    }

    /**
     *     打印二维码
     */
    private void print2dBarcode(String content, Bundle param) {
//        // 开始绘图任务，传入参数(页面宽度, 页面高度)
//        mPrinter.startJob(30, 30, 0);
//
//        // 开始一个页面的绘制，绘制二维码
//        // 传入参数(需要绘制的二维码的数据, 绘制的二维码左上角水平位置, 绘制的二维码左上角垂直位置, 绘制的二维码的宽度(宽高相同))
//        mPrinter.draw2DQRCode(content, 3, 2, 22);
//
//        mPrinter.drawText(content, 5, 25, 20, 5, 3);

        // 结束绘图任务提交打印
        mPrinter.startJob(30, 30, 0);
        mPrinter.startPage();
        mPrinter.drawQrCode(content, 3, 2, 22, 0);
        mPrinter.drawText(content, (double)3, (double)24, (double)20, (double)5, (double)4, (double)0, (float)1, (byte)4, 0, 0, false, "");
        mPrinter.endPage();
        mPrinter.commitJob(1, 1, 3, mPrintCallback);
    }

//    // 打印图片
//    private boolean printBitmap(Bitmap bitmap, Bundle param) {
//        // 打印
//        return mPrinter.printBitmap(bitmap, param);
//    }

    // 初始化界面
    private void initialView() {
        btnConnectDevice = (Button) findViewById(R.id.btn_printer);
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        String lastPrinterMac = sharedPreferences.getString(KeyLastPrinterMac, null);
        String lastPrinterName = sharedPreferences.getString(KeyLastPrinterName, null);
        String lastPrinterType = sharedPreferences.getString(KeyLastPrinterType, null);
        IDzPrinter.AddressType lastAddressType = TextUtils.isEmpty(lastPrinterType) ? null : Enum.valueOf(IDzPrinter.AddressType.class, lastPrinterType);
//        if (lastPrinterMac == null || lastPrinterName == null || lastAddressType == null) {
//            mPrinterAddress = null;
//        } else {
//            mPrinterAddress = new IDzPrinter.PrinterAddress(lastPrinterName, lastPrinterMac, lastAddressType);
//        }
        printQuality = sharedPreferences.getInt(KeyPrintQuality, -1);
        printDensity = sharedPreferences.getInt(KeyPrintDensity, -1);
        printSpeed = sharedPreferences.getInt(KeyPrintSpeed, -1);
        gapType = sharedPreferences.getInt(KeyGapType, -1);
        defaultText1 = sharedPreferences.getString(KeyDefaultText1, "SINSIM");
        defaultText2 = sharedPreferences.getString(KeyDefaultText2, "SINSIM");
        default1dBarcode = sharedPreferences.getString(KeyDefault1dBarcode, "SINSIM");
        default2dBarcode = sharedPreferences.getString(KeyDefault2dBarcode,"SINSIM");

        mDeviceAdapter = new DeviceListAdapter();
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

    private void searchBluetoothDevice() {
        //判断蓝牙是否开启
        if (mBluetoothAdapter.isEnabled()) {
//            //判断搜索权限是否开启
//            AndPermission.with(PrintQRCodeActivity.this)
//                    .runtime()
//                    .permission(Permission.Group.LOCATION)
//                    .onGranted(new Action<List<String>>() {
//                        @Override
//                        public void onAction(List<String> data) {
//                            mPrintDeviceInfoList.clear();
//                            mDeviceAddressList.clear();
//                            //刷新列表
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mLastConnectSuccessItemPosition = -1;
//                                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//                                    if (pairedDevices.size() > 0) {
//                                        for (BluetoothDevice device : pairedDevices) {
//                                            String deviceAddress = device.getAddress();
//                                            String deviceName = device.getName();
//                                            boolean isPrinterType = device.getBluetoothClass().getDeviceClass() == 1664;
//                                            if (!mDeviceAddressList.contains(deviceAddress) && deviceName != null && isPrinterType) {
//                                                mDeviceAddressList.add(deviceAddress);
//                                                PrintDeviceInfo printDeviceInfo;
//                                                if (!mLastConnectSuccessDeviceName.isEmpty() && mLastConnectSuccessDeviceName.equals(deviceName) && mPrinter.isConnection() == 0) {
//                                                    printDeviceInfo = new PrintDeviceInfo(device.getName(), device.getAddress(), 14);
//                                                } else {
//                                                    printDeviceInfo = new PrintDeviceInfo(device.getName(), device.getAddress(), BluetoothDevice.BOND_BONDED);
//                                                }
//                                                mPrintDeviceInfoList.add(printDeviceInfo);
//                                            }
//                                        }
//                                    }
//                                    mDeviceAdapter.notifyDataSetChanged();
//
//                                }
//                            });
//
//                            //权限获取成功,允许搜索
//                            mBluetoothAdapter.startDiscovery();
//                        }
//                    })
//                    .onDenied(new Action<List<String>>() {
//                        @Override
//                        public void onAction(List<String> data) {
//                            Toast.makeText(PrintQRCodeActivity.this, "请开启位置权限,用于搜索蓝牙设备", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .start();
            mPrintDeviceInfoList.clear();
            mDeviceAddressList.clear();
            //刷新列表
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLastConnectSuccessItemPosition = -1;
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceAddress = device.getAddress();
                            String deviceName = device.getName();
                            boolean isPrinterType = device.getBluetoothClass().getDeviceClass() == 1664;
                            if (!mDeviceAddressList.contains(deviceAddress) && deviceName != null && isPrinterType) {
                                mDeviceAddressList.add(deviceAddress);
                                //TODO:
                                PrintDeviceInfo printDeviceInfo;
                                if (!mLastConnectSuccessDeviceName.isEmpty() && mLastConnectSuccessDeviceName.equals(deviceName) && mPrinter.isConnection() == 0) {
                                    printDeviceInfo = new PrintDeviceInfo(device.getName(), device.getAddress(), 14);
                                } else {
                                    printDeviceInfo = new PrintDeviceInfo(device.getName(), device.getAddress(), BluetoothDevice.BOND_BONDED);
                                }
                                mPrintDeviceInfoList.add(printDeviceInfo);
                                if(mLastConnectSuccessDeviceName.equals(deviceName)) {
                                    connect(mPrintDeviceInfoList.indexOf(printDeviceInfo));
                                }
                            }
                        }
                    }
                    mDeviceAdapter.notifyDataSetChanged();
                }
            });

            //权限获取成功,允许搜索
            mBluetoothAdapter.startDiscovery();

        } else {
            Toast.makeText(PrintQRCodeActivity.this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
        }
    }

    private void processUnPairConnect(BluetoothDevice bluetoothDevice, int deviceStatus) {

        try {
            // 与设备配对
            if (ClsUtils.createBond(bluetoothDevice.getClass(), bluetoothDevice)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(PrintQRCodeActivity.this,"开始配对", ShowMessage.MessageDuring.SHORT);
                    }
                });

            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ShowMessage.showToast(PrintQRCodeActivity.this,"配对失败", ShowMessage.MessageDuring.SHORT);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processPairedConnect(int position, PrintDeviceInfo printDeviceInfo, String deviceAddress, int deviceStatus) {


        int isOpenSuccess = mPrinter.openPrinterByAddress(getApplication(), deviceAddress, 0);

        if (isOpenSuccess == 0) {
            if (mLastConnectSuccessItemPosition != -1) {
                mPrintDeviceInfoList.get(mLastConnectSuccessItemPosition).setDeviceStatus(12);
            }
            printDeviceInfo.setDeviceStatus(14);
            mLastConnectSuccessItemPosition = position;
            mLastConnectSuccessDeviceName = printDeviceInfo.getDeviceName();
            SinSimApp.getApp().setPrinterName(mLastConnectSuccessDeviceName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDeviceAdapter.notifyDataSetChanged();
                    ShowMessage.showToast(PrintQRCodeActivity.this, "连接成功", ShowMessage.MessageDuring.SHORT);
                    btnConnectDevice.setText("打印机：" + mLastConnectSuccessDeviceName);
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ShowMessage.showToast(PrintQRCodeActivity.this, "连接失败", ShowMessage.MessageDuring.SHORT);
                    mPrinterDialog.dismiss();
                }
            });
        }
    }

    private void connect(int position) {
        //连接前，请终止蓝牙搜索
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        final PrintDeviceInfo printDeviceInfo = mPrintDeviceInfoList.get(position);
        String deviceAddress = printDeviceInfo.getDeviceAddress();

        BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        int deviceStatus = printDeviceInfo.getDeviceStatus();
        //上一次连接时的状态
        mLastState = printDeviceInfo.getDeviceStatus();
        //未配对 进行配对
        if (deviceStatus == BluetoothDevice.BOND_NONE) {
            processUnPairConnect(bluetoothDevice, deviceStatus);
        } else if (deviceStatus == BluetoothDevice.BOND_BONDED) {
            //已配对设备 进行连接
            processPairedConnect(position, printDeviceInfo, deviceAddress, deviceStatus);
        }
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
        if(mPrinterDialog == null) {
            mPrinterDialog =  new AlertDialog.Builder(PrintQRCodeActivity.this).setTitle(R.string.selectbondeddevice).setAdapter(mDeviceAdapter, new DeviceListItemClicker()).create();
        }
        if(!mPrinterDialog.isShowing()) {
            mPrinterDialog.show();
        }
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
                print2dBarcode(mPrintContent, getPrintParam(1, 0));
            }
        }
    }

//    /**
//     *     连接打印机请求成功提交时操作
//     */
//    private void onPrinterConnecting(IDzPrinter.PrinterAddress printer, boolean showDialog) {
//        // 连接打印机请求成功提交，刷新界面提示
//        String txt = printer.shownName;
//        if (TextUtils.isEmpty(txt)) {
//            txt = printer.macAddress;
//        }
//        txt = getResources().getString(R.string.nowisconnectingprinter) + '[' + txt + ']';
//        txt += getResources().getString(R.string.printer);
//        if (showDialog) {
//            showStateAlertDialog(txt);
//        }
//        btnConnectDevice.setText(txt);
//    }
//
//    // 连接打印机操作提交失败、打印机连接失败或连接断开时操作
//    private void onPrinterDisconnected() {
//        // 连接打印机操作提交失败、打印机连接失败或连接断开时，刷新界面提示
//        clearAlertDialog();
//
//        Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.connectprinterfailed), Toast.LENGTH_SHORT).show();
//        btnConnectDevice.setText("");
//    }
//
//    /**
//     * 开始打印标签时操作
//     */
//    private void onPrintStart() {
//        // 开始打印标签时，刷新界面提示
//        showStateAlertDialog(R.string.nowisprinting);
//    }
//
//    /**
//     *     标签打印成功时操作
//     */
//    private void onPrintSuccess() {
//        // 标签打印成功时，刷新界面提示
//        clearAlertDialog();
//        Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.printsuccess), Toast.LENGTH_SHORT).show();
//    }
//
//    /**
//     *打印请求失败或标签打印失败时操作
//     */
//    private void onPrintFailed() {
//        // 打印请求失败或标签打印失败时，刷新界面提示
//        clearAlertDialog();
//        Toast.makeText(PrintQRCodeActivity.this, this.getResources().getString(R.string.printfailed), Toast.LENGTH_SHORT).show();
//    }
//
//    /**
//     *     显示连接、打印的状态提示框
//     */
//    private void showStateAlertDialog(int resId) {
//        showStateAlertDialog(getResources().getString(resId));
//    }
//
//    /**
//     *     显示连接、打印的状态提示框
//     */
//    private void showStateAlertDialog(String str) {
//        if (stateAlertDialog != null && stateAlertDialog.isShowing()) {
//            stateAlertDialog.setTitle(str);
//        } else {
//            stateAlertDialog = new AlertDialog.Builder(PrintQRCodeActivity.this).setCancelable(false).setTitle(str).show();
//        }
//    }
//
//    /**
//     *     清除连接、打印的状态提示框
//     */
//    private void clearAlertDialog() {
//        if (stateAlertDialog != null && stateAlertDialog.isShowing()) {
//            stateAlertDialog.dismiss();
//        }
//        stateAlertDialog = null;
//    }

    /**
     *     用于填充打印机列表的Adapter
     */
    private class DeviceListAdapter extends BaseAdapter {
        private TextView tv_name = null;
        private TextView tv_status = null;

        @Override
        public int getCount() {
            return mPrintDeviceInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPrintDeviceInfoList.get(position);
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
            tv_status = (TextView) convertView.findViewById(R.id.tv_status);

            if (mPrintDeviceInfoList != null && mPrintDeviceInfoList.size() > position) {
                tv_name.setText(mPrintDeviceInfoList.get(position).getDeviceName());
                String status = mPrintDeviceInfoList.get(position).getDeviceStatus() == BluetoothDevice.BOND_BONDED ? "未连接"
                        : mPrintDeviceInfoList.get(position).getDeviceStatus() == 14 ? "已连接" : "未知";
                tv_status.setText(status);
            }

            return convertView;
        }
    }

}
