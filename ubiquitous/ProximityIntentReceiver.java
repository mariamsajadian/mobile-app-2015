package com.sajadian.ubiquitous;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

public class ProximityIntentReceiver extends BroadcastReceiver {

	private static final int NOTIFICATION_ID = 1000;

	@Override
	public void onReceive(Context context, Intent intent) {
		String key = LocationManager.KEY_PROXIMITY_ENTERING;

		Boolean entering = intent.getBooleanExtra(key, false);
		String name = intent.getStringExtra("name");
		String loc=intent.getStringExtra("loc");
		String rate = intent.getStringExtra("rate");
		String blood=intent.getStringExtra("blood");
		String signs=intent.getStringExtra("signs");

		if (!entering) {
			return;
		}

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		final String private_number = settings.getString("private_number", "");

		try {
			PollutionService.removeProximityAlert();
			if (private_number != ""){
				String msg = "I'm in: \n" + name + "\n";
				msg+="It is my location: \r\nhttp://maps.google.com/?q=" + loc;
				msg+=rate==""?"":"Heart Rate: " + rate + "\n";
				msg+=blood==""?"":"Blood Presure: " + blood + "\n";
				msg+=signs==""?"":"Sings: " + signs + "\n";
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(private_number, null, msg, null, null);
			}
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			Bitmap bmp=BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_launcher);
			Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			
			Notification notification = new Notification.Builder(context)
					.setContentTitle("Arrival alert")
					.setContentText("You are near hospital.")
					.setTicker("You are near hospital.")
					.setSmallIcon(R.drawable.location)
					.setLargeIcon(bmp)
					.setAutoCancel(true)
					.setSound(alarmSound)
					.setVibrate(new long[]{500,500,500})
					.build();
			notificationManager.cancelAll();
			notificationManager.notify(NOTIFICATION_ID, notification);
		} catch (Exception e) {
			Log.e("sendig SMS", e.getMessage());
		}
	}
}
