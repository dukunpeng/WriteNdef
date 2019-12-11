package com.example.writendef.nfc;

import android.app.PendingIntent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

/**
 * @author Mark
 * @Date on 2019/1/30
 **/
public class NFCStevedore  implements INFC,  NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    private final static String TAG = NFCStevedore.class.getSimpleName();

    private PendingIntent mPendingIntent;

    private EditText editText;
    private WeakReference<AppCompatActivity> weakReference;

    private NfcAdapter mNfcAdapter;

    private volatile static NFCStevedore instance;

    public static NFCStevedore getInstance(){
        if (instance==null){
            synchronized (NFCStevedore.class){
                if (instance==null){
                instance = new NFCStevedore();
                }
            }
        }
        return instance;
    }
    public NFCStevedore assemblePendingIntent(PendingIntent pendingIntent){
        mPendingIntent = pendingIntent;
        return this;
    }

    public NFCStevedore assembleOriginalMessageEdit(EditText editText){
        this.editText  = editText;
        return  this;
    }

    private AppCompatActivity getActivity(){
        if (weakReference==null){
            return null;
        }
        return weakReference.get();
    }
    @Override
    public void enableForegroundDispatch(AppCompatActivity activity) {
        weakReference = new WeakReference<>(activity);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        mNfcAdapter.setNdefPushMessageCallback(this, getActivity());
        mNfcAdapter.setOnNdefPushCompleteCallback(this, getActivity());

        if (mNfcAdapter!=null){

            mNfcAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, null,
                    null);
        }
    }
    @Override
    public void disableForegroundDispatch() {
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(getActivity());
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String message = editText.getText().toString();
        if (TextUtils.isEmpty(message)){
            message = "无消息传输";
        }
        Log.d(TAG, "createNdefMessage="+message);
        NdefMessage ndefMessage = new NdefMessage(
                new NdefRecord[] { NfcUtils.createTextRecord(message) });

        return ndefMessage;
    }

    @Override
    /** 窗口处理完成 */
    public void onNdefPushComplete(NfcEvent event) {
        Log.d(TAG, "onNdefPushComplete");
    }




}
