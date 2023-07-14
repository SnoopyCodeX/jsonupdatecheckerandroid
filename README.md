# JSON-based Update Checker
[![Developed by SnoopyCodeX](https://img.shields.io/badge/Developed%20by-SnoopyCodeX-blue.svg?longCache=true&style=for-the-badge)](https://facebook.com/SnoopyCodeX)
[![Github Release](https://img.shields.io/github/release/SnoopyCodeX/jsonupdatecheckerandroid.svg?style=for-the-badge)](https://github.com/SnoopyCodeX/jsonupdatecheckerandroid/releases) 
[![Github Star](https://img.shields.io/github/stars/SnoopyCodeX/jsonupdatecheckerandroid.svg?style=for-the-badge)](https://github.com/SnoopyCodeX/jsonupdatecheckerandroid) 
[![Github Fork](https://img.shields.io/github/forks/SnoopyCodeX/jsonupdatecheckerandroid.svg?style=for-the-badge)](https://github.com/SnoopyCodeX/jsonupdatecheckerandroid) 
![License](https://img.shields.io/github/license/SnoopyCodeX/jsonupdatecheckerandroid.svg?style=for-the-badge)
[![DepShield Badge](https://depshield.sonatype.org/badges/SnoopyCodeX/jsonupdatecheckerandroid/depshield.svg)](https://depshield.github.io)
[![Build Status](https://travis-ci.org/SnoopyCodeX/jsonupdatecheckerandroid.svg?branch=master)](https://travis-ci.org/SnoopyCodeX/jsonupdatecheckerandroid)

- [x] Customizeable
- [x] Easy to implement
- [x] JSON-based checker
- [x] Can download app
- [x] Can auto install app
- [x] Automatically check for updates
- [x] Lightweight library
- [x] Uses Google's [Gson Library](https://github.com/google/gson) for easier json parsing

# Setup
### Clone this repo
- Clone this repo to your device
```
git clone https://github.com/SnoopyCodeX/jsonupdatecheckerandroid
```
- After cloning, copy the folder named ```cdph_updatechecker_lib``` to your project's directory then include it in your app level build.gradle
```groovy
compile project(':cdph_updatechecker_lib')
```
### JAR File (Unstable)
- Add this jar file to your app's libs directory
- (Unstable) [UpdateChecker - v22.0.0](https://raw.githubusercontent.com/SnoopyCodeX/jsonupdatecheckerandroid/master/Jar/v22.0.0-UpdateChecker.jar)
---
# Usage
### Initialize UpdateChecker
```java
UpdateChecker checker = UpdateChecker.getInstance(context);
```
---
### Set custom json model (example)
```java
checker.setJsonModel(MyModel.class);

public class MyModel
{
	@SerializedName("description")
	List<String> description;
	
	@SerializedName("name")
	String name;
	
	@SerializedName("version")
	String version;
	
	@SerializedName("downloadUrl")
	String downloadUrl;
}
```
---
### JSON File to read to (example)
```json
{
  "name": "Test App",
  "version": "1.2.0",
  "description": ["My description", "Test description"],
  "downloadUrl": "https://testurl.com/download"
}
```
---
### Enable auto update
```java
checker.shouldAutoRun(true);
```
---
### Enable update on both mobile networks
```java
checker.shouldCheckUpdateOnWifiOnly(false);
```
---
### Enable auto installation
```java
checker.shouldAutoInstall(true);
```
---
### Set the url of the json file
```java
checker.setUpdateLogsUrl("https://urlhere");
```
---
### Downloading the app
```java
//Returns the filepath of the downloaded app
UpdateChecker.downloadUpdate("https://urlHere", "filename.apk");
```
---
### Installing the app manually
```java
UpdateChecker.installApp(file);
```
----
### Add listener
```java
checker.setOnUpdateDetectedListener(new UpdateChecker.OnUpdateDetectedListener() {
    @Override
    public void onUpdateDetected(Object info)
    {}
});
```

### Demo Usage
```java
UpdateChecker.getInstance(this)
	.setUpdateLogsUrl("https://pastebin.com/raw/x9JufEML")
	.shouldAutoRun(true)
	.shouldAutoInstall(false)
	.setJsonModel(Model.class)
	.setOnUpdateDetectedListener(new UpdateChecker.OnUpdateDetectedListener() {
		@Override
		public void onUpdateDetected(Object info)
		{
			try {
				Model model = (Model) info;
				String str_curVer = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
				String str_newVer = model.version;

				// Check if the current version is lower than the new update version
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
	});
```
