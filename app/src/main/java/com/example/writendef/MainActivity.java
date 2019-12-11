package com.example.writendef;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.writendef.nfc.NFCStevedore;
import com.example.writendef.nfc.NfcUtils;

import java.io.IOException;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    public EditText edit;
    public TextView submit;
    public TextView uri_submit;

    private Tag tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkNfcBeam();
        edit = findViewById(R.id.edit);
        edit.setTextColor(Color.BLACK);
        submit = findViewById(R.id.submit);
        uri_submit = findViewById(R.id.uri_submit);
        submit.setEnabled(false);
        uri_submit.setEnabled(false);
        submit.setBackgroundResource(R.drawable.circle_gray_shape);
        uri_submit.setBackgroundResource(R.drawable.circle_gray_shape);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeNFC(tag,NdefRecord.RTD_TEXT);
                setFalse();
            }
        });
        uri_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeNFC(tag,NdefRecord.RTD_URI);
                setFalse();
            }
        });
      //  Util.setView(this,new DragFloatActionButton(this));
    }
   void  setFalse(){
       submit.setEnabled(false);
       submit.setBackgroundResource(R.drawable.circle_gray_shape);
       uri_submit.setEnabled(false);
       uri_submit.setBackgroundResource(R.drawable.circle_gray_shape);
    }
   void  setTrue(){
       submit.setEnabled(true);
       submit.setBackgroundResource(R.drawable.circle_blue_shape);
       uri_submit.setEnabled(true);
       uri_submit.setBackgroundResource(R.drawable.circle_blue_shape);
    }
    //接收到对面信息传输
    @Override
    public void onNewIntent(Intent intent) {
        //1.获取Tag对象
         tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
         edit.setTextColor(Color.BLACK);
        setTrue();
        //解析数据
        String  data = NfcUtils.readNfcTag(intent);

        Log.e("NFC","收到的信息："+data);
        Toast.makeText(this,"收到的信息："+data,Toast.LENGTH_LONG).show();
    }

    //NFC写入
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void writeNFC(Tag tag,byte[] RTD) {
        String data = edit.getText().toString();
        if (TextUtils.isEmpty(data)) {
            return;
        }

        //null不执行操作，强调写程序的逻辑性
        if (tag == null) {
            return;
        }
        NdefMessage ndefMessage;
        Uri uri = Uri.parse(data);

        if (RTD==NdefRecord.RTD_TEXT){
            ndefMessage = new NdefMessage(new NdefRecord[]{NfcUtils.createTextRecord(data),NdefRecord.createUri(uri)/*NdefRecord.createTextRecord("en", data)*/});
        }else if(RTD==NdefRecord.RTD_URI){
            ndefMessage = new NdefMessage(NdefRecord.createUri(uri),new NdefRecord[]{NfcUtils.createTextRecord(data),NfcUtils.createTextRecord(data)/*NdefRecord.createTextRecord("en", data)*/});
        }else {
            ndefMessage = new NdefMessage(new NdefRecord[]{NfcUtils.createTextRecord(data)/*NdefRecord.createTextRecord("en", data)*/});

        }

        //获得写入大小
        int size = ndefMessage.toByteArray().length;
        //2.判断是否是NDEF标签
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                NdefFormatable format = NdefFormatable.get(tag);
                format.connect();
                format.format(ndefMessage);
                if (format.isConnected()) {
                    format.close();
                }
            }
            if (ndef != null) {
                //说明是NDEF标签,开始连接
                ndef.connect();
                //判断是否可写
                if (!ndef.isWritable()) {
                    Toast.makeText(this, "当前设备不支持写入", Toast.LENGTH_LONG).show();
                    return;
                }
                //判断大小
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(this, "容量太小了", Toast.LENGTH_LONG).show();
                    return;
                }
                //写入
                try {
                    ndef.writeNdefMessage(ndefMessage);
                    Toast.makeText(this, "写入成功", Toast.LENGTH_LONG).show();
                    edit.setTextColor(Color.GREEN);

                } catch (FormatException e) {
                    e.printStackTrace();
                    edit.setTextColor(Color.RED);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            edit.setTextColor(Color.RED);
        } catch (FormatException e) {
            e.printStackTrace();
            edit.setTextColor(Color.RED);
        }
    }
    @Override
    protected void onResume() {
        //启动NFC自动管理
        NFCStevedore.getInstance()
                .assemblePendingIntent(
                        PendingIntent.getActivity(
                                this, 0, new Intent(this,
                                        getClass()), 0))
                .assembleOriginalMessageEdit(edit)
                .enableForegroundDispatch(this);
        if (nfcAdapter!=null){

            nfcAdapter.enableForegroundDispatch(this,  PendingIntent.getActivity(
                    this, 0, new Intent(this,
                            getClass()), 0), mWriteTagFilters,
                    mTechLists);
        }
        super.onResume();
    }
    PendingIntent pendingIntent;
    IntentFilter[] mWriteTagFilters;
    String[][] mTechLists;
    NfcAdapter  nfcAdapter;
    private void checkNfcBeam() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter !=null){
            if (!nfcAdapter.isEnabled()){
                //支持NFC，但是没有打开
                Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
                startActivity(intent);
            }else if(!nfcAdapter.isNdefPushEnabled()){
                //API 16+
                Intent intent = new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
                startActivity(intent);
            }
        }
        if (nfcAdapter == null) {
            //  shotToast("当前设备不支持NFC功能");
        } else if (!nfcAdapter.isEnabled()) {
            // shotToast("NFC功能未打开，请先开启后重试！");
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        ndef.addCategory("*/*");
        // 允许扫描的标签类型
        mWriteTagFilters = new IntentFilter[]{ndef};
        mTechLists = new String[][]{
                new String[]{MifareClassic.class.getName()},
                new String[]{NfcA.class.getName()}};// 允许扫描的标签类型




        /////////////////////

    }

    @Override
    protected void onPause() {
        super.onPause();
        NFCStevedore.getInstance().disableForegroundDispatch();
    }

}
