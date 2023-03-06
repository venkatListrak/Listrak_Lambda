package com.config;

import java.io.File;
import java.io.FileReader;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

public class ApplicationConfig {
	
	public static JSONObject getEnvJsonObject(String fileName, String stage) {
	    JSONObject jsonObject = null;
	    JSONParser parser = new JSONParser();
	    File envConfig = new File(fileName);
	    try {
	      Object obj = parser.parse(new FileReader(envConfig));
	      jsonObject = new JSONObject(obj.toString());
	      jsonObject = jsonObject.getJSONObject(stage);
	    } catch (Exception e) {
	      e.printStackTrace();
	    } 
	    return jsonObject;
	  }

}
