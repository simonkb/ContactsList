package com.example.contactslist;

import android.content.ContentProviderOperation;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

interface ExecutorCallback {
    void onComplete(String result);
}

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
        if (checkPermission())
            setupContactsObserver();
    }

    public boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 1);
        } else {
            return true;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Permission granted");
        } else {
            Log.d("MainActivity", "Permission not granted");
        }
    }

    private void setupContactsObserver() {
        if (checkPermission()) {
            contactsObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange, Uri uri, int flag) {
                    super.onChange(selfChange, uri);
                    fetchUpdatedContacts(flag);
                }
            };
            getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactsObserver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(contactsObserver);
    }

    @SuppressLint("Range")
    public void fetch(String action, ExecutorCallback callback) {
        executor.execute(() -> {
            JSONArray contactsArray = new JSONArray();
            if (checkPermission()) {
                ContentResolver contentResolver = getContentResolver();
                try (Cursor cursor = getCursorForAction(contentResolver, action)) {
                    if (cursor != null && cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            String name = "";
                            String phoneNumber = "";
                            String contactId = "";
                            JSONObject contact = new JSONObject();
                            if ("deleted".equals(action)) {
                                contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.DeletedContacts.CONTACT_ID));
                            } else {
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
            }
            callback.onComplete(contactsArray.toString());
        });
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
        fetch("load", result -> {
            this.threadPointer = ptr;
            onContactsLoaded(result, ptr);
            lastFetchTimestamp = System.currentTimeMillis();
        });
    }

    public void fetchUpdatedContacts(int flag) {
        String action = (flag == 0) ? "deleted" : "updated";
        fetch(action, result -> {
            onContactsChanged(result, this.threadPointer, action);
            lastFetchTimestamp = System.currentTimeMillis();
        });
    }

    public void deleteContacts(String contacts) {
        if (checkPermission()) {
            executor.execute(() -> {
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

    public void saveContact(String contactJson, String action) {
        if (checkPermission()) {
            try {
                JSONObject contactObject = new JSONObject(contactJson);
                String name = contactObject.getString("name");
                String phoneNumber = contactObject.getString("phoneNumber");
                String contactId = contactObject.getString("contactId");
                ArrayList<ContentProviderOperation> operations = new ArrayList<>();
                if (action.equals("edit")) {
                    operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                            .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " +
                                            ContactsContract.Data.MIMETYPE + "=?",
                                    new String[]{String.valueOf(contactId),
                                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                            .build());


                    operations.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                            .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " +
                                            ContactsContract.Data.MIMETYPE + "=? AND " +
                                            ContactsContract.CommonDataKinds.Phone.TYPE + "=?",
                                    new String[]{String.valueOf(contactId),
                                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                            String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)})
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                            .build());
                } else if (action.equals("add")) {
                    operations.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build());

                    operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                            .build());

                    operations.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build());
                } else {
                    Log.e("MainActivity", "Invalid Action");
                    return;
                }
                try {
                    this.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error adding or updating contact", e);
            }
        }
    }

}
