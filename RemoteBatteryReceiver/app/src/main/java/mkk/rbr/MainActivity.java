package mkk.rbr;

import android.app.*;
import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.CompoundButton.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

import android.view.View.OnClickListener;


public class MainActivity extends Activity 
{
	Thread Thread1 = null;
	TextView tv1, tv2;
	String SERVER_IP;
	int SERVER_PORT = 1977;
	Socket socket;
	private BufferedReader input;
	SharedPreferences myPref;
	Button bt;
	Switch sw;
	boolean bl = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		tv1 = (TextView)findViewById(R.id.mainTextView1);
		tv2 = (TextView)findViewById(R.id.mainTextView2);
		bt = (Button)findViewById(R.id.mainButton1);
		sw = (Switch)findViewById(R.id.mainSwitch1);
		
		try
		{
			SERVER_IP = getServerIpAddress();
		}
		catch (UnknownHostException e)
		{}
		startService(new Intent (MainActivity.this, ReceiverService.class));
		myPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Thread1 = new Thread(new Thread1());
		Thread1.start();
		

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
					if(isMyServiceRunning(ReceiverService.class)){
						stopService(new Intent (MainActivity.this, ReceiverService.class));
						bt.setText("Start Service");
					}else{
						startService(new Intent (MainActivity.this, ReceiverService.class));
						bt.setText("Stop Service");
					}		
				}
			});
	}

	@Override
	protected void onDestroy()
	{
		Thread1.interrupt();
		bl = false;
		super.onDestroy();
	}
	
	
	class Thread1 implements Runnable {
		@Override
		public void run() 
		{
			if(Thread.interrupted())
			{
				return;
			}else{			
				try {
					socket = getSocket(SERVER_IP, SERVER_PORT);
					if(socket!=null)
					{
						input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						runOnUiThread(new Runnable() 
						{
							@Override
							public void run() 
							{
								tv2.setText("Connected");
							}
						});
					new Thread(new Thread2()).start();
					}else{
						runOnUiThread(new Runnable() 
						{
							@Override
							public void run() 
							{
								tv1.setText("");
								tv2.setText("Not Connected");
							}
						});
						
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{}

						Thread1 = new Thread(new Thread1());
						Thread1.start();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	class Thread2 implements Runnable {
		@Override
		public void run() {
			while (bl) {
					try{
					final String message = input.readLine();
					if (message == null) {
						Thread1 = new Thread(new Thread1());
						Thread1.start();
						return;							
					} else {
						runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								tv1.setText(message);
							}
						});
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getServerIpAddress() throws UnknownHostException {
		WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		DhcpInfo dhc = wifiManager.getDhcpInfo();
		int i = dhc.gateway;
		return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(i).array()).getHostAddress();
	}
	
	public Socket getSocket(String SERVER_IP, int SERVER_PORT){
		try
		{
			Socket socket = new Socket(SERVER_IP, SERVER_PORT);
			return socket;
		}catch(Exception e){
			return null;
		}
	}
	

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
