package com.sajadian.ubiquitous;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class HealthHistoryActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.saved_list);
		final MySQLiteHelper db = new MySQLiteHelper(
				this.getApplicationContext());

		final ArrayList<Pollution> list = db.getAll();
		final PollutionAdapter adapter = new PollutionAdapter(this, list);

		TextView tx=(TextView)findViewById(R.id.txtStatus);
		if(list.size()>0)
			tx.setVisibility(View.GONE);
		
		setListAdapter(adapter);
		this.getListView().setLongClickable(true);
		this.getListView().setOnItemLongClickListener(
				new OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, final long id) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(HealthHistoryActivity.this);
						dialog.setTitle("Attention!")
								.setMessage("Are you sure to delete?")
								.setCancelable(false)
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface arg0,int arg1) {
												db.delete(id);
												list.remove(position);
												adapter.notifyDataSetChanged();
											}
										})
								.setNegativeButton("No",new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface arg0,int arg1) {
												arg0.dismiss();
											}
										});
						AlertDialog alertDialog = dialog.create();
						alertDialog.show();
						return true;
					}
				});
	}
	
	public void showMap(View view){
		if(view.getTag().equals("")){
			Toast.makeText(this, "Location not available!", Toast.LENGTH_LONG).show();
			return;
		}
		String[] l=view.getTag().toString().split(",");
		double lat=Double.parseDouble(l[0]);
		double lng=Double.parseDouble(l[1]);
		
		MapActivity.dangerLocation = null;
		Intent intent = new Intent(HealthHistoryActivity.this, MapActivity.class);
		intent.putExtra("lat", lat);
		intent.putExtra("lng", lng);
		startActivity(intent);
	}
}
