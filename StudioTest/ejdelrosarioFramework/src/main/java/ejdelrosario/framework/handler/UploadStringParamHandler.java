/**
 * Created by EJ Del Rosario
 * Copyright (c) 2015
 * Personal Intellectual Property
 * All Rights Reserved
 */
package ejdelrosario.framework.handler;

public class UploadStringParamHandler {
	
	private String key, value;
	
	public UploadStringParamHandler(String key, String value){
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
