/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */
package ejdelrosario.framework.handler;

public class UploadFileParamHandler {
	
	private String key, filePath, fileName;
	
	public UploadFileParamHandler(String key, String filePath, String fileName){
		this.key = key;
		this.filePath = filePath;
		this.fileName = fileName;
	}
	
	public UploadFileParamHandler(String key, String filePath){
		this.key = key;
		this.filePath = filePath;
	}
	
	public String getKey() {
		return key;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public String getFileName(){
		return fileName;
	}

}
