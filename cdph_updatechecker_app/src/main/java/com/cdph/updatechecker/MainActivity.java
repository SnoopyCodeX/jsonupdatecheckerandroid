package com.cdph.updatechecker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.cdph.app.UpdateChecker;

public class MainActivity extends Activity 
{
	private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		tv = findViewById(R.id.mainTextView);

		UpdateChecker.getInstance(this)
			.setUpdateLogsUrl("https://pastebin.com/raw/x9JufEML")
			.shouldAutoRun(true)
			.shouldAutoInstall(true)
			.setJsonModel(Model.class)
			.setOnUpdateDetectedListener(new UpdateChecker.OnUpdateDetectedListener() {
				@Override
				public void onUpdateDetected(Object info)
				{
					try {
						Model model = (Model) info;
						String str_curVer = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
						String str_newVer = model.version;

						if(UpdateChecker.compareVersion(str_curVer, str_newVer))
						{
							String txt = String.format("Name: %s\nVersion: %s\nDownload: %s\nDescription: %s",
													   model.name,
													   model.version,
													   model.downloadUrl,
													   model.description.get(0)
													   );

							AlertDialog dlg = new AlertDialog.Builder(MainActivity.this).create();
							dlg.setCancelable(true);
							dlg.setCanceledOnTouchOutside(false);
							dlg.setMessage(txt);
							dlg.setTitle("Update Available");
							dlg.show();
						}
						else
							Toast.makeText(MainActivity.this, "You have the latest version!", Toast.LENGTH_LONG).show(); 
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			})
			.runUpdateChecker();
    }

	public static final class Model 
	{
		@SerializedName("description")
		List<String> description;

		@SerializedName("version")
		String version;

		@SerializedName("name")
		String name;

		@SerializedName("downloadUrl")
		String downloadUrl;
	}
}
