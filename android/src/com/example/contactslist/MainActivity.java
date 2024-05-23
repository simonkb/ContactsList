package com.example.contactslist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.net.Uri;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONObject;

import org.qtproject.qt.android.bindings.QtActivity;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends QtActivity {
    public long threadPointer;
    public long lastFetchTimestamp = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public native void onContactsLoaded(String str, long ptr);
    public native void onContactsChanged(String str, long ptr, String action);
    private ContentObserver contactsObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setupContactsObserver();

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
        }

    }
    private void setupContactsObserver() {
        contactsObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri, int flag) {
                super.onChange(selfChange, uri);
                // 0 for delete
                // 1 for edited and new contact
                fetchUpdatedContacts(flag);
            }
        };
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactsObserver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(contactsObserver);
    }
    @SuppressLint("Range")
    public String fetch(String action) {
        JSONArray contactsArray = new JSONArray();
        ContentResolver contentResolver = getContentResolver();

        try (Cursor cursor = getCursorForAction(contentResolver, action)) {
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String name = "";
                    String phoneNumber = "";
                    String contactId = "";
                    JSONObject contact = new JSONObject();

                    if("deleted".equals(action)){
                        contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_ID));

                    }else {
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    }
                    contact.put("name", name);
                    contact.put("phoneNumber", phoneNumber);
                    contact.put("contactId", contactId);
                    contact.put("selected", false);
                    contactsArray.put(contact);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error fetching contacts", e);
        }

        return contactsArray.toString();
    }

    private Cursor getCursorForAction(ContentResolver contentResolver, String action) {
        Uri uri;
        String selection;
        String[] selectionArgs;

        switch (action) {
            case "updated":
                uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                selection = ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP + " > ?";
                selectionArgs = new String[]{String.valueOf(lastFetchTimestamp)};
                break;
            case "deleted":
                uri = ContactsContract.DeletedContacts.CONTENT_URI;
                selection = ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP + " > ?";
                selectionArgs = new String[]{String.valueOf(lastFetchTimestamp)};
                break;
            default:
                uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                selection = null;
                selectionArgs = null;
                break;
        }
        return contentResolver.query(uri, null, selection, selectionArgs, null);
    }
        public void fetchContacts(long ptr) {
        executor.execute(() -> {
            this.threadPointer = ptr;
            runOnUiThread(()->{
                onContactsLoaded(fetch("load"), ptr);
            });
            lastFetchTimestamp = System.currentTimeMillis();
        });
    }
    public void fetchUpdatedContacts(int flag) {
        executor.execute(() -> {
            String action = (flag == 0) ? "deleted" : "updated";
            String contactsJson = fetch(action);
            runOnUiThread(() -> {
                onContactsChanged(contactsJson, this.threadPointer, action);
            });
            lastFetchTimestamp = System.currentTimeMillis();
        });
    }
    public void deleteSelectedContacts(String contacts){
        executor.execute(() ->{
            String[] contactIds = contacts.split(",");
            for (String contactId : contactIds) {
                ContentResolver contentResolver = this.getContentResolver();
                Uri contactUri = ContactsContract.Contacts.CONTENT_URI;
                Uri contactToDelete = Uri.withAppendedPath(contactUri, String.valueOf(contactId));

                int deleted = contentResolver.delete(contactToDelete, null, null);
                if (deleted > 0) {
                    Log.d("MainActivity", "Deleted: " + contactId);
                }
            }
        });


    }
}
