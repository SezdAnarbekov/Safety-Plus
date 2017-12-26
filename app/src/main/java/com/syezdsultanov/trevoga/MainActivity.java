package com.syezdsultanov.trevoga;

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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 111;
    private static final int PICK_NUMBER_REQUEST = 222;
    private static final int REQUEST_READ_CONTACTS = 333;
    private static final int REQUEST_LOCATION = 444;
    private static final int REQUEST_SEND_SMS = 555;
    private EditText mSmsEditText, mContactsEditText;
    private StringBuilder numbers = new StringBuilder("");
    private String contactName, contactNumber;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        mContactsEditText = findViewById(R.id.contactsName);
        if (!TextUtils.isEmpty(pref.getString("name", ""))) {
            mContactsEditText.setText(pref.getString("name", ""));
        }
        mContactsEditText.setKeyListener(null);
        mSmsEditText = findViewById(R.id.messageText);
        if (!TextUtils.isEmpty(pref.getString("text", ""))) {
            mSmsEditText.setText(pref.getString("text", ""));
        }
        if (!TextUtils.isEmpty(pref.getString("number", ""))) {
            numbers = new StringBuilder(pref.getString("number", ""));
        }
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSmsPermission();
                Intent starIntent = new Intent(MainActivity.this, JoystickService.class);
                if (mContactsEditText.getText().toString().length() < 3) {
                    Toast.makeText(MainActivity.this, "Please enter a valid number.", Toast.LENGTH_SHORT).show();
                } else {
                    saveValues();
                    startService(starIntent);
                }
            }
        });
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopIntent = new Intent(MainActivity.this, JoystickService.class);
                stopService(stopIntent);
            }

        });
        final ImageView contactsPhoto = findViewById(R.id.contacts_photo);
        contactsPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                if (count == 3) {
                    mSmsEditText.requestFocus();
                    count = 0;
                }
                getReadContactPermission();
                pickContact();
            }
        });

        final ImageView deletePhoto = findViewById(R.id.delete_photo);
        deletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteContact();
            }
        });
        checkPermission();
        getLocationPermission();
    }

    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    private void getSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SEND_SMS);
        }
    }

    private void getReadContactPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length < 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Please, enter phone number manually.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case REQUEST_LOCATION: {
                if (grantResults.length < 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location won't send to Emergency cotact", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            case REQUEST_SEND_SMS: {
                if (grantResults.length < 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "SMS won't send to Emergency cotact", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_NUMBER_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (resultCode == RESULT_CANCELED) {
                }
            }
        }
        if (requestCode == PICK_NUMBER_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri contactData = data.getData();
            Cursor contact = getContentResolver().query(contactData, null, null, null, null);
            if (contact.moveToFirst()) {
                //contact name
                contactName = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                        "DISPLAY_NAME = '" + contactName + "'", null, null);
                if (cursor.moveToFirst()) {
                    String contactId =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    //  Get all phone numbers.
                    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                    while (phones.moveToNext()) {
                        contactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phones.close();
                }
                cursor.close();
            }
            contact.close();
            if (mContactsEditText.length() > 0) {
                mContactsEditText.append("," + contactName);
                numbers.append(",").append(contactNumber);
            } else {
                mContactsEditText.setText(contactName);
                // numbers = new StringBuilder("");
                numbers.append(contactNumber);
            }
        }
        if (requestCode == PICK_NUMBER_REQUEST && resultCode == RESULT_CANCELED) {
            contactName = "";
        }
    }

    private void deleteContact() {
        if (mContactsEditText.length() > 0 && mContactsEditText.getText().toString().contains(",")) {
            String temp = mContactsEditText.getText().toString();
            temp = temp.substring(0, temp.lastIndexOf(','));
            mContactsEditText.setText(temp);
            numbers = new StringBuilder(numbers.substring(0, numbers.lastIndexOf(",")));
        } else {
            mContactsEditText.setText("");
            numbers = new StringBuilder("");
        }

    }

    private void saveValues() {
        SharedPreferences pref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("number", numbers.toString());
        edit.putString("name", mContactsEditText.getText().toString());
        edit.putString("text", mSmsEditText.getText().toString());
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
