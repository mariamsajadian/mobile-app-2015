package com.sajadian.ubiquitous;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PollutionService {

	private static Context context;
	private GoogleMap map;
	private Pollution _pollution;
	private final GetPollution getpollution;
	private static final String PROX_ALERT_INTENT = "com.sajadian.ubiquitous";
	private static LocationManager locationManager;

	public PollutionService(Context context) {
		PollutionService.context = context;
		getpollution = new GetPollution();
	}

	public Pollution GetPollution() {
		_pollution = new Pollution();
		Location l = getLocation();
		if (l != null) {
			if (getpollution.getStatus() == Status.PENDING)
				getpollution.execute(l);
		}
		return _pollution;
	}

	public Pollution GetPollution(GoogleMap map) {
		this.map = map;
		_pollution = new Pollution();
		Location l = getLocation();
		if (l != null) {
			if (getpollution.getStatus() == Status.PENDING)
				getpollution.execute(l);
		}
		return _pollution;
	}

	public Pollution GetPollution(GoogleMap map, LatLng location) {
		this.map = map;
		_pollution = new Pollution();
		Location l = new Location(LocationManager.GPS_PROVIDER);
		l.setLatitude(location.latitude);
		l.setLongitude(location.longitude);
		if (getpollution.getStatus() == Status.PENDING)
			getpollution.execute(l);
		return _pollution;
	}

	// Get current location
	private Location getLocation() {
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		// This line jump to CheckProviders function, get best enabled provider
		// and put it in provider variable
		String provider = checkProviders();

		if (provider != null) {
			// if last location is not null we return it
			Location l = locationManager.getLastKnownLocation(provider);
			if (l != null)
				return l;

			// else force GPS to update it, maybe some time wait...
			LocationListener ls = new LocationListener() {
				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				}

				@Override
				public void onProviderEnabled(String arg0) {
					CharSequence ch = "Waiting for location...";
					Toast t = Toast.makeText(context, ch, Toast.LENGTH_LONG);
					t.show();
				}

				@Override
				public void onProviderDisabled(String arg0) {
				}

				@Override
				public void onLocationChanged(Location arg0) {
					if (getpollution.getStatus() == Status.PENDING)
						getpollution.execute(arg0);
					MainActivity.UpdateLocation(_pollution);
				}
			};
			locationManager.requestSingleUpdate(provider, ls, null);
			return locationManager.getLastKnownLocation(provider);
		}
		// if no provider available return null :(
		return null;
	}

	// Checking provider that who is enabled
	private String checkProviders() {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		String provider = LocationManager.GPS_PROVIDER;
		// first check GPS
		if (locationManager.isProviderEnabled(provider)) {
			return provider;
		} else {
			CharSequence ch = "GPS is not available!";
			Toast t = Toast.makeText(context, ch, Toast.LENGTH_LONG);
			t.show();
			provider = LocationManager.NETWORK_PROVIDER;
			if (locationManager.isProviderEnabled(provider)) {
				return provider;
			}
		}

		// build dialog to show for enabling gps
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(
				"Your GPS seems to be disabled, do you want to enable it?")
				.setCancelable(false)
				// Yes button
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								context.startActivity(new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS));
							}
						})
				// No Button
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});

		// show dialog
		final AlertDialog alert = builder.create();
		alert.show();
		return null;
	}

	// Retrieve stations data from Internet
	private class GetPollution extends AsyncTask<Location, Void, String> {

		private Location l;

		@Override
		protected String doInBackground(Location... loc) {
			l = loc[0];
			String result;
			InputStream isr;
			// Connecting...
			try {
				HttpClient httpclient = new DefaultHttpClient();
				String l = loc[0].getLatitude() + "," + loc[0].getLongitude();
				HttpPost httppost = new HttpPost(
						"http://aasare.ir/msrouting.php?loc=" + l);
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				isr = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(isr, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				isr.close();
				result = sb.toString();
			} catch (Exception e) {
				Log.e("log_tag", "Error converting result" + e.toString());
				result = "";
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				if (result.equals("")) {
					Toast.makeText(context,
							"Error in receiving pollution data!",
							Toast.LENGTH_LONG).show();
					MainActivity.UpdateLocation(null);
					return;
				}
				int pollution = Integer.parseInt(result);

				if (_pollution != null) {
					_pollution = new Pollution();
					_pollution.setLocation(l);
					_pollution.setPollution(pollution);
				}
				MainActivity.UpdateLocation(_pollution);
				if (map == null)
					return;

				map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
						l.getLatitude(), l.getLongitude()), 15));
				String title = "";
				float markercolor = BitmapDescriptorFactory.HUE_BLUE;
				if (pollution > 0 && pollution <= 50) {
					title = "Safe";
					markercolor = BitmapDescriptorFactory.HUE_GREEN;
				} else if (pollution > 50 && pollution <= 100) {
					title = "Moderate";
					markercolor = BitmapDescriptorFactory.HUE_YELLOW;
				} else if (pollution > 100 && pollution <= 150) {
					title = "Unhealthy";
					markercolor = BitmapDescriptorFactory.HUE_ORANGE;
				} else if (pollution > 150) {
					markercolor = BitmapDescriptorFactory.HUE_RED;
					title = "Hazardous";
				}
				// create marker
				MarkerOptions marker = new MarkerOptions()
						.position(new LatLng(l.getLatitude(), l.getLongitude()))
						.title(title)
						.icon(BitmapDescriptorFactory
								.defaultMarker(markercolor));

				// adding marker
				map.addMarker(marker);
				if (pollution > 100) {
					MapActivity.direction.SearchHospital(l.getLatitude() + ","
							+ l.getLongitude(), 0);
				}

			} catch (Exception e) {
				Log.e("log_tag", "Error Parsing Data " + e.toString());
			}
		}
	}

	public static void AddProximityAlert(double latitude, double longitude, String name, int duration) {

		Intent intent = new Intent(PROX_ALERT_INTENT);
		intent.putExtra("name", name);
		intent.putExtra("loc", latitude + "," + longitude);
		MySQLiteHelper msql=new MySQLiteHelper(context);
		Pollution p=msql.getLast();
		if(p!=null){
			intent.putExtra("rate",p.getRate());
			intent.putExtra("blood",p.getBlood());
			intent.putExtra("signs",p.getSigns());
		}
		PendingIntent proximityIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);

		locationManager.addProximityAlert(latitude,longitude,200, (duration+120)*1000, proximityIntent);

		IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
		context.registerReceiver(new ProximityIntentReceiver(), filter);

		Intent intent2 = new Intent(context, MainActivity.class);
		intent2.putExtra("cancelAlert", true);
		intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pIntent = PendingIntent.getActivity(context, 0,
				intent2, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);

		// build notification
		// the addAction re-use the same intent to keep the example short
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Notification n = new Notification.Builder(context)
				.setContentTitle("Arrival alert")
				.setContentText("I will inform your family when you arrive to hospital.")
				.setStyle(new Notification.BigTextStyle()
								.bigText("I will inform your family when you arrive to hospital. To cancel this, Click Me!"))
				.setSmallIcon(R.drawable.location).setLargeIcon(bmp)
				.setSound(alarmSound)
				.setTicker("Arrival alert")
				.setContentIntent(pIntent).setAutoCancel(false).build();

		n.flags = Notification.FLAG_NO_CLEAR;
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0, n);
	}

	public static void CancelProximityAlert(final Context context) {
		// build dialog to show for enabling gps
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setMessage(
				"Arrival alert is enabled. Do you want to disable it?")
				.setCancelable(false)
				// Yes button
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								Intent intent = new Intent(PROX_ALERT_INTENT);
								PendingIntent pIntent = PendingIntent
										.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
								if(locationManager!=null && pIntent!=null)
									locationManager.removeProximityAlert(pIntent);
								NotificationManager notificationManager = (NotificationManager) context
										.getSystemService(Context.NOTIFICATION_SERVICE);
								notificationManager.cancelAll();
							}
						})
				// No Button
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});

		// show dialog
		final AlertDialog alert = builder.create();
		alert.show();
	}
	
	public static void removeProximityAlert(){
		Intent intent = new Intent(PROX_ALERT_INTENT);
		PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
		locationManager.removeProximityAlert(pIntent);
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}
}
