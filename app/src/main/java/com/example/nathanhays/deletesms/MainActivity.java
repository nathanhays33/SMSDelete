package com.example.nathanhays.deletesms;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.os.Handler;
import android.widget.Toast;


public class MainActivity extends Activity {

    private CustomAdapter adapter;

    ListView lv;
    final static int PICK_CONTACT = 1;

    ArrayList<String> contacts;
    List<Sms> deleteSMS;

    SharedPreferences sharedPrefs;
    public static final String PREFS_NAME = "MyPrefsFile";
    SharedPreferences.Editor editor;

    ProgressDialog pd;
    volatile  Handler handler = new Handler();

    EditText pField;

    int count = 0;
    /*
    Add a contact to the sharedPref and to the list
     */
    public void setContact(String contact){
       addStringPref(contact);
       setList();
       updateList();
    }

    /*
    refreshes the contacts from sharedPref set to be inserted into the list view
     */
    public void setList(){
        Set<String> temp = sharedPrefs.getStringSet("contacts", null);
        if(temp != null) {
            contacts = new ArrayList<String>(temp);
        }
    }

    /*
    adds a string to the shared pref
     */
    public void addStringPref(String contact){
       // contacts.add(contact);
        Set<String> temp = new HashSet<String>(contacts);
        temp.add(contact);
        editor.putStringSet("contacts", temp);
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set up preference
        try {
            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
        } catch (NullPointerException e) {
            sharedPrefs = null;
        }

        editor = sharedPrefs.edit();

        //set up list
        contacts = new ArrayList<String>();
        setList();

        lv = (ListView) findViewById(R.id.listView);

        adapter = new CustomAdapter(this,
                R.id.listView,
                contacts);
        lv.setAdapter(adapter);

        pField = (EditText)findViewById(R.id.phoneNumber);
        pField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    hideKeyboard();
                }

            }
        });

    }

    public void updateList() {
        adapter.clear();
        adapter.addAll(contacts);
        adapter.notifyDataSetChanged();
    }

    /*
    protected void sendSMSMessage() {
        Log.i("Send SMS", "");

        String phoneNo = txtphoneNo.getText().toString();
        String message = txtMessage.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    public List<Sms> deleteList(String type, boolean all) {
        List<Sms> lstSms = new ArrayList<Sms>();
        Sms objSms = new Sms();
        Uri message = Uri.parse(type);
        ContentResolver cr = this.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        this.startManagingCursor(c);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                String address = null;
                String name = null;

                objSms = new Sms();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));

                String sent = c.getString(c.getColumnIndexOrThrow("type"));
                if(!all) {
                    if (type.equalsIgnoreCase("content://mms/")) {
                        address = getANumber(Integer.parseInt(objSms.getId()));
                    }

                    if (type.equalsIgnoreCase("content://sms/")) {
                        objSms.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
                        address = objSms.getAddress();
                    }

                    if (address != null) {
                        name = getContactNameFromNumber(address);
                    }

                /* Check to see if it equals name */
                    if (name != null) {
                        if (contacts.contains(name)) {
                            deleteSMS(objSms.getId(), type);
                            count++;
                        }
                    }
                }
                else{
                    count++;
                    deleteSMS(objSms.getId(), type);
                }
/*
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                } else {
                    objSms.setFolderName("sent");
                }
*/
                lstSms.add(objSms);
                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
    //    if(c != null)c.close();
        return lstSms;
    }

private String getANumber(int id) {
        String addx = "";
        String type = "";
        final String[] projection = new String[] {"address","contact_id","charset","type"};
        final String selection = "type=137 or type=151"; // PduHeaders
        Uri.Builder builder = Uri.parse("content://mms").buildUpon();
        builder.appendPath(String.valueOf(id)).appendPath("addr");

        Cursor cursor = this.getContentResolver().query(
                builder.build(),
                projection,
                selection,
                null, null);

        if (cursor.moveToFirst()) {
            do {
                addx = cursor.getString(cursor.getColumnIndex("address"));
                type = cursor.getString(cursor.getColumnIndex("type"));
            } while(cursor.moveToNext());
        }
        // Outbound messages address type=137 and the value will be 'insert-address-token'
        // Outbound messages address type=151 and the value will be the address
        // Additional checking can be done here to return the correct address.
      //  if(cursor != null)cursor.close();

        return addx;
    }

public String getMMSNumber(int id){
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = getContentResolver().query(uriAddress, null,
                selectionAdd, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
                String number = cAdd.getString(cAdd.getColumnIndex("address"));
                if (number != null) {
                    try {
                        Long.parseLong(number.replace("-", ""));
                        name = number;
                    } catch (NumberFormatException nfe) {
                        if (name == null) {
                            name = number;
                        }
                    }
                }
            } while (cAdd.moveToNext());
        }

        if (cAdd != null) {
         //   cAdd.close();
        }
        return name;
    }


   /****** Buttons *******/


    public void deleteTexts(){
        count= 0;
        pd = ProgressDialog.show(this, "Zuking List" , "");
        Thread t = new Thread() {
            public void run() {
               deleteList("content://sms/", false);
               deleteList("content://mms/", false);
               pd.dismiss();
               handler.post(Success);
            }
        };
        t.start();
    }

    public void deleteAll(){
        count= 0;
        pd = ProgressDialog.show(this, "Zuking All" , "");
        Thread t = new Thread() {
            public void run() {
                deleteList("content://sms/", false);
                deleteList("content://mms/", false);
                pd.dismiss();
                handler.post(Success);
            }
        };
        t.start();
    }


    public void addNumber(View v){
       String pNumber = ((EditText)findViewById(R.id.phoneNumber)).getText().toString();
       setContact(pNumber);
       ((EditText)findViewById(R.id.phoneNumber)).setText("");
    }

    public void getContacts(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    final Runnable Success = new Runnable() {
        public void run() {
            Toast toast = Toast.makeText(getApplicationContext(), Integer.toString(count) + " texts have been deleted", Toast.LENGTH_LONG);
            toast.show();
        }
    };

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (PICK_CONTACT) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        contacts.add(name);
                        setContact(name);
                        // TODO Whatever you want to do with the selected contact name.
                    }
                //    if(c != null) c.close();
                }
                updateList();

                break;
        }
    }

    public boolean deleteSMS(String id, String type){
        this.getContentResolver().delete(
                Uri.parse(type + id), null, null);
        Log.d("SMS", "Deleted");
        return true;
    }

    private String getContactNameFromNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name ="not found";
        Cursor cursor = this.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},null,null,null);
        if (cursor.moveToFirst())
        {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
       // if(cursor != null)cursor.close();
        return name;
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pField.getWindowToken(), 0);
    }
    public void onDestroy() {
        super.onDestroy();
        /*
        if (cursor != null) {
            c.close();
        }
        if (db != null) {
            db.close();
        }
        */
    }

    public void deleteTexts(View v){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Are you sure you want to Zuke the List");

        // Setting Dialog Message
        alertDialog.setMessage("You can not go back!!");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Zuke List", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                deleteTexts();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    public void deleteAll(View v){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("Are you sure you want to Zuke everything");

        // Setting Dialog Message
        alertDialog.setMessage("You can not go back!!");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Zuke List", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                deleteAll();
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }
}