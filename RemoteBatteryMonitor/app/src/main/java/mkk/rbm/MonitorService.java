package mkk.rbm;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MonitorService extends Service
{
	private Notification notification;
	NotificationManager notifyMan;
	private RemoteViews rV;
	String all;
	float batteryPct;
	private String plugged;
	ServerSocket serverSocket;
	Thread Thread1 = null;
	public static String SERVER_IP = "0.0.0.0";
	public static final int SERVER_PORT = 1977;
	String message;
	int pl;
	TimerTask task;
	
	

	public void createNotification()
	{
		if (this.notification != null){
			return;
		}

		rV = new RemoteViews(getPackageName(), R.layout.service_notification);
		rV.setTextViewText(R.id.servicenotificationTextView1, "Its null");
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent =
			PendingIntent.getActivity(this, 0, intent, 0);
		this.notification = new Notification.Builder
		(this.getApplicationContext())
		.setContent(rV)
		.setContentIntent(pendingIntent)
		.setSmallIcon(R.drawable.batt_unknown)
		.setPriority(3000).build();
	}
	
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}
	
	public void onCreate()
	{
		


		String p2p = getWifiApIpAddressP2p();
		if (p2p!=null)
		{
			SERVER_IP = p2p;
		}else{
			String wlan = getWifiApIpAddressWlan();
			if(wlan!=null)
			{
				SERVER_IP = wlan;				
			}
		}

		
		
		this.createNotification();
		notifyMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		startForeground(3000, notification);
		//notifyMan.notify(3000, notification);
		

		this.registerReceiver(
		this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		Timer timer = new Timer();
		task = new Printout();
		timer.schedule(task, 2, 1000);
		Thread1 = new Thread(new Thread1());
		Thread1.start();
		super.onCreate();
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return super.START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		task.cancel();
		notifyMan.cancel(3000);
		super.onDestroy();
	}
	

	class Printout extends TimerTask
	{

		private Notification notification;
		Context ct;

		@Override
		public void run()
		{
			ct = getApplicationContext();
			rV.setTextViewText(R.id.servicenotificationTextView1, all);
			int bat = (int) batteryPct;

			if(pl==0){

				if(bat>=100){
					this.createNotification(0x7f020007);
				}else if(bat>=90){
					this.createNotification(0x7f020006);
				}else if(bat>=75){
					this.createNotification(0x7f020005);
				}else if(bat>=60){
					this.createNotification(0x7f020004);
				}else if(bat>=49){
					this.createNotification(0x7f020003);
				}else if(bat>=35){
					this.createNotification(0x7f020002);
				}else if(bat>=15){
					this.createNotification(0x7f020001);
				}else if(bat>=00){
					this.createNotification(0x7f020000);
				}else{
					this.createNotification(0x7f020010);
				}
			}else{

				if(bat>=100){
					this.createNotification(0x7f02000f);
				}else if(bat>=90){
					this.createNotification(0x7f02000e);
				}else if(bat>=75){
					this.createNotification(0x7f02000d);
				}else if(bat>=60){
					this.createNotification(0x7f02000c);
				}else if(bat>=49){
					this.createNotification(0x7f02000b);
				}else if(bat>=35){
					this.createNotification(0x7f02000a);
				}else if(bat>=15){
					this.createNotification(0x7f020009);
				}else if(bat>=00){
					this.createNotification(0x7f020008);
				}else{
					this.createNotification(0x7f020010);
				}
			}
			notifyMan.notify(3000, notification);
			
		}
		

		public void createNotification(int icon)
		{
			rV = new RemoteViews(getPackageName(), R.layout.service_notification);
			rV.setTextViewText(R.id.servicenotificationTextView1, all);
			Intent intent = new Intent(ct, MainActivity.class);
			PendingIntent pendingIntent =
				PendingIntent.getActivity(ct, 0, intent, 0);
			this.notification = new Notification.Builder(ct)
				.setContent(rV)
				.setContentIntent(pendingIntent)		
				.setSmallIcon(icon)
				.setPriority(3000)
				.build();
		}
		
	}
	
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){

		
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
			int st = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			pl = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

			batteryPct = level * 100 / (float)scale;
			
			String batteryTxt = String.valueOf(batteryPct) + "%";

			if(pl == 0)
			{plugged="Not Charging";}
			else if(pl==1)
			{plugged="Charging AC";}
			else if(pl==2)
			{plugged="Charging USB";}
			else if(pl==3)
			{plugged="Charging NFC";}
			all = new String(batteryTxt+"  "+plugged);
			
		}
	};
	
	class Thread1 implements Runnable {
		@Override
		public void run() {
			Socket socket;
			//while (true){
			try {
				serverSocket = new ServerSocket(SERVER_PORT);
				while (true){
					try {
						socket = serverSocket.accept();

						Timer timer = new Timer();
						TimerTask task2 = new Thread2(socket);
						timer.schedule(task2, 2, 1000);
					} catch (IOException e) {
						e.printStackTrace();
					}}
			} catch (IOException e) {
				e.printStackTrace();
			}//}
		}
	}

	class Thread2 extends TimerTask implements Runnable {
		
		protected Socket socket = null;
		
		Thread2(Socket socket){
			this.socket = socket;
		}
		
		@Override
		public void run() {
			try
			{
				PrintWriter output = new PrintWriter(socket.getOutputStream());
				output.println(all);
				output.flush();
			}
			catch (IOException e)
			{}
		}
	}
	
	public String getWifiApIpAddressP2p()
	{
		try
		{
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
			{
				NetworkInterface intf = en.nextElement();
				if (intf.getName().contains("p2p"))
				{
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
					{
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4))
						{

							return inetAddress.getHostAddress();
						}
					}
				}
			}
		} catch (SocketException ex) {}
		return null;
	}

	public String getWifiApIpAddressWlan()
	{
		try
		{
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
			{
				NetworkInterface intf = en.nextElement();
				if (intf.getName().contains("wlan"))
				{
					for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
					{
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress() && (inetAddress.getAddress().length == 4))
						{
							return inetAddress.getHostAddress();
						}
					}
				}
			}
		} catch (SocketException ex) {}
		return null;
	}

	
}
