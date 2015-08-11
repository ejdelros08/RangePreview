/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */

package ejdelrosario.framework.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class DialogUtil {
	
	public static void showAlertDialog(Context context, String message){
		AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
		msgbox.setMessage(message);
		msgbox.setPositiveButton("Ok", null);
		msgbox.setCancelable(false);
		msgbox.show();
	}
	
	public static void showAlertDialog(Context context, String message, DialogInterface.OnDismissListener listener){
		AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
		msgbox.setMessage(message);
		msgbox.setPositiveButton("Ok", null);
		msgbox.setCancelable(false);
		
		AlertDialog dialog = msgbox.create();
		dialog.setOnDismissListener(listener);
		dialog.show();
	}
	
	public static void showConfirmationDialog(Context context, String message, String buttonPositive, String buttonNegative, DialogInterface.OnClickListener listener){
		AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
		msgbox.setMessage(message);
		msgbox.setPositiveButton(buttonPositive, listener);
		msgbox.setNegativeButton(buttonNegative, listener);
		msgbox.setCancelable(false);
		msgbox.show();
	}
	
	public static void showToast(Context context, String message, int length){
		Toast.makeText(context, message, length).show();
	}

}
