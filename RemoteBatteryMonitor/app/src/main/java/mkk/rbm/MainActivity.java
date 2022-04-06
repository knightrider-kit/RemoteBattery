package mkk.rbm;

import android.app.*;
import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import android.preference.*;
import android.widget.CompoundButton.*;

public class MainActivity extends Activity 
{
	SharedPreferences myPref;
	private TextView batteryTxt, tv2, tv3;
	Button bt;
	Switch sw;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		batteryTxt = (TextView)findViewById(R.id.mainTextView1);
		tv2 = (TextView)findViewById(R.id.mainTextView2);
		tv3 = (TextView)findViewById(R.id.mainTextView3);
		bt = (Button)findViewById(R.id.mainButton1);
		sw = (Switch)findViewById(R.id.mainSwitch1);
		

		this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		startService(new Intent(MainActivity.this, MonitorService.class));
		myPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		if(myPref.contains("bootOn"))
		{
			sw.setChecked(myPref.getBoolean("bootOn", false));
		}
		
		sw.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton p1, boolean p2)
				{
					if(sw.isChecked())
					{
						myPref.edit().putBoolean("bootOn", true).commit();
					}else{
						myPref.edit().putBoolean("bootOn", false).commit();
					}
				}

			
		});

		bt.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if(isMyServiceRunning(MonitorService.class)){
						stopService(new Intent (MainActivity.this, MonitorService.class));
						bt.setText("Start Service");
					}else{
						startService(new Intent (MainActivity.this, MonitorService.class));
						bt.setText("Stop Service");
					}		
				}
			});
    }
	
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int st = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			int pl = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

			float batteryPct = level * 100 / (float)scale;
			batteryTxt.setText(String.valueOf(batteryPct) + "%");
			//tv2.setText(String.valueOf(st));
			if(pl == 0)
			{tv3.setText("Not Charging");}
			else if(pl==1)
			{tv3.setText("Charging AC");}
			else if(pl==2)
			{tv3.setText("Charging USB");}
			else if(pl==3)
			{tv3.setText("Charging NFC");}
		}
	};
	
	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> service = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : service) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
				return true;
			}
		}
		return false;
	}
	
	//@Override
	public void onPointerCaptureChanged(boolean hasCapture)
	{
		// TODO: Implement this method
	}
}
