package com.example.nathanhays.deletesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by nathanhays on 8/18/14.
 */
public class WidgetBroadCast extends BroadcastReceiver {

    private static int clickCount = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("pl.looksok.intent.action.CHANGE_PICTURE")){
            updateWidgetPictureAndButtonListener(context);
        }
    }

    private void updateWidgetPictureAndButtonListener(Context context) {

        Toast toast = Toast.makeText(context," texts have been deleted", Toast.LENGTH_LONG);
        toast.show();
    }

}