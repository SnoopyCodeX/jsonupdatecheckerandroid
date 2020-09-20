# JSON-based Update Checker
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
### JAR File
- Add this jar file to your app's libs directory
- [UpdateChecker - v22.0.0](https://raw.githubusercontent.com/SnoopyCodeX/jsonupdatecheckerandroid/master/Jar/v22.0.0-UpdateChecker.jar)
---
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
UpdateChecker.downloadUpdate("https://urlHere");
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
