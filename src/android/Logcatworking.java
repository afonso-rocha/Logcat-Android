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
	protected void pluginInitialize() {
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
                    //Runtime.getRuntime().exec(
                            //"logcat -f " + outputFile.getAbsolutePath());
			//System.out.println(outputFile.getAbsolutePath());
			Process process = Runtime.getRuntime().exec("logcat logcat -b events");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			StringBuilder log=new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				Toast.makeText(LogReaderService.this, line, Toast.LENGTH_LONG).show();
			}
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
