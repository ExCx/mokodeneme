package com.tagofid.mokodeneme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.MQTTSupport;
import com.moko.support.MokoBleScanner;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;
import com.tagofid.mokodeneme.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MokoScanDeviceCallback {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private boolean isScanning = false;
    public MokoBleScanner mokoBleScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // initialization
        checkPermissions();
        EventBus.getDefault().register(this);
        XLog.init(LogLevel.ALL);
        MokoSupport.getInstance().init(this);
        MQTTSupport.getInstance().init(this);
        mokoBleScanner = new MokoBleScanner(this);

        binding.fab.setOnClickListener(view -> {
            if (isScanning)
                mokoBleScanner.stopScanDevice();
            else
                mokoBleScanner.startScanDevice(this);
            isScanning = !isScanning;
        });
    }

    protected void checkPermissions() {
        final String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            int result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED)
                listPermissionsNeeded.add(p);
        }
        if (!listPermissionsNeeded.isEmpty())
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), 100);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            Toast.makeText(this, "Cihaza bağlanılamadı", Toast.LENGTH_LONG).show();
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            Toast.makeText(this, "Cihaza bağlandı", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(() -> {
                ArrayList<OrderTask> orderTasks = new ArrayList<>();
                orderTasks.add(OrderTaskAssembler.setPassword("Moko4321"));
                orderTasks.add(OrderTaskAssembler.getDeviceMac());
                orderTasks.add(OrderTaskAssembler.getDeviceName());
                orderTasks.add(OrderTaskAssembler.setMqttHost("185.141.33.75"));
                orderTasks.add(OrderTaskAssembler.setMqttPort(1883));
                orderTasks.add(OrderTaskAssembler.setMqttClientId("frekanstantest2"));
                orderTasks.add(OrderTaskAssembler.setMqttCleanSession(1));
                orderTasks.add(OrderTaskAssembler.setMqttQos(1));
                orderTasks.add(OrderTaskAssembler.setMqttKeepAlive(60));
//                orderTasks.add(OrderTaskAssembler.setWifiSSID("TTNET_ZyXEL_HFAF"));
//                orderTasks.add(OrderTaskAssembler.setWifiPassword("iblis9000"));
                orderTasks.add(OrderTaskAssembler.setWifiSSID("INCA-N"));
                orderTasks.add(OrderTaskAssembler.setWifiPassword(""));
                orderTasks.add(OrderTaskAssembler.setMqttDeivceId("frekanstantestdevice2"));
                orderTasks.add(OrderTaskAssembler.setMqttPublishTopic("frekanstan/test"));
                //orderTasks.add(OrderTaskAssembler.setMqttSubscribeTopic(mqttDeviceConfig.topicSubscribe));
                orderTasks.add(OrderTaskAssembler.setMqttUserName("frekanstanmqtt"));
                orderTasks.add(OrderTaskAssembler.setMqttPassword("mq33261978tt"));
                orderTasks.add(OrderTaskAssembler.setMqttConnectMode(0));
                /*if (mqttDeviceConfig.connectMode == 2) {
                    File file = new File(mqttDeviceConfig.caPath);
                    orderTasks.add(OrderTaskAssembler.setCA(file));
                } else if (mqttDeviceConfig.connectMode == 3) {
                    File caFile = new File(mqttDeviceConfig.caPath);
                    orderTasks.add(OrderTaskAssembler.setCA(caFile));
                    File clientKeyFile = new File(mqttDeviceConfig.clientKeyPath);
                    orderTasks.add(OrderTaskAssembler.setClientKey(clientKeyFile));
                    File clientCertFile = new File(mqttDeviceConfig.clientCertPath);
                    orderTasks.add(OrderTaskAssembler.setClientCert(clientCertFile));
                }
                if (!TextUtils.isEmpty(mqttDeviceConfig.ntpUrl)) {
                    orderTasks.add(OrderTaskAssembler.setNTPUrl(mqttDeviceConfig.ntpUrl));
                }*/
                //orderTasks.add(OrderTaskAssembler.setNTPTimezone(3));
                orderTasks.add(OrderTaskAssembler.exitConfigMode());
                try {
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                } catch(Exception ex){
                    ex.fillInStackTrace();
                }
            }, 1000);
        }
    }

    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        XLog.d("action: " + event.getAction() + " char: " + event.getResponse().orderCHAR + " type: " + event.getResponse().responseType + " value: " + event.getResponse().responseValue);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onStartScan() {
        XLog.d("start");
    }

    @Override
    public void onScanDevice(DeviceInfo device) {
        XLog.d("scanned");
        mokoBleScanner.stopScanDevice();
        /*if (!MokoSupport.getInstance().isBluetoothOpen()) {
            MokoSupport.getInstance().enableBluetooth();
            return;
        }*/
        new Handler().postDelayed(() ->
                MokoSupport.getInstance().connDevice(device.mac), 1000);
    }

    @Override
    public void onStopScan() {
        XLog.d("stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}