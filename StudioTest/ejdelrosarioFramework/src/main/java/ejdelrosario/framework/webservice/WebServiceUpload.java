/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */
package ejdelrosario.framework.webservice;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import ejdelrosario.framework.handler.UploadFileParamHandler;
import ejdelrosario.framework.handler.UploadStringParamHandler;
import ejdelrosario.framework.interfaces.WebServiceInterface.onExceptionListener;
import ejdelrosario.framework.interfaces.WebServiceInterface.onNetworkExceptionListener;
import ejdelrosario.framework.interfaces.WebServiceInterface.onResponseListener;
import ejdelrosario.framework.webservice.MultiPartEntity.ProgressListener;

public class WebServiceUpload extends AsyncTask<Void, Integer, Void>{
	
	private String URL = null;
	
	private ArrayList<UploadStringParamHandler> stringParam = new ArrayList<UploadStringParamHandler>();
	private ArrayList<UploadFileParamHandler> fileParam = new ArrayList<UploadFileParamHandler>();
	
	private Context context;
	
	private ProgressDialog dlg;
	
	private HttpClient httpClient;
	
	private String responseString;
	
	private onResponseListener mResponseListener;
	
	private onNetworkExceptionListener mNetworkListener;
	
	private onExceptionListener mExceptionListener;
	
	private Exception catcher;
	private String exceptionMessage = "";
	
	private long totalSize = 0;
	
	private String dialogMessage = "";
	
	public WebServiceUpload(Context context){
		this.context = context;
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		if(getProgressDialog() != null){
			getProgressDialog().show();
		}
		super.onPreExecute();
		
	}
	
	
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		if(WebServiceHelper.isNetworkAvailable(context)){
			
			httpClient = new DefaultHttpClient();
			
			HttpPost post = new HttpPost(getURL());
			MultiPartEntity mEntity = new MultiPartEntity(new ProgressListener() {
				
				@Override
				public void transferred(long num) {
					// TODO Auto-generated method stub
					publishProgress((int) ((num / (float) totalSize) * 100));
				}
			});
			
			for(UploadFileParamHandler files : fileParam){
				mEntity.addPart(files.getKey(), new FileBody(new File(files.getFilePath())));
			}
			
			for(UploadStringParamHandler strings : stringParam){
				try {
					mEntity.addPart(strings.getKey(), new StringBody(strings.getValue()));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					exceptionMessage = "Invalid string parameter encoding";
					catcher = e;
					e.printStackTrace();
				}
			}
			
			totalSize = mEntity.getContentLength();
			
			post.setEntity(mEntity);
			
			try {
				
				HttpResponse resp = httpClient.execute(post);
				HttpEntity entity = resp.getEntity();
				responseString = EntityUtils.toString(entity);
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				catcher = e;
				exceptionMessage = "Connetion Error";
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				exceptionMessage = "Connetion Error";
				catcher = e;
				e.printStackTrace();
			} catch (UnknownHostException e){
				exceptionMessage = "Connetion Error";
				catcher = e;
				e.printStackTrace();
			} catch(ConnectTimeoutException e){
				exceptionMessage = "Connetion Timeout";
				catcher = e;
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				exceptionMessage = "Connetion Error";
				catcher = e;
				e.printStackTrace();
			}
			
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(final Integer... values) {
		// TODO Auto-generated method stub
		if(getProgressDialog() != null){
			new Handler().post(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					getProgressDialog().setMessage(dialogMessage + "" + values[0] + "%");
				}
			});
		}
	}
	
	
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		if(getProgressDialog() != null){
			if(getProgressDialog().isShowing()){
				getProgressDialog().dismiss();
			}
		}
		if(responseString != null){
			mResponseListener.onResponse(responseString);
		}
		else{
			if(!WebServiceHelper.isNetworkAvailable(context)){
				mNetworkListener.onNetworkException();
			}
			else if(catcher != null){
				mExceptionListener.onException(catcher, exceptionMessage);
			}
		}
		super.onPostExecute(result);
	}
	
	
	public void addStringParam(UploadStringParamHandler handler){
		stringParam.add(handler);
	}
	
	public ArrayList<UploadStringParamHandler> getStringParam(){
		return stringParam;
	}
	
	public void addFileParam(UploadFileParamHandler handler){
		fileParam.add(handler);
	}
	
	public ArrayList<UploadFileParamHandler> getFileParam(){
		return fileParam;
	}
	
	public void setProgressDialog(String msg){
		dlg = new ProgressDialog(context);
		dlg.setMessage(msg);
		dlg.setCancelable(false);
		dialogMessage = msg;
	}
	
	private ProgressDialog getProgressDialog(){
		return dlg;
	}
	
	public void setURL(String url){
		URL = url;
	}
	
	public String getURL(){
		return URL;
	}
	
	public void setOnResponseListener(onResponseListener listener){
		mResponseListener = listener;
	}
	
	public void setOnNetworkExceptionListener(onNetworkExceptionListener listener){
		mNetworkListener = listener;
	}
	
	public void setOnExceptionListener(onExceptionListener listener){
		mExceptionListener = listener;
	}

}
