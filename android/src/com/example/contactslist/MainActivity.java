package com.example.contactslist;
import android.content.ContentProviderOperation;
import android.content.Context;
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

import java.util.ArrayList;
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
        setupContactsObserver();

    }
    private void setupContactsObserver() {
        contactsObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri, int flag) {
                super.onChange(selfChange, uri);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
            return;
        }
        executor.execute(() -> {
            this.threadPointer = ptr;
            onContactsLoaded(fetch("load"), ptr);
            lastFetchTimestamp = System.currentTimeMillis();
        });
    }
    public void fetchUpdatedContacts(int flag) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
            return;
        }
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
            return;
        }
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

    public static void addContact(Context context, String phoneNumber, String fullName) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        if (fullName != null) {
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
                    .build());
        }

        if (phoneNumber != null) {
            operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
            Log.d("MainActivity", "Contact Added");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void editContact(Context context, String contactId, String fullName, String phoneNumber) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        if (fullName != null) {
            operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " +
                                    ContactsContract.Data.MIMETYPE + "=?",
                            new String[]{String.valueOf(contactId),
                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullName)
                    .build());
        }

        if (phoneNumber != null) {
            operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " +
                                    ContactsContract.Data.MIMETYPE + "=? AND " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE + "=?",
                            new String[]{String.valueOf(contactId),
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                    String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)})
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addOrUpdateContact(String contactJson, String action) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
            return;
        }
        try {
            JSONObject contactObject = new JSONObject(contactJson);
            String name = contactObject.getString("name");
            String phoneNumber = contactObject.getString("phoneNumber");
            String contactId = contactObject.getString("contactId");
            if ("edit".equals(action)) {
               editContact(this, contactId, name, phoneNumber);
            } else if ("add".equals(action)) {
               addContact(this, phoneNumber, name);
            } else {
                Log.e("MainActivity", "Invalid action: " + action);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error adding or updating contact", e);
        }
    }
}
