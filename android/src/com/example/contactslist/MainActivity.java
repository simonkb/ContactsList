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

import java.util.concurrent.Executors;

public class MainActivity extends QtActivity {
    public long threadPointer;
    public long lastFetchTimestamp = 0;
    public native void onContactsLoaded(String str, long ptr);
    public native void onContactsChanged(String str, long ptr);
    private ContentObserver contactsObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setupContactsObserver();

    }
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS}, 1);
        }
    }
    private void setupContactsObserver() {
        contactsObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                fetchUpdatedContacts();
            }
        };
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactsObserver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(contactsObserver);
    }
    public void fetchContacts(long ptr) {
        Executors.newSingleThreadExecutor().execute(() -> {
            this.threadPointer = ptr;
            JSONArray contactsArray = new JSONArray();
            ContentResolver contentResolver = getContentResolver();

            try (Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")) {
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        @SuppressLint("Range")
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        @SuppressLint("Range")
                        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                        JSONObject contact = new JSONObject();
                        contact.put("name", name);
                        contact.put("phoneNumber", phoneNumber);
                        contact.put("contactId", contactId);
                        contact.put("deleted", 0);

                        contactsArray.put(contact);
                    }
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error fetching contacts", e);
            }

            onContactsLoaded(contactsArray.toString(), ptr);
            lastFetchTimestamp = System.currentTimeMillis();
        });
    }
    public void fetchUpdatedContacts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            JSONArray contactsArray = new JSONArray();
            ContentResolver contentResolver = getContentResolver();

            try (Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP + " > ?",
                    new String[]{String.valueOf(lastFetchTimestamp)}, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")) {
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        @SuppressLint("Range")
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        @SuppressLint("Range")
                        String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                        JSONObject contact = new JSONObject();
                        contact.put("name", name);
                        contact.put("phoneNumber", phoneNumber);
                        contact.put("contactId", contactId);
                        contact.put("deleted", 0);
                        contactsArray.put(contact);
                    }
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error fetching updated contacts", e);
            }
            if (contactsArray.length() == 0 ){
                String selection = ContactsContract.DeletedContacts.CONTACT_DELETED_TIMESTAMP + " > ?";
                String[] selectionArgs = new String[]{String.valueOf(lastFetchTimestamp)};

               try( Cursor cursor = contentResolver.query(ContactsContract.DeletedContacts.CONTENT_URI, null, selection, selectionArgs, null);){
                   if (cursor != null && cursor.getCount() > 0) {
                       while (cursor.moveToNext()) {
                           @SuppressLint("Range")
                           String name = "";

                           @SuppressLint("Range")
                           String phoneNumber = "";
                           @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_ID));
                           JSONObject contact = new JSONObject();
                           contact.put("name", name);
                           contact.put("phoneNumber", phoneNumber);
                           contact.put("contactId", contactId);
                           contact.put("deleted", 1);
                           contactsArray.put(contact);
                       }
                   }
               } catch (Exception e){
                   Log.e("MainActivity", "Error fetching updated contacts", e);
               }

            }

            onContactsChanged(contactsArray.toString(), this.threadPointer);
            lastFetchTimestamp = System.currentTimeMillis();

        });
    }
}
