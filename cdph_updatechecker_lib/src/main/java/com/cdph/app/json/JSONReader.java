package com.cdph.app.json;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cdph.app.UpdateChecker.NewUpdateInfo;

public abstract class JSONReader
{
	public abstract NewUpdateInfo readJson(String json) throws Exception
}
