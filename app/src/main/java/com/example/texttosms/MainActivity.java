package com.example.texttosms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView sms;
    private final int CODE = 123;
    private final int requestCODE = 1;
    private Button btnSms;
    private EditText inputPhoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSms = (Button) findViewById(R.id.btnSend);
        sms = (TextView) findViewById(R.id.textInput);
        inputPhoneNum = (EditText) findViewById(R.id.mblTxt);
        requestSmsPermission(); // before using app, grant all permissions from user.
    }


    // **************  RECORDING SPEECH IN ORDER TO SEND AS SMS  **************
    public void recordSpeech(View view) {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) { // check if phone has proper activity to record speech.
            startActivityForResult(intent, CODE);

        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sms.setText(result.get(0));
                showSendButton();
            }
        }

        if (requestCode == requestCODE){
            if (resultCode == RESULT_OK){
                String phoneNum = data.getStringExtra("no");
                System.out.println(phoneNum);
                inputPhoneNum.setText(phoneNum);
            }
        }
    }

    // It will display the activity that consists of the list of contacts.
    public void showContacts(View view) {
        System.out.println("********\nCONTACTS\n********");
        Intent intent = new Intent(this, contacts.class);
        startActivityForResult(intent, requestCODE);
    }

    public void clearSMS(View view) {
        sms.setText("");
        sms.setHint("Message: ");
        inputPhoneNum.setText("");
        hideSendButton();
    }

    private void requestSmsPermission() {
        String permission = Manifest.permission.SEND_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }

    // this function is cont of the "requestSmsPermission" function.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,"permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 17.08.2019 -> Permission issue is solved through request permission via Java code as above(requestSmsPermission() function).
    public void sendMessage(View view) {

        btnSms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try{
                    SmsManager smgr = SmsManager.getDefault();
                    smgr.sendTextMessage(inputPhoneNum.getText().toString(),null,sms.getText().toString(),null,null);
                    Toast.makeText(MainActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void sendSMS() {
        try{
            SmsManager smgr = SmsManager.getDefault();
            smgr.sendTextMessage(inputPhoneNum.getText().toString(),null,sms.getText().toString(),null,null);
            Toast.makeText(MainActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
        }
    }


    public void showSendButton(){
        if (sms.length() > 0)
        {
            btnSms.setVisibility(View.VISIBLE);
        }
    }

    public void hideSendButton(){
        btnSms.setVisibility(View.GONE);
    }

    public void openDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        })
                .setTitle("Sending Sms").setMessage("Are you sure to send sms?")
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSMS();
                    }
                }).show();
    }
}
