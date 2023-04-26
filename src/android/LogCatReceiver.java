public class LogCatReceiver extends BroadcastReceiver { 
	private static final String TAG = "LogCatReceiver";
	
		@Override 
		public void onReceive(Context context, Intent intent) { 
			Log.i(TAG, "onReceive: action=" + intent.getAction());
			if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
				
				Intent i = new Intent(context, DafCanService.class); 
				context.startForegroundService(i); 
				
			} else {
				
				intent.setClass(context, DafCanService.class); 
				context.startService(intent);	
				
			}
		}

}
