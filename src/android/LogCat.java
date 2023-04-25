package org.apache.cordova.logcat;
import java.io.File;
import java.io.IOException;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.os.Environment;
import android.app.Activity;


public class LogCat extends CordovaPlugin {
	
	private volatile boolean mIsLogcatStarted;
    	private volatile Thread mLogcatThread;
    	private final Object mProcessLock = new Object();
    	private volatile long mLogcatCount;
    	private final List<String> mProcessWithLogs = new ArrayList<>();
    	private final Handler mMainHandler;
	
	protected void pluginInitialize() {
	  }
	
	private class LogcatRecorder extends Thread {
        @Override
        public void run() {
            BufferedReader logcatReader = null;
            Process process;
            try {
                Log.i(TAG, "Thread started, opening logcat process");
                try {
                    //process = Runtime.getRuntime().exec("logcat");
                    //logcatReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			process = Runtime.getRuntime().exec("logcat logcat -b events");
			logcatReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                } catch (IOException e) {
                    Log.i(TAG, "Failed to start logcat reading thread", e);
                    return;
                }

                try {
                    Log.i(TAG, "Thread started, starting to read");
                    String line;
                    while (true) {
                        if (!mIsLogcatStarted) {
                            Log.i(TAG, "logcat is stopped; leaving thread");
                            process.destroy();
                            break;
                        }
                        Log.i(TAG, "read");
                        line = logcatReader.readLine();
                        if (line == null) {
                            try {
                                Thread.sleep(200);
                           } catch (InterruptedException e) {
                                Log.i(TAG, "logcat thread interrupted", e);
                            }
                        } else {
                            Log.i(TAG, "line=" + line);
                            String[] tokens = line.replaceAll("  ", " ").split(" ", 6);
                            Log.i(TAG, "token 2=" + tokens[2]);
                            synchronized (mProcessLock) {
                                mLogcatCount++;
                                if (! mProcessWithLogs.contains(tokens[2])) {
                                    mProcessWithLogs.add(tokens[2]);
                                }
                            }
                        }
                        if (!mIsLogcatStarted) {
                            Log.i(TAG, "logcat is stopped; leaving thread");
                            break;
                        }
                    }
                } catch (IOException e) {
                    Log.i(TAG, "Failed to read from logcat", e);
                }
            } finally {
                if (logcatReader != null) {
                    try {
                        logcatReader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
                synchronized (mProcessLock) {
                    Log.i(TAG, "end of thread");
                    if (mLogcatThread == this) {
                        mLogcatThread = null;
                    }
                    mProcessLock.notifyAll();
                }
            }
        }
    }
	private void clearData() {
        // thread cannot be running
        mProcessWithLogs.clear();
        mLogcatCount = 0;
        updateView();
    }

    private void updateView() {
        Log.i(TAG, "updateView isLogcatStarted=" + mIsLogcatStarted);
        StringBuilder sb = new StringBuilder();
        synchronized (mProcessLock) {
            if (mLogcatCount == 0) {
                if (mIsLogcatStarted) {
                    sb.append("Waiting for first line...");
                } else {
                    sb.append("None. Stopped.");
                }
            } else {
                for (String a : mProcessWithLogs) {
                    if (sb.length() != 0) {
                        sb.append('\n');
                    }
                    sb.append(a);
                }
            }
        }
        mProcessView.setText(sb.toString());
        mLineCountView.setText(Long.toString(mLogcatCount));

        if (mIsLogcatStarted) {
            mMainHandler.postDelayed(this::updateView, 500);
        }
        Log.i(TAG, "updateView exit");
    }

    private void startThread() {
        synchronized (mProcessLock) {
            if (mLogcatThread != null) {
                // just wait a little
                long end = System.currentTimeMillis() + 10000;
                while (mLogcatThread != null && System.currentTimeMillis() < end) {
                    try {
                        mProcessLock.wait(300);
                    } catch (InterruptedException e) {
                        Log.i(TAG, "startThread interrupted", e);
                        return;
                    }
                }
                if (mLogcatThread != null) {
                    Log.e(TAG, "thread still running");
                    return;
                }
            }

            mLogcatThread = new LogcatRecorder();
            mLogcatThread.setDaemon(true);
            mLogcatThread.start();
        }
    }
	
	 private void dumpService(@NonNull String service) {
        File tmpFile = new File(getCacheDir(), "service." + service + ".txt");
        try {
            tmpFile.delete();
        } catch (Exception e) {
            // ignore
        }
        Log.i(TAG, "Dumping service " + service);
        try {
            FileOutputStream fos = new FileOutputStream(tmpFile);
            // this requires the DUMP permission, which is not likely to be there.
            if (!Debug.dumpService(service, fos.getFD(), new String[0])) {
                Log.i(TAG, "Failed to dump service " + service);
            }
            fos.getFD().sync();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Dumping service " + service + " finished");
        int lineCount = 0;
        try {
            FileReader fr = new FileReader(tmpFile);
            BufferedReader bis = new BufferedReader(fr);
            String line;
            while ((line = bis.readLine()) != null) {
                Log.i(TAG, "line: " + line);
                lineCount++;
            }
            bis.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Dump of " + service + " produced " + lineCount + " lines", Toast.LENGTH_LONG).show();
        try {
            tmpFile.delete();
        } catch (Exception e) {
            // ignore
        }
    }

	  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) 
	      throws JSONException {
	    if (action.equals("sendLogs")) {
                        // save logcat in file
		    //System.out.println(Environment.getDataDirectory());
		    //Environment.getExternalStorageDirectory()
		    //Environment.getDownloadCacheDirectory()
		    
		Activity activity = cordova.getActivity();
		String packageName = activity.getPackageName();    
		    
                File outputFile = new File("/storage/emulated/0/Android/data/"+packageName+"/files/",
                        "logcat.txt");
                try {
                    Runtime.getRuntime().exec(
                            "logcat -f " + outputFile.getAbsolutePath());
			//System.out.println(outputFile.getAbsolutePath());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception ex) {
		    ex.printStackTrace();
		}
             return true;
	    }
          else{        
	    return false;
	  }
      }
}
