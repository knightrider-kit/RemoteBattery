package mkk.rbr;

import android.app.*;
import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class ReceiverService extends Service
{
	private Notification notification;
	NotificationManager notifyMan;
	private RemoteViews rV;
	Thread Thread3 = null;
	Thread Thread4 = null;
	private static String SERVER_IP = "0.0.0.0";
	private static final int SERVER_PORT = 1977;
	Socket socket;
	private BufferedReader input;
	String message;
	boolean bl = true;
	
	@Override
	public IBinder onBind(Intent p1)
	{
		return null;
	}
	
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
	public void onCreate()
	{

		try
		{
			SERVER_IP = getServerIpAddress();
		}
		catch (UnknownHostException e)
		{}
		
		this.createNotification();
		notifyMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		startForeground(3000, notification);
		Thread3 = new Thread(new Thread3());
		Thread3.start();
	
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return super.START_STICKY;
	}
	
	public void onDestroy()
	{
		Thread3.interrupt();
		bl = false;
		notifyMan.cancel(3000);
		super.onDestroy();
	}
	
	class Thread3 implements Runnable {
		
		@Override
		public void run() {

			try {
				socket = getSocket(SERVER_IP, SERVER_PORT);
				if(socket!=null){
				new Thread(new Thread4()).start();
				if(Thread.interrupted()){
					return;
				}
				}else{
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{}

					Thread3 = new Thread(new Thread3());
					Thread3.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class Thread4 implements Runnable
	{

		@Override
		public void run() {
			System.out.println("mkk_tag t4 "+bl);
			while (bl) {			
				try{
					input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					final String message = input.readLine();
					if (message == null) {
						Thread3 = new Thread(new Thread3());
						Thread3.start();
						return;							
					} else {
						new Printout(message);						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class Printout
	{
		private Notification notification;
		Context ct;
		String message;
		
		private Printout(String message){
			this.message = message;
			run();
		}
		
		public void run()
		{
			ct = getApplicationContext();
			String lvl = message.substring(0, message.lastIndexOf("."));
			int bat = Integer.parseInt(lvl);
			if(message.contains("Not")){

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
			rV.setTextViewText(R.id.servicenotificationTextView1, message);
			notifyMan.notify(3000, notification);
			if(!bl){notifyMan.cancel(3000);}
		}

		public void createNotification(int icon)
		{
			rV = new RemoteViews(getPackageName(), R.layout.service_notification);
			rV.setTextViewText(R.id.servicenotificationTextView1, null);
			Intent intent = new Intent(ct, MainActivity.class);
			PendingIntent pendingIntent =
				PendingIntent.getActivity(ct, 0, intent, 0);
			this.notification = new Notification.Builder(this.ct)
				.setContent(rV)
				.setContentIntent(pendingIntent)
				.setSmallIcon(icon)
				.setPriority(3000).build();
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
}
