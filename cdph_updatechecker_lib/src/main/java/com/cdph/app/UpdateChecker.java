/*
* Checks for updates through json
*
*@author    SnoopyCodeX
*@copyright 2020
*@email     extremeclasherph@gmail.com
*@package   com.cdph.app
*/

package com.cdph.app;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class UpdateChecker
{
	private static OnUpdateDetectedListener listener;
	private static String updateLogUrl = "";
	private static boolean autoRun = false, 
						   autoInstall = false, 
						   updateOnWifiOnly = true;
	
	private static Class<?> jsonModel;
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
	*@param	  autoRun             - Default is false
	*@return  UpdateChecker.class
	*/
	public UpdateChecker shouldAutoRun(boolean autoRun)
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
	*@param   updateLogsUrl       - The url of the json-encoded info of the new update
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
	*@param   autoInstall         - Default is false
	*@return  UpdateChecker.class
	*/
	public UpdateChecker shouldAutoInstall(boolean autoInstall)
	{
		this.autoInstall = autoInstall;
		return this;
	}
	
	/*
	* When set to false, this will also allow
	* updating using mobile data.
	*
	*@param  wifiOnly  - If will only check for updates if connected on wifi network
	*@return UpdateChecker.class
	*/
	public UpdateChecker shouldCheckUpdateOnWifiOnly(boolean wifiOnly)
	{
		this.updateOnWifiOnly = wifiOnly;
		return this;
	}
	
	/*
	* Sets an OnUpdateDetectedListener, when a new update
	* is detected, this listener will be triggered.
	*
	*@param   listener            - The listener to be triggered
	*@return  UpdateChecker.class
	*/
	public UpdateChecker setOnUpdateDetectedListener(UpdateChecker.OnUpdateDetectedListener listener)
	{
		this.listener = listener;
		return this;
	}
	
	/*
	* Sets a custom json model to suit your needs
	*
	*@param  jsonModel           - A model class that will contain the update info
	*@return UpdateChecker.class
	*/
	public UpdateChecker setJsonModel(Class<?> jsonModel)
	{
		this.jsonModel = jsonModel;
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
				Toast.makeText(ctx, "You are not connected to an active network", Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(ctx, String.format("[ERROR (runUpdateChecker)]: %s", e.getMessage()), Toast.LENGTH_LONG).show();
		}
	}
	
	/*
	* Installs the application
	*
	*@param	  filePath  - The path of the apk to be installed
	*@return  null
	*/
	public static void installApp(String path)
	{
		try {
			Uri uri = Uri.parse("file://" + path);
			
			Intent promptInstall = new Intent(Intent.ACTION_VIEW);
			promptInstall.setDataAndType(uri, "application/vnd.android.package-archive");
			promptInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ctx.startActivity(promptInstall);
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(ctx, String.format("[ERROR (installApp)]: %s", e.getMessage()), Toast.LENGTH_LONG).show();
		}
	}
	
	/*
	* Downloads the file from the url
	*
	*@param  url         - The download url
	*@param  filename    - The filename
	*@return file        - The downloaded file
	*/
	public static File downloadUpdate(String url, String filename)
	{
		File file = null;
		
		if(!ConnectivityReceiver.isConnected(ctx))
			return file;
		
		try {
			TaskDownloadUpdate down = new TaskDownloadUpdate();
			file = down.execute(url, filename).get();
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(ctx, String.format("[ERROR (downloadUpdate)]: %s", e.getMessage()), Toast.LENGTH_LONG).show();
		}
		
		return file;
	}
	
	/*
	* Checks and formats version into a 
	* proper semantic versioning format
	*
	*@param  version  -  The string version (xx.xx.xx | [major].[minor].[patch])
	*/
	public static HashMap<String, Integer> toSemanticVersioning(String version)
	{
		HashMap<String, Integer> semanticVer = new HashMap<>();
		version = version.replaceAll("[a-zA-Z\\-]+", "");
		
		if(version.matches("\\d+\\.\\d+\\.\\d+"))
		{
			String[] vers = version.split("[.]");
			semanticVer.put("major", Integer.parseInt(vers[0]));
			semanticVer.put("minor", Integer.parseInt(vers[1]));
			semanticVer.put("patch", Integer.parseInt(vers[2]));
		}
		
		if(version.matches("\\d+\\.\\d+"))
		{
			String[] vers = version.split("[.]");
			semanticVer.put("major", Integer.parseInt(vers[0]));
			semanticVer.put("minor", Integer.parseInt(vers[1]));
			semanticVer.put("patch", 0);
		}
		
		if(version.matches("\\d+"))
		{
			semanticVer.put("major", Integer.parseInt(version));
			semanticVer.put("minor", 0);
			semanticVer.put("patch", 0);
		}
		
		return semanticVer;
	}
	
	/*
	* Compares version1 to version2,
	* returns true if version1 is greater than
	* version2.
	*
	*@param  version1  -  String version1
	*@param  version2  -  String version2
	*@return  boolean
	*/
	public static boolean compareVersion(String version1, String version2)
	{
		HashMap<String, Integer> curVer = UpdateChecker.toSemanticVersioning(version1);
		HashMap<String, Integer> newVer = UpdateChecker.toSemanticVersioning(version2);

		int cmaj = curVer.get("major"),
			cmin = curVer.get("minor"),
			cpat = curVer.get("patch");

		int nmaj = newVer.get("major"),
			nmin = newVer.get("minor"),
			npat = newVer.get("patch");
		
		return ((cmaj < nmaj) || (cmin < nmin) || (cpat < npat));
	}
	
	public static interface OnUpdateDetectedListener
	{
		public void onUpdateDetected(Object info)
	}
	
	private static class TaskUpdateChecker extends AsyncTask<String, Void, String>
	{
		private static final int CONNECT_TIMEOUT = 6000;
		private static final int READ_TIMEOUT = 3000;
		
		private Gson gson;
		private ProgressDialog dlg;
		private String errMsg = "";
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			
			gson = new GsonBuilder().create();
			dlg = new ProgressDialog(ctx);
			dlg.setCancelable(false);
			dlg.setCanceledOnTouchOutside(false);
			dlg.setProgressDrawable(ctx.getResources().getDrawable(android.R.drawable.progress_horizontal));
			dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dlg.setMessage("Checking for new update...");
			dlg.show();
		}
		
		@Override
		protected String doInBackground(String... params)
		{
			try {
				String str_url = params[0];
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
					
					return json;
				}
				
				conn.disconnect();
			} catch(Exception e) {
				e.printStackTrace();
				errMsg += e.getMessage();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			
			try {
				if(dlg != null)
					dlg.dismiss();
				
				if(listener != null && errMsg.isEmpty())
					listener.onUpdateDetected(gson.fromJson(result, jsonModel));
				else
					Toast.makeText(ctx, String.format("[ERROR]: %s", errMsg), Toast.LENGTH_LONG).show();
			
			} catch(Exception e) {
				e.printStackTrace();
				Toast.makeText(ctx, String.format("[ERROR (task_updatechecker)]: %s", e.getMessage()), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private static final class TaskDownloadUpdate extends AsyncTask<String, Void, File>
	{
		private ProgressDialog dlg;
		private String errMsg;
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			
			dlg = new ProgressDialog(ctx);
			dlg.setCancelable(false);
			dlg.setCanceledOnTouchOutside(false);
			dlg.setIndeterminate(false);
			dlg.setProgressPercentFormat(NumberFormat.getPercentInstance());
			dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dlg.setMessage("Downloading update...");
			dlg.setMax(100);
			dlg.show();
		}
		
		@Override
		protected File doInBackground(String[] params)
		{
			File file = null;
			
			try {
				String str_url = params[0];
				String str_tag = params[1];
				
				File dest = new File(Environment.DIRECTORY_DOWNLOADS, str_tag);
				if(dest.exists())
					dest.delete();
				
				DownloadManager.Request request = new DownloadManager.Request(Uri.parse(str_url));
				request.setTitle("Downloading update");
				request.setDescription("Please wait...");
				request.setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, str_tag);
				
				DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
				boolean downloading = true;
				long id = dm.enqueue(request);
				
				while(downloading)
				{
					DownloadManager.Query query = new DownloadManager.Query();
					query.setFilterById(id);
					
					Cursor cursor = dm.query(query);
					cursor.moveToFirst();
					
					int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
					int curr = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
					int maxx = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
					int prog = (curr > 0) ? (maxx / curr) : 0;
					dlg.setProgress(prog);
					
					if(prog < 100)
						dlg.setMessage(String.format("Downloading... (%s)", prog + "%"));
					else
						dlg.setMessage("Download finished...");
					
					if(status == DownloadManager.STATUS_SUCCESSFUL)
					{
						String filePath = cursor.getString(cursor.getColumnIndex("local_uri"));
						file = new File(filePath);
						
						if(autoInstall && errMsg == null)
							installApp(filePath);
						
						downloading = false;
					}
					
					cursor.close();
				}
			} catch(Exception e) {
				e.printStackTrace();
				errMsg = e.getMessage();
			}
			
			return file;
		}

		@Override
		protected void onPostExecute(File result)
		{
			super.onPostExecute(result);
			
			if(dlg != null)
				dlg.dismiss();
				
			if(errMsg != null)
				Toast.makeText(ctx, String.format("[ERROR (task_downloadUpdate)]: %s", errMsg), Toast.LENGTH_LONG).show();
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
			
			if(updateOnWifiOnly)
				if(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().getType() != ConnectivityManager.TYPE_WIFI)
					return false;
				else
					return (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI && cm.getActiveNetworkInfo().isConnected());
			else
				return (cm.getActiveNetworkInfo() != null && (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE || cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) && cm.getActiveNetworkInfo().isConnected());
		}
	}
}
