package com.calypso.buetools;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calypso.bluelib.bean.MessageBean;
import com.calypso.bluelib.bean.SearchResult;
import com.calypso.bluelib.database.Student;
import com.calypso.bluelib.database.StudentRepository;
import com.calypso.bluelib.listener.OnConnectListener;
import com.calypso.bluelib.listener.OnReceiveMessageListener;
import com.calypso.bluelib.listener.OnSearchDeviceListener;
import com.calypso.bluelib.listener.OnSendMessageListener;
import com.calypso.bluelib.manage.BlueManager;
import com.calypso.bluelib.utils.TypeConversion;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private BlueManager bluemanage;
    private int progress = 0;
    private TextView statusView;
    private TextView contextView;
    private ProgressBar progressBar;
    private StringBuilder stringBuilder;
    private List<SearchResult> mDevices;
    private DeviceListAdapter mAdapter;
    private RecyclerView recycleView;
    private RelativeLayout devieslist;
    private RelativeLayout deviesinfo;
    private OnConnectListener onConnectListener;
    private OnSendMessageListener onSendMessageListener;
    private OnSearchDeviceListener onSearchDeviceListener;
    private OnReceiveMessageListener onReceiveMessageListener;
    private StudentRepository studentRepository;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String message = msg.obj.toString();
            switch (msg.what) {
                case 0:
                    statusView.setText(message);
                    break;
                case 1:
                    stringBuilder.append(message + " \n");
                    contextView.setText(stringBuilder.toString());
                    progress += 4;
                    progressBar.setProgress(progress);
                    break;
                case 2:
                    progress = 100;
                    progressBar.setProgress(progress);
                    break;
                case 3:
                    statusView.setText("???????????????");
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(message);
                    contextView.setText(stringBuilder.toString());
                    break;
                case 4:
                    statusView.setText(message);
                    deviesinfo.setVisibility(View.VISIBLE);
                    devieslist.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        studentRepository = new StudentRepository(MainActivity.this);
        List<Student> students =  studentRepository.getAll();
        Log.i("onCreate before ",students.toString());
        Student stuent = new Student("hello","123",123);
        studentRepository.insert(stuent);
        students =  studentRepository.getAll();
        Log.i("onCreate after",students.toString());
        mDevices = new ArrayList<>();
        mAdapter = new DeviceListAdapter(R.layout.device_list_item, mDevices);
        stringBuilder = new StringBuilder();
        devieslist = findViewById(R.id.parent_r1);
        deviesinfo = findViewById(R.id.parent_r2);
        progressBar = findViewById(R.id.progressbar);
        recycleView = findViewById(R.id.blue_rv);
        recycleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        contextView = findViewById(R.id.context);
        statusView = findViewById(R.id.status);
        recycleView.setAdapter(mAdapter);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(MainActivity.this, "shouldShowRequestPermissionRationale", Toast.LENGTH_SHORT).show();
                }
            }
        }
        initBlueManager();
        initLisetener();

    }

    /**
     * ????????????????????????????????????
     */
    public void initBlueManager() {
        onSearchDeviceListener = new OnSearchDeviceListener() {
            @Override
            public void onStartDiscovery() {
                sendMessage(0, "??????????????????..");
                Log.d(TAG, "onStartDiscovery()");

            }

            @Override
            public void onNewDeviceFound(BluetoothDevice device) {
                Log.d(TAG, "new device: " + device.getName() + " " + device.getAddress());
            }

            @Override
            public void onSearchCompleted(List<SearchResult> bondedList, List<SearchResult> newList) {
                Log.d(TAG, "SearchCompleted: bondedList" + bondedList.toString());
                Log.d(TAG, "SearchCompleted: newList" + newList.toString());
                sendMessage(0, "????????????,???????????????????????????");
                mDevices.clear();
                mDevices.addAll(newList);
                mAdapter.notifyDataSetChanged();
                deviesinfo.setVisibility(View.GONE);
                devieslist.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception e) {
                sendMessage(0, "????????????");
            }
        };
        onConnectListener = new OnConnectListener() {
            @Override
            public void onConnectStart() {
                sendMessage(0, "????????????");
                Log.i("blue", "onConnectStart");
            }

            @Override
            public void onConnectting() {
                sendMessage(0, "????????????..");
                Log.i("blue", "onConnectting");
            }

            @Override
            public void onConnectFailed() {
                sendMessage(0, "???????????????");
                Log.i("blue", "onConnectFailed");

            }

            @Override
            public void onConectSuccess(String mac) {
                sendMessage(4, "???????????? MAC: " + mac);
                Log.i("blue", "onConectSuccess");
            }

            @Override
            public void onError(Exception e) {
                sendMessage(0, "???????????????");
                Log.i("blue", "onError");
            }
        };
        onSendMessageListener = new OnSendMessageListener() {
            @Override
            public void onSuccess(int status, String response) {
                sendMessage(0, "???????????????");
                Log.i("blue", "send message is success ! ");
            }

            @Override
            public void onConnectionLost(Exception e) {
                sendMessage(0, "???????????????");
                Log.i("blue", "send message is onConnectionLost ! ");
            }

            @Override
            public void onError(Exception e) {
                sendMessage(0, "???????????????");
                Log.i("blue", "send message is onError ! ");
            }
        };
        onReceiveMessageListener = new OnReceiveMessageListener() {


            @Override
            public void onProgressUpdate(String what, int progress) {
                sendMessage(1, what);
            }

            @Override
            public void onDetectDataUpdate(String what) {
                sendMessage(3, what);
            }

            @Override
            public void onDetectDataFinish() {
                sendMessage(2, "???????????????");
                Log.i("blue", "receive message is onDetectDataFinish");
            }

            @Override
            public void onNewLine(String s) {
                sendMessage(3, s);
            }

            @Override
            public void onConnectionLost(Exception e) {
                sendMessage(0, "????????????");
                Log.i("blue", "receive message is onConnectionLost ! ");
            }

            @Override
            public void onError(Exception e) {
                Log.i("blue", "receive message is onError ! ");
            }
        };
        bluemanage = BlueManager.getInstance(getApplicationContext());
        bluemanage.setOnSearchDeviceListener(onSearchDeviceListener);
        bluemanage.setOnConnectListener(onConnectListener);
        bluemanage.setOnSendMessageListener(onSendMessageListener);
        bluemanage.setOnReceiveMessageListener(onReceiveMessageListener);
        bluemanage.requestEnableBt();
    }

    /**
     * ???????????????????????????
     */
    public void initLisetener() {

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String mac = mDevices.get(position).getAddress();
                bluemanage.connectDevice(mac);
            }
        });

        findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluemanage.setReadVersion(false);
                bluemanage.searchDevices();
            }
        });

        findViewById(R.id.get_sn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBean item = new MessageBean(TypeConversion.getDeviceVersion());
                bluemanage.setReadVersion(true);
                bluemanage.sendMessage(item, true);
            }
        });

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluemanage.closeDevice();
                contextView.setText(null);
                devieslist.setVisibility(View.VISIBLE);
                deviesinfo.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluemanage.setReadVersion(false);
                progress = 0;
                progressBar.setProgress(progress);
                stringBuilder.delete(0, stringBuilder.length());
                contextView.setText("");
                MessageBean item = new MessageBean(TypeConversion.startDetect());
                bluemanage.sendMessage(item, true);
            }
        });
    }

    /**
     * @param type    0 ????????????  1 ????????????  2 ????????????  3 ??????????????????
     * @param context
     */
    public void sendMessage(int type, String context) {
        if (handler != null) {
            Message message = handler.obtainMessage();
            message.what = type;
            message.obj = context;
            handler.sendMessage(message);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2) {
            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.
                        permission.ACCESS_COARSE_LOCATION)) {
                    return;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluemanage != null) {
            bluemanage.close();
            bluemanage = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

    }
}