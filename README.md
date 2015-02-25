# DAME - Dynamic Android Malware Engine

![DAME logo](logo.png)	

*University of Sannio - Software and Network Security - A.Y. 2014-2015*

__WARNING: this project is for educational purposes and the authors are not responsible for any illegal use of the product__

                             			   
## I. AUTHORS
* [Danilo Cianciulli](mailto:cianciullidanilo@gmail.com)
* [Ermanno Francesco Sannini](mailto:esannini@gmail.com)
* [Roberto Falzarano](mailto:robertofalzarano@gmail.com)

## II. CONTENTS

1. <a href="#INTRODUCTION">Introduction</a>
1. <a href="#PREREQUISITES">Prerequisites</a>	
	2.1. <a href="#USAGE_PREREQUISITES">Usage prerequisites</a>	
 	2.2. <a href="#DEVELOPMENT_PREREQUISITES">Development prerequisites</a>
1. <a href="#USAGE">Usage</a>	
	3.1. <a href="#OPTIONS">Options</a>	
    3.2. <a href="#FTP_SERVER_CONFIGURATION">FTP server configuration</a>	
	3.3. <a href="#PAYLOADS">Payloads</a>	
	3.4. <a href="#OUTPUTS">Outputs</a>
1. <a href"#DEVELOPMENT">Development</a>	
	4.1. <a href"#PAYLOADS_DEVELOPMENT">Payloads development</a>	
	4.2. <a href="#BINARY_BUILDING">Binary building</a>


<a name="INTRODUCTION"></a>
## 1. Introduction

DAME (Dynamic Android Malware Engine) is a system which enables to inject malicious code
in Android applications through a dynamic loading process.	
As opposed to other solutions, it does not change permissions used by the application because
it allows to inject only malwares that are compatible with the permissions used by the
application. DAME injects the malicious code only in that methods in which the trusted
application legitimately uses the permissions required. This method makes the malware
detection hard to accomplish.

<a name="PREREQUISITES"></a>
## 2. Prerequisites

This section lists the prerequisites that are differentiated depending if your intent is
to develop or use only.

<a name="USAGE_PREREQUISITES"></a>
### 2.1. Usage prerequisites

Before using DAME, you have to make sure that you have installed the following software
packets:

* [Java SE JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (>= 1.7)
* [Android SDK Tools](http://developer.android.com/sdk/index.html)
* [Androguard](https://code.google.com/p/androguard/)
* [Python](https://www.python.org/downloads/) (>= 2.7.6)

Alternatively, you can execute DAME in the A.R.E (Android Reverse Engineering) virtual machine, downloadable from [here](https://redmine.honeynet.org/projects/are/wiki).	
A.R.E. has obsolete software, so, if you want to use it, it is mandatory to update Java, Android SDK Tools and Python to the latest available versions.

If you are not interested in develompent, you can jump to section 3.

<a name="DEVELOPMENT_PREREQUISITES"></a>
### 2.2. Development prerequisites

Before developing DAME, you have to make sure that you have installed the following software
packets:

* [Java SE JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (>= 1.7)
* [Android SDK Tools](http://developer.android.com/sdk/index.html)
* [Androguard](https://code.google.com/p/androguard/)
* [Ant](http://ant.apache.org/) (>= 1.9.3)
* [Maven](http://maven.apache.org/) (>= 3.2.3)
* [Python](https://www.python.org/downloads/) (>= 2.7.6)

As for the usage, also the development can be done in the A.R.E. virtual machine.	
Obviously, due to outdated software, it is mandatory to update Java, Android SDK Tools, Ant,
Maven and Python to the latest available versions.

Before developing, it is also required to follow the subsequent step in order to install the
Android API version 19 in the local Maven repository.

1. Update your own Android SDK and install the version 21.1.2 of the build tools, and the
	  version 19 of the Android API. 
1. Annotate the position of the ```android.jar``` file relative to the version 19 of the API, for example
		/opt/android-sdk/platforms/android-19/android.jar
1. From a terminal, execute the following command:
		$ mvn install:install-file -Dfile=<android-jar> -DgroupId=com.google.android
				-DartifactId=android -Dversion=19 -Dpackaging=jar -DgeneratePom=true
   Where ```<android-jar>``` is the path annotated in the step 2.

To make the Ant scripts properly work, it is required to update the ```local.properties``` file
to adapt it to your own environment.	
In order to accomplish this, follow these steps.

1. Annotate the path of the ```dame-android``` project. It might be ```~/dame/dame-android``` if the parent project has been unpacked in the home directory.
2. Move to ```tools``` directory of your own Android SDK, for instance
		/opt/android-sdk/tools
3. From a terminal, execute the command:
		$ ./android update project -p <dame-android-path>
   Where ```<dame-android-path>``` is the path annotated in the step 1.

These steps allow you to generate the ```local.properties``` file in the root of ```dame-android``` which contains
your SDK path.

<a name="USAGE"></a>
## 3. Usage

In the ```releases``` directory there is the archive that contains the last DAME release.	
Unpack the archive where you want.

If you want to execute DAME, you must run the ```dame``` script.

DAME provides a command line interface:

	$ dame [options...] FILE.apk

The only mandatory parameter is the ```FILE.apk``` that refers to the APK on which you want to use DAME.

<a name="OPTIONS"></a>
### 3.1. Options

The following list shows various options that is allowed by DAME.	
(If you execute the dame script without parameters, it is displayed on the screen)

```
 -ag (--androguard) VAL          : androguard path (~/tools/androguard/ by
                                   default)
 -at (--apktool) VAL             : apktool path (./tools/apktool by default)
 -bt (--android-build-tools) VAL : Android build tools path (~/tools/android/and
                                   roid-sdk-linux_x86/build-tools/21.1.2/ by
                                   default)
 -c (--ftp-server-config) FILE   : ftp server file config (config.properties in
                                   the source apk path by default)
 -o (--output) VAL               : apk file dest path (out.apk in the source
                                   apk path by default)
 -py VAL                         : python path (system path by default)
 -v (--version)                  : Prints the engine version and build time.
```

The ```-ag``` option (or ```--androguard```) suggests to the engine the Androguard installation directory. If you don't pass this option, it uses the default path ```~/tools/androguard```. If the path is different and if you don't want to pass right path every time, you can make a symbolic link:

	$ ln -s <real-androguard-path> ~/tools/androguard
    
May be required to replace ~ with your home absolute path.

The ```-at``` option (or ```--apktool```) suggests to the engine the apktool position. DAME is already equipped with a version of apktool that is used by default, but with this option you might use a different one.

The ```-bt``` option (or ```--android-build-tools```) suggests to the engine the Android build tools path. It uses ```~/tools/android/android-sdk-linux_x86/build-tools/21.1.2/``` by default. If the path is different and if you don't want to pass every time the right path, you can make a symbolic link as well as the ```-ag``` option.

The ```-c``` option (or ```--ftp-server-config```) suggests to the engine the FTP server configuration file path that is used by the malicious application to download the payloads and to upload their execution outputs. By default, the engine looks for ```config.properties``` file in the same directory as the source APK. In the section 3.2 you can take a look at the structure of such configuration file.

The ```-o``` option (or ```--output```) suggests to the engine where to save the malicious APK. By default, the engine outputs the ```out.apk``` file in the same directory as the source APK; if a file with that name already exists, it is overwritten.

The ```-py``` option suggests to the engine where to look for Python interpreter. It is searched in system path by default.

Finally, the ```-v``` option (or ```--version```) shows the engine version and build date. It can be used without the source APK parameter.

When you execute dame with right options and parameters, the engine analyzes the source APK and suggests a set of payloads that is allowed to be injected. Every suggested payload uses one or more permissions that are already used in the application. You can choose which payloads the engine has to inject by pointing out the showed indexes separated by comma. When execution ends, you obtain the modified APK already signed and optimized that can be installed on a device.


<a name="FTP_SERVER_CONFIGURATION"></a>
### 3.2. FTP server configuration

The malicious app doesn't include the choosen payloads but only calls to them. On the first time the app legitimately access to the Internet, the modified app downloads the payloads, while, on every next time, it uploads the execution outputs. You must specify the URL addresses to download the payloads and to upload the output. You must specify this informations in a file that you must pass to the engine by the ```-c``` option (as shown above). 

The server configuration file must be formed as follows:

	server=<server ftp address with schema and port number, if it isn't 21>
	payload_uri=<path, relative to the server root, pointing to the payload jar file>
	result_uri=<path, relative to the server root, pointing to the directory where 
					you want to save the outputs>
	username=<login username>
	password=<login password>
	passive_mode=<true if you want to use passive mode, false otherwise>

For instance, you might have

	server=ftp://10.0.2.2
	payload_uri=/DAME/payloads.jar
	result_uri=/DAME/results/
	username=userftp
	password=srss2015
	passive_mode=false

In such example, the server uses the port 21 by default. If it is different, you must indicate, for instance,

	server=ftp://10.0.2.2:1234

The payloads jar file is located in the payloads-jar directory of the release. You need to load such file on your FTP server in the location defined by the configuration file. Make sure that your FTP server has the directory, specified in the result_uri field, accessible for writing, and that the file, indicated in payload_uri field, is accessible for reading.

<a name="PAYLOADS"></a>
### 3.3. Payloads

The payloads are units of malicious code that are dynamically loaded at run-time by the malicious app. Payloads uses permissions which must be already used by the trusted app, and could be executed once (on first call) or always (on every call).

In its latest release, DAME integrates the following payloads:

* **CallsLog**	
	This payload allows to obtain the call list. It is executed on every payload call.
* **IMEI**	
	This payload allows to obtain the device IMEI. It is executed only once.	
* **Networks**	
	This payload allows to obtain the list of saved nertowks. It is executed on every payload call.
* **ReadContacts**	
	This payload allows to obtain the contact list. Due to performance issues, it is executed only once.
* **SMSInbox**	
	This payload allows to obtain the SMS list. Due to performance issues, it is executed only once.
	
In order to develop other payloads, you should follow the development section, only after fulfilling all prerequisites.

<a name="OUTPUTS"></a>
### 3.4. Outputs

The output of payload executions are uploaded to the directory given in the FTP server configuration file.	
The nomenclature follows the syntax:

	<payload-name>-<date>-<time>.txt

Where

* ```<payload-name>``` is the payload identifier, for instance ```IMEI``` or ```SMSInbox```;
* ```<date>``` is the date in *yyyyMMdd* format, for instance ```20150217``` indicates *February 17, 2015*.
* ```<time>``` is the time including milliseconds in *HHmmssSSSS* format, for instance ```152645874``` indicates *3:26:45.874 PM*.
	  

<a name="DEVELOPMENT"></a>
## 4. Development

Before reading this section, you sould follow the instructions in section 2.2.

The DAME project uses Maven as dependency management and installation system.

DAME is made-up of two sub-projects: ```dame-engine``` and ```dame-android```. The former contains the DAME logic with regard to the source APK manipulation and the generation of the malicious one. The latter, from which the former depends, contains
Android classes and services that are injected in the APK. ```dame-engine``` uses ```dame-android``` for the generation of smali code used in the injection.

<a name="PAYLOADS_DEVELOPMENT"></a>
### 4.1. Payload development

In addition to the default payloads, in the ```dame-android``` project, you can develop others of them by following the subsequent rules.

1. For each payload, make a sub-package of ```it.unisannio.srss.dame.android.payloads```;
2. In the package created in step 1, make a class which extends
		it.unisannio.srss.dame.android.payloads.Payload
3. In the class created in step 2, implement two constructors:	
	* The former, without arguments which calls ```super()```, is used by the engine for the payload enumeration and configuration loading;	
	* The latter must have one parameter ```android.content.Context``` which must be passed to the super-class.
4. Implement the method ```public synchronized void run();``` in which encode the payload logic, eventually using the ```Context``` object obtainable from the super-class.
5. To save the payload output, use the ```save(String)``` method inherited from the super-class.

The Android services injected by the engine take care of calling the run() method at the right time, as well as provide the storing logic.

For instance, if you want to make a payload named MyPayload, you might have:

```java
package it.unisannio.srss.dame.android.payloads.mypayload;

import it.unisannio.srss.dame.android.payloads.Payload;
import android.content.Context;

public class MyPayload extends Payload {
	
	public MyPayload() {
		super();
	}
	
	public MyPayload(Context context) {
		super(context);
	}
	
	// @Override
	public synchronized void run() {
		// payload logic
		// use this.context if needed
		// use save(String) to store the output
	}
}
```

For each payload must be specified a configuration file named ```payload.properties``` which must be located in the same package of the class which extends ```Payload```. Such file must contains the following fields:

```cmake
# whatever (required, must be unique)
name=<payload-name>
# whatever (not required)
description=<description>
# comma separated permissions (not required)
permissions=<permission[,permission...]>
# once or always. If missing, assumes once
execution=<once-or-always>
```

The payload ```name``` must be unique. All other fields can be omitted.	
In the ```permissions``` field, you have to write all permissions (comma-separated) required by the payload. The prefix ```android.permission``` prefix is not required.	
**Pay attention**: the permissions field is used by the engine to locate the right injection points in the application; if your payload does not require any permission, then it won't be injected at all. In this case, you may want use a common permission even if not used by the payload.	
The ```execution``` field, if it is ```once```, make sure that the payload is executed only once. If ```always``` is used, the payload can be executed more times.

In relation to the previous example, you might have the following configuration:

	name=MyPayload
	description=This payload does nothing
	permissions=READ_CONTACTS
	execution=once
    
<a name="BINARY_BUILDING"></a>
### 4.2 Binary building

To compile and to build binary files, you must follow this process:

1. Open a terminal and move to parent project root directory.
2. Execute the command
		$ mvn clean install

When this process ends, you can find in ```dame-engine/dist``` directory all you need to execute DAME. At the same time, in the release directory, a ```tar.gz``` file is created that is named according to project version defined in the pom file. The archive contains everything is contained in ```dame-engine/dist```.