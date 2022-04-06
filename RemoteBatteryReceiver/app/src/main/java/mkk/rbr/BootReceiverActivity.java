package mkk.rbr;


import android.content.*;
import android.preference.*;

public class BootReceiverActivity extends BroadcastReceiver
 {
	SharedPreferences myPref;
	
	public void onReceive(Context context, Intent intent)
	{
		myPref = PreferenceManager.getDefaultSharedPreferences(context);
		
		if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && myPref.getBoolean("bootOn", false))
		{
			Intent serviceIntent = new Intent(context, ReceiverService.class);
			context.startService(serviceIntent);
		}
	}	
}

