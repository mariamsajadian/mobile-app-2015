package com.sajadian.ubiquitous;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static Pollution pollution;
	private PollutionService pollutionService;
	private static TextView pollutionText;
	private static MainActivity instance;
	private static ProgressBar progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		StrictMode.enableDefaults();
		
		boolean cancelAlert=false;
		instance = this;
		if (savedInstanceState == null) {
		    Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		    	cancelAlert = extras.getBoolean("cancelAlert");
		    }
		} else {
			cancelAlert= savedInstanceState.getBoolean("cancelAlert");
		}
		
		if(cancelAlert){
			PollutionService.CancelProximityAlert(instance);
			return;
		}
		
		pollutionService = new PollutionService(instance);
		pollutionText = (TextView) findViewById(R.id.pollutionText);
		progress = (ProgressBar) findViewById(R.id.mainProgress);

		if (pollution == null || pollution.getLocation()==null) {
			progress.setVisibility(View.VISIBLE);
			pollution = pollutionService.GetPollution();
		} else {
			UpdateLocation(pollution, false);
		}
	}

	private static void UpdateLocation(Pollution p, boolean showMsg) {
		pollution = p;
		int pv =p==null?0: pollution.getPollution();
		int c = Color.WHITE;
		String t = "Unknown";

		if (pv > 0 && pv <= 50) {
			c = Color.GREEN;
			t = "Good";
		} else if (pv > 50 && pv <= 100) {
			c = Color.YELLOW;
			t = "Moderate";
		} else if (pv > 100 && pv <= 150) {
			c = Color.rgb(255, 50, 10);
			t = "Unhealthy";
		} else if (pv > 150) {
			c = Color.RED;
			t = "Hazardous";
		}
		pollutionText.setTextColor(c);
		pollutionText.setText("State: " + t);
		progress.setVisibility(View.INVISIBLE);
		if (pollution.getPollution() > 100 && showMsg)
			showHealthParameters();
	}

	public static void UpdateLocation(Pollution p) {
		UpdateLocation(p, true);
	}

	private static void showHealthParameters() {
		AlertDialog.Builder alert = new AlertDialog.Builder(instance);

		alert.setTitle("Health Warning!");

		alert.setMessage(
				"Air pollution is high here! Do you want to check your health parameters?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									Intent i = new Intent();
									i.setClass(instance, HealthActivity.class);
									instance.startActivity(i);
								} catch (Exception e) {
								}
							}

						});
		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		// create alert dialog
		AlertDialog alertDialog = alert.create();

		// show it
		alertDialog.show();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	    Bundle extras = intent.getExtras();
	    if(extras != null) {
	    	boolean cancelAlert = extras.getBoolean("cancelAlert");
	    	if(cancelAlert){
				PollutionService.CancelProximityAlert(instance);
			}
	    }
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (pollution == null || pollution.getLocation()==null) {
			progress.setVisibility(View.VISIBLE);
			pollution = pollutionService.GetPollution();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mymenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_showlist:
			Intent intent = new Intent(MainActivity.this,
					HealthHistoryActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_setting:
			Intent i = new Intent();
			i.setClass(MainActivity.this, SetPreferenceActivity.class);
			startActivity(i);
			return true;
		case R.id.menu_help:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void ecall(View view) {
		Intent intent = new Intent("android.intent.action.DIAL");
		try {
			intent.setData(Uri.parse("tel:115"));
			startActivity(intent);
		} catch (Exception e) {
			Log.e("SampleApp", "Failed to invoke call", e);
		}
		startActivity(intent);
	}

	public void sendSMS(View view) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(instance);
		final String private_number = settings.getString("private_number", "");
		String loc = pollution.getLocation();
		if (loc == null) {
			Toast.makeText(getApplicationContext(), "Waiting for location...", Toast.LENGTH_LONG).show();
			return;
		}
		if (private_number != "") {
			final String msg = "My health state isn't noraml, it's my location:\r\nhttp://maps.google.com/?q=" + loc;
			final AlertDialog.Builder builder = new AlertDialog.Builder(instance);

			builder.setMessage("Are you sure to send SMS?")
					.setCancelable(false)
					// Yes button
					.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
									try {
										SmsManager smsManager = SmsManager.getDefault();
										smsManager.sendTextMessage(private_number, null, msg, null, null);
										Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
									} catch (Exception e) {
										Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
										e.printStackTrace();
									}
								}
							})
					// No Button
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
								}
							});

			// show dialog
			final AlertDialog alert = builder.create();
			alert.show();
		} else {
			final AlertDialog.Builder builder = new AlertDialog.Builder(instance);

			builder.setMessage(
					"You don't set your private number. Do you want to set it?")
					.setCancelable(false)
					// Yes button
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0, int arg1) {
									Intent i = new Intent();
									i.setClass(MainActivity.this, SetPreferenceActivity.class);
									startActivity(i);
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

	}

	public void healthParameters(View view) {
		Intent inte = new Intent(MainActivity.this, HealthActivity.class);
		startActivity(inte);
	}

	public void showMap(View view) {
		MapActivity.dangerLocation = null;
		Intent inte = new Intent(MainActivity.this, MapActivity.class);
		startActivity(inte);
	}

	public static Pollution getPollution() {
		return pollution;
	}
}