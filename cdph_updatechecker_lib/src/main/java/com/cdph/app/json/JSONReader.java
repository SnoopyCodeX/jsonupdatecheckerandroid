package com.cdph.app.json;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cdph.app.UpdateChecker.NewUpdateInfo;
import com.cdph.app.util.Config;

public class JSONReader
{
	public NewUpdateInfo readJson(String json) throws Exception
	{
		//Parse as jsonObject then get the values
		JSONObject job = new JSONObject(json);
		int versionCode = job.getInt(Config.KEY_VERSION_CODE);
		String versionName = job.getString(Config.KEY_VERSION_NAME);
		String downloadUrl = job.getString(Config.KEY_DOWNLOAD_URL);
		String description = "";

		//Parse 'description' as jsonArray then get the values
		JSONArray jar = job.getJSONArray(Config.KEY_DESCRIPTION);
		for(int i = 0; i < jar.length(); i++)
			description += jar.getString(i) + "\n";	
		description = description.substring(0, description.length()-1);
		
		return (new NewUpdateInfo(downloadUrl, versionName, description, versionCode));
	}
}
