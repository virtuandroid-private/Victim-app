<div align="center">

  # VirtualXposed Victim app

</div>

### Introduction

This is a sample Android application on which we test our [attack modules](https://github.com/virtuandroid-private/Attacks-private) on.
The app performs basic operations such as network calls, file access and fingerprinting. 
The results of these operations are shown in the app interface, which makes it immediately visible when operations are modified by 
attack modules.

### Building

This app can be built and installed within Android Studio as a normal Android app.
The app can also be built from the command line using:

Linux:
```sh
./gradlew build
```
Windows:
```bat
gradlew.bat build
```


The resulting APK file can be found in `app/build/outputs/apk`

### Installation

To automatically install the app to VirtualXposed use the `installToXposed` task:

Linux:
```sh
./gradlew installToXposed
```
Windows:
```sh
gradlew.bat installToXposed
```