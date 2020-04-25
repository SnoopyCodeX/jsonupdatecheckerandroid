package com.cdph.updatechecker;

import android.app.*;
import android.content.*;
import android.os.*;
import org.json.*;

import com.cdph.app.UpdateChecker;
import com.cdph.app.UpdateChecker.NewUpdateInfo;
import com.cdph.app.json.JSONReader;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		UpdateChecker.getInstance(this)
			.setUpdateLogsUrl("https://pastebin.com/raw/e3q1h4iQ")
			.shouldAutoRun(true)
			.shouldAutoInstall(true)
			.setJsonReader(new MyCustomJsonReader())
			.setOnUpdateDetectedListener(new UpdateChecker.OnUpdateDetectedListener() {
				@Override
				public void onUpdateDetected(final UpdateChecker.NewUpdateInfo info, boolean autoInstall)
				{
					final AlertDialog dlg = new AlertDialog.Builder(MainActivity.this).create();
					
					String msg = "";
					msg += info.app_version + "\n";
					msg += info.app_versionName + "\n";
					msg += info.app_updateUrl + "\n";
					msg += info.app_description;
					
					dlg.setTitle("New Update Detected");
					dlg.setMessage(msg);
					dlg.setButton(AlertDialog.BUTTON1, "Update now", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface di, int btn)
						{
							dlg.dismiss();
							UpdateChecker.downloadUpdate("https://github.com/SnoopyCodeX/jsonupdatecheckerandroid/raw/master/updatedapp/cdph_updatechecker_app.apk", "cdph_updatechecker_app.apk");
						}
					});
					dlg.show();
				}
			});
    }
	
	private class MyCustomJsonReader extends JSONReader
	{
		@Override
		public NewUpdateInfo readJson(String json) throws Exception
		{
			//Parse as jsonObject then get the values
			JSONObject job = new JSONObject(json);
			int versionCode = job.getInt("versionCode");
			String versionName = job.getString("versionName");
			String downloadUrl = job.getString("url");
			String description = "";

			//Parse 'description' as jsonArray then get the values
			JSONArray jar = job.getJSONArray("description");
			for(int i = 0; i < jar.length(); i++)
				description += jar.getString(i) + "\n";	
			description = description.substring(0, description.length()-1);

			return (new NewUpdateInfo(downloadUrl, versionName, description, versionCode));
		}
	}
}
