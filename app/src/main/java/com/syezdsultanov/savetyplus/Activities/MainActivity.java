package com.syezdsultanov.savetyplus.Activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.syezdsultanov.savetyplus.JoystickService;
import com.syezdsultanov.savetyplus.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 111;
    private static final int PICK_NUMBER_REQ_CODE = 222;
    private static final int READ_CONTACTS_PERMISSION_REQ_CODE = 333;
    private static final int LOCATION_PERMISSION_REQ_CODE = 444;
    private static final int SEND_SMS_PERMISSION_REQ_CODE = 555;
    private static final int RECORD_AUDIO_PERMISSION_REQ_CODE = 777;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQ_CODE = 888;
    @BindView(R.id.contactsName)
    EditText mContactsName;
    @BindView(R.id.contacts_photo)
    ImageView mContactsPhoto;
    @BindView(R.id.delete_photo)
    ImageView mDeletePhoto;
    @BindView(R.id.messageText)
    EditText mMessageText;
    @BindView(R.id.startButton)
    Button mStartButton;
    @BindView(R.id.stopButton)
    Button mStopButton;
    private StringBuilder numbers = new StringBuilder("");
    private String contactName, contactNumber;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (!TextUtils.isEmpty(pref.getString("name", ""))) {
            mContactsName.setText(pref.getString("name", ""));
        }
        mContactsName.setKeyListener(null);
        if (!TextUtils.isEmpty(pref.getString("text", ""))) {
            mMessageText.setText(pref.getString("text", ""));
        }
        if (!TextUtils.isEmpty(pref.getString("number", ""))) {
            numbers = new StringBuilder(pref.getString("number", ""));
        }
        getOverlayPermission();
        getLocationPermission();
        getSmsPermission();
        getWriteExternalStorage();
    }

    @OnClick({R.id.contacts_photo, R.id.delete_photo, R.id.startButton, R.id.stopButton})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.contacts_photo:
                count++;
                if (count == 3) {
                    mMessageText.requestFocus();
                    count = 0;
                }
                getReadContactPermission();
                getContactNumber();
                break;
            case R.id.delete_photo:
                deleteContact();
                break;
            case R.id.startButton:
                getRecordAudioPermission();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        Toast.makeText(MainActivity.this,
                                "Safety Plus needs Overlay permission", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                Intent starIntent = new Intent(MainActivity.this, JoystickService.class);
                if (mContactsName.getText().toString().length() < 3) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    saveValues();
                    startService(starIntent);
                }
                break;
            case R.id.stopButton:
                Intent stopIntent = new Intent(MainActivity.this, JoystickService.class);
                stopService(stopIntent);
                break;
        }
    }

    private void getWriteExternalStorage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_PERMISSION_REQ_CODE);
        }
    }

    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQ_CODE);
        }
    }

    private void getSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    SEND_SMS_PERMISSION_REQ_CODE);
        }
    }

    private void getReadContactPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    READ_CONTACTS_PERMISSION_REQ_CODE);
        }
    }

    private void getRecordAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_CONTACTS_PERMISSION_REQ_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Safety Plus needs to access contacts",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
            case LOCATION_PERMISSION_REQ_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location will not sent to Emergency contact",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case SEND_SMS_PERMISSION_REQ_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Sms will not sent to Emergency contact.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            case RECORD_AUDIO_PERMISSION_REQ_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Audio will not recorded.",
                            Toast.LENGTH_SHORT).show();
                }
            }
            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQ_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Audio will not stored.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getOverlayPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
    }

    private void getContactNumber() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_NUMBER_REQ_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d("TAG", "DENIYED");
                finish();
            }
        }
        if (requestCode == PICK_NUMBER_REQ_CODE && resultCode == RESULT_OK && data != null
                && ActivityCompat.checkSelfPermission(this, Manifest.permission
                .READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Uri contactData = data.getData();
            try {
                Cursor contact = getContentResolver().query(contactData, null, null,
                        null, null);
                if (contact.moveToFirst()) {
                    contactName = contact.getString(contact.getColumnIndex(ContactsContract
                            .Contacts.DISPLAY_NAME));
                    ContentResolver cr = getContentResolver();
                    Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                            "DISPLAY_NAME = '" + contactName + "'", null, null);
                    if (cursor.moveToFirst()) {
                        String contactId =
                                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                                        + contactId, null, null);
                        while (phones.moveToNext()) {
                            contactNumber = phones.getString(phones.getColumnIndex
                                    (ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        phones.close();
                    }
                    cursor.close();
                }
                contact.close();
            } catch (NullPointerException e) {
                return;
            }
            if (mContactsName.length() > 0) {
                mContactsName.append("," + contactName);
                numbers.append(",").append(contactNumber);
            } else {
                mContactsName.setText(contactName);
                // numbers = new StringBuilder("");
                numbers.append(contactNumber);
            }
        }
        if (requestCode == PICK_NUMBER_REQ_CODE && resultCode == RESULT_CANCELED) {
            contactName = "";
        }
    }

    private void deleteContact() {
        if (mContactsName.length() > 0 && mContactsName.getText().toString().contains(",")) {
            String temp = mContactsName.getText().toString();
            temp = temp.substring(0, temp.lastIndexOf(','));
            mContactsName.setText(temp);
            numbers = new StringBuilder(numbers.substring(0, numbers.lastIndexOf(",")));
        } else {
            mContactsName.setText("");
            numbers = new StringBuilder("");
        }
    }

    private void saveValues() {
        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("number", numbers.toString());
        edit.putString("name", mContactsName.getText().toString());
        edit.putString("text", mMessageText.getText().toString());
        edit.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveValues();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
