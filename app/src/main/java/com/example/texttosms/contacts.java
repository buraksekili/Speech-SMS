package com.example.texttosms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class contacts extends AppCompatActivity {
    String phoneNum;
    ListView contactsListView;  // store all phone contacts list.
                                // Each contact is form of " Name \r\n Phone Number \r\n Phone Type "
    private List<String> phoneContactsList = new ArrayList<String>();
    // This is the phone contacts list view's data adapter.
    private ArrayAdapter<String> contactsListDataAdapter;
    private int PERMISSION_REQUEST_CODE_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactsListView = (ListView) findViewById(R.id.contactsListView);
        contactsListDataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, phoneContactsList);
        contactsListView.setAdapter(contactsListDataAdapter);

        if(!hasPhoneContactsPermission(Manifest.permission.READ_CONTACTS))
        {
            requestPermission(Manifest.permission.READ_CONTACTS, PERMISSION_REQUEST_CODE_READ_CONTACTS);
        }else{
            readPhoneContacts();
        }
    }
    private boolean hasPhoneContactsPermission(String permission)
    {
        boolean permis = false;

        // If android sdk version is bigger than 23 the need to check run time permission.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // return phone read contacts permission grant status.
            int hasPermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            // If permission is granted then return true.
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                permis = true;
            }
        }else
        {
            permis = true;
        }
        return permis;
    }
    // Request a runtime permission to app user.
    private void requestPermission(String permission, int requestCode)
    {
        String requestPermissionArray[] = {permission};
        ActivityCompat.requestPermissions(this, requestPermissionArray, requestCode);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int length = grantResults.length;
        if(length > 0)
        {
            int grantResult = grantResults[0];

            if(grantResult == PackageManager.PERMISSION_GRANTED) {

                if(requestCode==PERMISSION_REQUEST_CODE_READ_CONTACTS)
                {
                    // If user grant read contacts permission.
                    readPhoneContacts();
                }
            }else
            {
                Toast.makeText(getApplicationContext(), "You denied permission.", Toast.LENGTH_LONG).show();
            }
        }
    }
    // Read and display android phone contacts in list view.
    private void readPhoneContacts()
    {

        StringBuffer tmp_contactStringBuf = new StringBuffer();

        // First empty current phone contacts list data.
        int size = phoneContactsList.size();
        for(int i=0;i<size;i++)
        {
            phoneContactsList.remove(i);
            i--;
            size = phoneContactsList.size();
        }

        // Get query phone contacts cursor object.
        Uri readContactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = getContentResolver().query(readContactsUri, null, null, null, null);

        if(cursor!=null)
        {
            cursor.moveToFirst();

            // Loop in the phone contacts cursor to add each contacts in phoneContactsList.
            do{
                // Get contact display name.
                int displayNameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String userDisplayName = cursor.getString(displayNameIndex);

                // Get contact phone number.
                int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = cursor.getString(phoneNumberIndex);

//                // Get contact phone type.
//                String phoneTypeStr = "Mobile";
//                int phoneTypeColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
//                int phoneTypeInt = cursor.getInt(phoneTypeColumnIndex);
//                if(phoneTypeInt== ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
//                {
//                    phoneTypeStr = "Home";
//                }else if(phoneTypeInt== ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
//                {
//                    phoneTypeStr = "Mobile";
//                }else if(phoneTypeInt== ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
//                {
//                    phoneTypeStr = "Work";
//                }

                StringBuffer contactStringBuf = new StringBuffer();
                contactStringBuf.append(userDisplayName);
                contactStringBuf.append("\r\n");
                contactStringBuf.append(phoneNumber);
//                contactStringBuf.append("\r\n");
//                contactStringBuf.append(phoneTypeStr);

                phoneContactsList.add(contactStringBuf.toString());
            }while(cursor.moveToNext());

            // Refresh the listview to display read out phone contacts.
            contactsListDataAdapter.notifyDataSetChanged();
            contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    System.out.println(phoneContactsList.get(position));
                    System.out.println(phoneContactsList.get(position).length());
                    String[] lines = phoneContactsList.get(position).split(System.getProperty("line.separator"));
                    System.out.println(lines[1]);
                    phoneNum = lines[1].toString();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("no", phoneNum);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            });


        }
    }

    // search in contact list
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_contact);
        SearchView searchView = null;
        if (menuItem != null){
            searchView = (SearchView) menuItem.getActionView();
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactsListDataAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}