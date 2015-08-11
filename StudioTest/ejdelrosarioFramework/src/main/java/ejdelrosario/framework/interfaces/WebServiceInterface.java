/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */

package ejdelrosario.framework.interfaces;

public class WebServiceInterface {
	
	public interface onNetworkExceptionListener{
		void onNetworkException();
	}
	
	public interface onResponseListener{
		void onResponse(String response);
	}
	
	public interface onExceptionListener{
		void onException(Exception e, String message);
	}

}
