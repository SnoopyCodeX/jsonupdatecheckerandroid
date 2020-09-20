# JSON-based Update Checker
![DepShield Badge](https://depshield.sonatype.org/badges/SnoopyCodeX/repository/depshield.svg)
---
- [x] Customizeable
- [x] Easy to implement
- [x] JSON-based checker
- [x] Can download app
- [x] Can auto install app
- [x] Automatically check for updates
- [x] Lightweight library
- [x] Uses Google's [Gson Library](https://github.com/google/gson) for json parsing
---
# Setup
---
### Initialize UpdateChecker
```java
UpdateChecker checker = UpdateChecker.getInstance(context);
```
---
### Set custom json reader
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
