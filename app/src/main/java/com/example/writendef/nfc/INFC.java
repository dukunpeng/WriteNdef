package com.example.writendef.nfc;


import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Mark
 * @Date on 2019/1/30
 **/
public interface INFC {

    void enableForegroundDispatch(AppCompatActivity activity);

    void disableForegroundDispatch();

}
