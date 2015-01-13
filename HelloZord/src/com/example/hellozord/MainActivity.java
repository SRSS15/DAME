package com.example.hellozord;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			final String libPath = Environment.getExternalStorageDirectory()
					+ "/dex.jar";
			final File tmpDir = getDir("dex", 0);
			final DexClassLoader classloader = new DexClassLoader(libPath,
					tmpDir.getAbsolutePath(), null, this.getClassLoader());
			final Class<Object> classToLoad = (Class<Object>) classloader
					.loadClass("org.prova.Main");
			final Object myInstance = classToLoad.newInstance();
			final Method doSomething = classToLoad.getMethod("doSomething");
			doSomething.invoke(myInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
