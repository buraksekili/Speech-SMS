package com.example.texttosms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class contacts extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ExampleAdapyer mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<ExampleItem> exampleList = new ArrayList<ExampleItem>();
    private int PERMISSION_REQUEST_CODE_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        System.out.println("********\nON CREATE\n********");

        mRecyclerView = findViewById(R.id.recyclerView);

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
                final String userDisplayName = cursor.getString(displayNameIndex);

                // Get contact phone number.
                final int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                final String phoneNumber = cursor.getString(phoneNumberIndex).replace(" ", "");

                if (checkValidity(exampleList, phoneNumber, userDisplayName)){
                    ExampleItem denemeItem = new ExampleItem(userDisplayName, phoneNumber); // It might cause problems.
                    exampleList.add(denemeItem);
                    Collections.sort(exampleList, new Comparator<ExampleItem>() {
                        @Override
                        public int compare(ExampleItem o1, ExampleItem o2) {
                            return o1.getText1().toLowerCase().compareTo(o2.getText1().toLowerCase());
                        }
                    });
                }

                mRecyclerView.setHasFixedSize(true);
                mLayoutManager = new LinearLayoutManager(this);
                mAdapter = new ExampleAdapyer(exampleList);

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);

                mAdapter.setOnItemClickListener(new ExampleAdapyer.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Intent resultIntent = new Intent();
                        ArrayList<ExampleItem> wow = new ArrayList<>();

                        wow = mAdapter.sendExampleList();
                        resultIntent.putExtra("no",  wow.get(position).getText2());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                });

            }while(cursor.moveToNext());
        }
    }

    public  boolean checkValidity(ArrayList<ExampleItem> list, String phoneNum, String displayName) {
       for (ExampleItem element : list) {
           if (element.getText1().equals(displayName) && element.getText2().equals(phoneNum)){
               return false;
           }
       }
       return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_contact);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}