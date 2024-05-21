package com.example.contactslist;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONArray;

import org.qtproject.qt.android.bindings.QtActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import java.util.concurrent.Executors;

public class MainActivity extends QtActivity {
    public native void onContactsLoaded(String str, long ptr);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
    }
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS}, 1);
         }
    }
    public void fetchContacts(long ptr) {
        Executors.newSingleThreadExecutor().execute(() -> {

            JSONArray contactsArray = new JSONArray();
            ContentResolver contentResolver = getContentResolver();

            try (Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)) {
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        @SuppressLint("Range")
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        @SuppressLint("Range")
                        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        JSONObject contact = new JSONObject();
                        contact.put("name", name);
                        contact.put("phoneNumber", phoneNumber);

                        contactsArray.put(contact);
                    }
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error fetching contacts", e);
            }

            onContactsLoaded(contactsArray.toString(), ptr);
        });
    }
}
