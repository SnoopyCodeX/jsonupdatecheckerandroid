/*
* Checks for updates through json
*
*@author    SnoopyCodeX
*@copyright 2020
*@email     extremeclasherph@gmail.com
*@package   com.cdph.app
*/

package com.cdph.app;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public final class UpdateChecker
{
	private static OnUpdateDetectedListener listener;
	private static String updateLogUrl = "";
	private static boolean autoRun = false, autoInstall = false;
	private static Context ctx;
	
	/*
	* For singleton purposes
	*
	*@param        context
	*@constructor  UpdateChecker
	*/
	private UpdateChecker(Context context)
	{
		this.ctx = context;
	}
	
	/*
	* Used to get the instance of this class
	*
	*@param  context
	*@return UpdateChecker.class
	*/
	public static final UpdateChecker getInstance(Context ctx)
	{
		return (new UpdateChecker(ctx));
	}
	
	/*
	* When set to true, this will auto check for
	* new updates when connected to the internet.
	* Default is false.
	*
	*@param	  autoRun
	*@return  UpdateChecker.class
	*/
	public UpdateChecker shouldRunWhenConnected(boolean autoRun)
	{
		this.autoRun = autoRun;
		
		if(autoRun)
			this.ctx.registerReceiver(new ConnectivityReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			
		return this;
	}
	
	/*
	* The url where the new info of the app
	* will be read to.
	*
	*@param   updateLogsUrl
	*@return  UpdateChecker.class
	*/
	public UpdateChecker setUpdateLogsUrl(String updateLogsUrl)
	{
		this.updateLogUrl = updateLogsUrl;
		return this;
	}
	
	/*
	* When set to true, this will automatically
	* install the new app after it has been
	* downloaded.
	*
	*@param   autoInstall
	*@return  UpdateChecker.class
	*/
	public UpdateChecker shouldAutoInstall(boolean autoInstall)
	{
		this.autoInstall = autoInstall;
		return this;
	}
	
	/*
	* Sets an OnUpdateDetectedListener, when a new update
	* is detected, this listener will be triggered.
	*
	*@param   listener
	*@return  UpdateChecker.class
	*/
	public UpdateChecker setOnUpdateDetectedListener(UpdateChecker.OnUpdateDetectedListener listener)
	{
		this.listener = listener;
		return this;
	}
	
	/*
	* Runs the update checker
	*
	*@return null
	*/
	public void runUpdateChecker()
	{
		try {
			if(ConnectivityReceiver.isConnected(ctx))
				(new TaskUpdateChecker()).execute(updateLogUrl);
			else
				Toast.makeText(ctx, String.format("[ERROR]: %s", "You are not connected to a wifi network"), Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(ctx, String.format("[ERROR]: %s", e.getMessage()), Toast.LENGTH_LONG).show();
		}
	}
	
	/*
	* Installs the application
	*
	*@param	  filePath
	*@return  null
	*/
	public void installApp(String path)
	{
		try {
			File file = new File(path);
			Uri uri = Uri.fromFile(file);
			
			Intent promptInstall = new Intent(Intent.ACTION_VIEW);
			promptInstall.setDataAndType(uri, "application/vnd.android.package-archive");
			promptInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startActivity(promptInstall);
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(ctx, String.format("[ERROR]: %s", e.getMessage()), Toast.LENGTH_LONG).show();
		}
	}
	
	public static interface OnUpdateDetectedListener
	{
		public void onUpdateDetected(NewUpdateInfo info, boolean autoInstall)
	}
	
	public static class NewUpdateInfo
	{
		public int app_version;
		public String app_updateUrl;
		public String app_versionName;
		public String app_description;
		
		public NewUpdateInfo(String url, String versionName, String description, int version)
		{
			this.app_version = version;
			this.app_versionName = versionName;
			this.app_updateUrl = url;
			this.app_description = description;
		}
	}
	
	private static class TaskUpdateChecker extends AsyncTask<String, Void, NewUpdateInfo>
	{
		private static final int CONNECT_TIMEOUT = 6000;
		private static final int READ_TIMEOUT = 3000;
		
		private ProgressDialog dlg;
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			
			dlg = new ProgressDialog(ctx);
			dlg.setCancelable(false);
			dlg.setCanceledOnTouchOutside(false);
			dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dlg.setMessage("Checking for new update...");
			dlg.show();
		}
		
		@Override
		protected NewUpdateInfo doInBackground(String... params)
		{
			NewUpdateInfo info = null;
			
			try {
				String str_url = sanitizeUrl(params[0]);
				URL url = new URL(str_url);
				
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(CONNECT_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);
				conn.setDoInput(true);
				conn.connect();
				
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK)
				{
					InputStream is = conn.getInputStream();
					String json = new String();
					byte[] buffer = new byte[1024];
					int len = 0;
					
					//Read the json text from the website
					while((len = is.read(buffer)) != -1)
						json += new String(buffer, 0, len);
					is.close();
					
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
					
					info = new NewUpdateInfo(downloadUrl, versionName, description, versionCode);
				}
				
				conn.disconnect();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			return info;
		}
		
		@Override
		protected void onPostExecute(NewUpdateInfo result)
		{
			super.onPostExecute(result);
			
			try {
				if(dlg != null)
					dlg.dismiss();
				
				if(listener != null)
					if(ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode < result.app_version)
						listener.onUpdateDetected(result, autoInstall);
			} catch(Exception e) {
				e.printStackTrace();
				Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
		private String sanitizeUrl(String url)
		{
			String sanitized = url;
			
			if(url.contains("//"))
			{
				String[] params = url.split("//");
				if(!params[0].equals("http:") || !params[0].equals("https:"))
					sanitized = "https://" + url;
			}
			else
				sanitized = "https://" + url;
			
			return sanitized;
		}
	}
	
	private static final class ConnectivityReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context ctx, Intent data)
		{
			if(isConnected(ctx))
				(new TaskUpdateChecker()).execute(updateLogUrl);
		}
		
		public static final boolean isConnected(Context ctx)
		{
			ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI && cm.getActiveNetworkInfo().isConnected());
		}
	}
	
	private class Config
	{
		public static final String KEY_VERSION_CODE = "versionCode";
		public static final String KEY_VERSION_NAME = "versionName";
		public static final String KEY_DOWNLOAD_URL = "url";
		public static final String KEY_DESCRIPTION = "description";
	}
}
