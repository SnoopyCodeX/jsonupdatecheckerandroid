package com.cdph.updatechecker;

import android.app.*;
import android.os.*;

import com.cdph.app.UpdateChecker;

public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		UpdateChecker.getInstance(this)
			.setUpdateLogsUrl("https://pastebin.com/raw/SFpLs0De")
			.shouldRunWhenConnected(true)
			.shouldAutoInstall(true)
			.setOnUpdateDetectedListener(new UpdateChecker.OnUpdateDetectedListener() {
				@Override
				public void onUpdateDetected(UpdateChecker.NewUpdateInfo info, boolean autoInstall)
				{
					AlertDialog dlg = new AlertDialog.Builder(MainActivity.this).create();
					
					String msg = "";
					msg += info.app_version + "\n";
					msg += info.app_versionName + "\n";
					msg += info.app_updateUrl + "\n";
					msg += info.app_description;
					
					dlg.setTitle("New Update Detected");
					dlg.setMessage(msg);
					dlg.show();
				}
			});
    }
}
