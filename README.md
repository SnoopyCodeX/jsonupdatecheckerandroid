# JSON-based Update Checker

> ☑ Customizeable
---
> ☑ Easy to implement
---
> ☑ JSON-based checker
---
> ☑ Can download app
---
> ☑ Can auto install app
---
> ☑ Automatically check for updates
---
> ☑ Lightweight library
---
Setup
---
> Initialize UpdateChecker
```java
UpdateChecker checker = UpdateChecker.getInstance(context);

```
---
> Set custom json reader
```java
checker.setJsonReader(new MyCustomJsonReader());

private class MyCustomJsonReader extends JSONReader
{
    @Override
    public NewUpdateInfo readJson(String json) throws Exception
    {
       JSONObject job = new JSONObject(json);
       int vcode = job.getInt("versionCode");
       String vname = job.getString("versionName");
       String url = job.getString("downloadUrl");
       String desc = job.getString("description");
       
       return (new NewUpdateInfo(url, desc, vname, vcode));
    }
}
```
---
> Enable auto update
```java
checker.shouldAutoRun(true);
```
---
> Enable update on both mobile networks
```java
checker.shouldCheckUpdateOnWifiOnly(false);
```
---
> Enable auto installation
```java
checker.shouldAutoInstall(true);
```
---
> Set the url of the json file
```java
checker.setUpdateLogsUrl("https://urlhere");
```
---
> Downloading the app
```java
//Returns the filepath of the downloaded app
UpdateChecker.downloadUpdate("https://urlHere");
```
---
> Installing the app manually
```java
UpdateChecker.installApp(file);
```
----
> Add listener
```java
checker.setOnUpdateDetectedListener(new UpdateChecker.OnUpdateDetectedListener() {
    @Override
    public void onUpdateDetected(NewUpdateInfo info)
    {}
});
```
---
