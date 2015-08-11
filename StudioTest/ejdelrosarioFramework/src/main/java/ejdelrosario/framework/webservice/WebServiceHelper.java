/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */

package ejdelrosario.framework.webservice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WebServiceHelper {
	
	public static final int TIMEOUT 			= 15000;
	public static final String LINE_END 		= "\r\n";
	public static final String TWO_HYPENS		= "--";
	public static final String BOUNDARY			= "*****";

	
	public static boolean isNetworkAvailable(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
