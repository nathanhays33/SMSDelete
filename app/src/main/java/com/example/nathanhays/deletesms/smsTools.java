package com.example.nathanhays.deletesms;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by nathanhays on 8/18/14.
 */
public class  smsTools {

    static int count = 0;
    SharedPreferences sharedPrefs;

 public void smsTools(){

 }
    public boolean deleteSMS(String id, String type, Context context){
        context.getContentResolver().delete(
                Uri.parse(type + id), null, null);
        Log.d("SMS", "Deleted");
        return true;
    }

    private String getANumber(int id, Context context) {
        String addx = "";
        String type = "";
        final String[] projection = new String[] {"address","contact_id","charset","type"};
        final String selection = "type=137 or type=151"; // PduHeaders
        Uri.Builder builder = Uri.parse("content://mms").buildUpon();
        builder.appendPath(String.valueOf(id)).appendPath("addr");

        Cursor cursor = context.getContentResolver().query(
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

    public String getMMSNumber(int id, Context context){
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = context.getContentResolver().query(uriAddress, null,
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

    public int deleteList(String type, boolean all, Context context) {

        //set up preference
        try {
            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
        } catch (NullPointerException e) {
            sharedPrefs = null;
        }
         ArrayList<String> contacts = new ArrayList<String>();
        Set<String> temp = sharedPrefs.getStringSet("contacts", null);
        if(temp != null) {
            contacts = new ArrayList<String>(temp);
        }

        List<Sms> lstSms = new ArrayList<Sms>();
        Sms objSms = new Sms();
        Uri message = Uri.parse(type);
        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
       // this.startManagingCursor(c);
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
                        address = getANumber(Integer.parseInt(objSms.getId()), context);
                    }

                    if (type.equalsIgnoreCase("content://sms/")) {
                        objSms.setAddress(c.getString(c.getColumnIndexOrThrow("address")));
                        address = objSms.getAddress();
                    }

                    if (address != null) {
                        name = getContactNameFromNumber(address, context);
                    }

                /* Check to see if it equals name */
                    if (name != null) {
                        if (contacts.contains(name)) {
                            deleteSMS(objSms.getId(), type, context);
                            count++;
                        }
                    }
                }
                else{
                    count++;
                    deleteSMS(objSms.getId(), type, context);
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
        return count;
    }

    private String getContactNameFromNumber(String number, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name ="not found";
        Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},null,null,null);
        if (cursor.moveToFirst())
        {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        // if(cursor != null)cursor.close();
        return name;
    }

}
