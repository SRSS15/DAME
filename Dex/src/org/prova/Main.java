package org.prova;

import android.util.Log;
/*
 * Il .jar deve contenere il file .dex
 * Per il .jar-> javac -verbose -d (dove salvare) -classpath (lib android.jar) *.java
 * Per il .dex-> dx --dex --verbose --output=... source.jar
 * Per ora il .jar viene salvato sul device 
 * Per copiare sul device-> adb push (source) /mnt/sdcard/ 
 */

public class Main {
    public Main() {
        Log.d(Main.class.getName(), "MyClass: constructor called.");
    }

    public void doSomething() {
        Log.d(Main.class.getName(), "MyClass: doSomething() called.");
    }
}
