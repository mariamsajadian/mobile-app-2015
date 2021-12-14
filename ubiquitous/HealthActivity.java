package com.sajadian.ubiquitous;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HealthActivity extends FragmentActivity {
	private Pollution pollution;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.health);
		pollution = MainActivity.getPollution();
		context = getApplicationContext();
	}

	public void saveData(View view) {
		if (pollution == null || pollution.getLocation()==null) {
			Toast.makeText(context, "Your location is not set!",
					Toast.LENGTH_LONG).show();
			return;
		}
		TextView hRate = (TextView) findViewById(R.id.heartrate);
		TextView blood1 = (TextView) findViewById(R.id.blood1);
		TextView blood2 = (TextView) findViewById(R.id.blood2);
		CheckBox vt1 = (CheckBox) findViewById(R.id.vital1);
		CheckBox vt2 = (CheckBox) findViewById(R.id.vital2);
		CheckBox vt3 = (CheckBox) findViewById(R.id.vital3);
		CheckBox vt4 = (CheckBox) findViewById(R.id.vital4);
		CheckBox sg1 = (CheckBox) findViewById(R.id.sign1);
		CheckBox sg2 = (CheckBox) findViewById(R.id.sign2);
		CheckBox sg3 = (CheckBox) findViewById(R.id.sign3);
		CheckBox sg4 = (CheckBox) findViewById(R.id.sign4);
		CheckBox sg5 = (CheckBox) findViewById(R.id.sign5);
		CheckBox sg6 = (CheckBox) findViewById(R.id.sign6);
		CheckBox sg7 = (CheckBox) findViewById(R.id.sign7);

		int rate, bl1, bl2;

		rate = hRate.getText().toString().equals("") ? 0 : Integer
				.parseInt(hRate.getText().toString());
		bl1 = blood1.getText().toString().equals("") ? 0 : Integer
				.parseInt(blood1.getText().toString());
		bl2 = blood2.getText().toString().equals("") ? 0 : Integer
				.parseInt(blood2.getText().toString());

		pollution.setRate(rate);
		pollution.setBlood1(bl1);
		pollution.setBlood2(bl2);
		pollution.setVital1(vt1.isChecked());
		pollution.setVital2(vt2.isChecked());
		pollution.setVital3(vt3.isChecked());
		pollution.setVital4(vt4.isChecked());
		pollution.setSign1(sg1.isChecked());
		pollution.setSign2(sg2.isChecked());
		pollution.setSign3(sg3.isChecked());
		pollution.setSign4(sg4.isChecked());
		pollution.setSign5(sg5.isChecked());
		pollution.setSign6(sg6.isChecked());
		pollution.setSign7(sg7.isChecked());
		
		SimpleDateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
		Date date = new Date();
		pollution.setDate(df.format(date));

		 MySQLiteHelper mysql = new MySQLiteHelper(context);
		 mysql.insert(pollution);

		Toast.makeText(context, "Successfully saved!", Toast.LENGTH_LONG)
				.show();

		if ((rate > 100 || rate < 65)
				|| (vt1.isChecked() || vt2.isChecked() || vt3.isChecked() || vt4
						.isChecked()) || (bl1 > 16 || bl1 < 9) || (bl2 > 8 || bl2 < 5)) {
			showDangerDialog();
		}else{
			HealthActivity.this.finish();
		}

	}

	public void cancelSave(View view) {
		this.finish();
	}

	public void measure(View view) {
		try {
			Intent ih;
			PackageManager manager = getPackageManager();
			ih = manager
					.getLaunchIntentForPackage("com.runtastic.android.heartrate.pro");
			ih.addCategory(Intent.CATEGORY_LAUNCHER);
			startActivity(ih);
		} catch (Exception e) {
		}
	}

	private void showDangerDialog() {
		final LatLng latlng = new LatLng(pollution.getLat(), pollution.getLng());
		final Dialog dialog=new Dialog(this);
		dialog.setContentView(R.layout.health_alert);
		dialog.setTitle("Medical Warning!");
		ListView ls=(ListView)dialog.findViewById(R.id.alertlistview);
		ls.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?>  parent, View view, int position,
                    long id) {
				switch (position) {
				case 0:
					Intent intent = new Intent("android.intent.action.DIAL");
					try {
						intent.setData(Uri.parse("tel:115"));
						startActivity(intent);
					} catch (Exception e) {
						Log.e("SampleApp", "Failed to invoke call", e);
					}
					startActivity(intent);
					break;
				case 1:
					MapActivity.dangerLocation = latlng;
					Intent i = new Intent();
					i.setClass(HealthActivity.this,MapActivity.class);
					startActivity(i);
					break;
				case 2:
					dialog.dismiss();
					HealthActivity.this.finish();
					break;
				}				
			}
		});
		dialog.show();
	}
}
