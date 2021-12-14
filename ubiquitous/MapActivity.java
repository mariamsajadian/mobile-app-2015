package com.sajadian.ubiquitous;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity {

	private GoogleMap map;
	private RadioButton rbDriving;
	private RadioButton rbWalking;
	private CheckBox pointsearch;
	private RadioGroup rgModes;
	public  static ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();
	private int directionMode = 0;
	private Context context;
	private PollutionService pollutionService;
	private Pollution pollution;
	public static LatLng dangerLocation;
	public static Direction direction;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		context = getApplicationContext();
		// Getting reference to SupportMapFragment of the activity_main
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mymap);

		// Getting Map for the SupportMapFragment
		map = fm.getMap();
		direction=new Direction(map,this);
		
		double lat=0,lng=0;
		if (savedInstanceState == null) {
		    Bundle extras = getIntent().getExtras();
		    if(extras != null) {
		        lat = extras.getDouble("lat");
		        lng= extras.getDouble("lng");
		    }
		} else {
		    lat= savedInstanceState.getDouble("lat");
		    lng=savedInstanceState.getDouble("lng");
		}
		
		if(lat>0 && lng>0){
			LatLng loc=new LatLng(lat, lng);
			map.addMarker(new MarkerOptions().position(loc));
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
		}else{
			checkState(null);
		}
		
		rbDriving = (RadioButton) findViewById(R.id.rb_driving);
		rbWalking = (RadioButton) findViewById(R.id.rb_walking);
		rgModes = (RadioGroup) findViewById(R.id.rg_modes);
		pointsearch=(CheckBox)findViewById(R.id.pointsearch);
		
		rgModes.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// Checks, whether start and end locations are captured
				if (markerPoints.size() >= 2) {
					LatLng origin = markerPoints.get(0);
					LatLng dest = markerPoints.get(1);
					if (rbDriving.isChecked()) {
						directionMode = 0;
					} else if (rbWalking.isChecked()) {
						directionMode = 2;
					}
					direction.getDirections(origin, dest, directionMode);
				}
			}
		});

		// Enable MyLocation Button in the Map
		map.setMyLocationEnabled(true);

        // Setting onclick event listener for the map
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
            	if(!pointsearch.isChecked())
            		return;
				markerPoints.clear();
				map.clear();

				// Adding new item to the ArrayList
				markerPoints.add(point);
				checkState(point);
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if(markerPoints.size()==2){
					if(markerPoints.get(1)==marker.getPosition()){
						return false;
					}
				}
				markerPoints.set(1, marker.getPosition());
				direction.setMarker(marker);
				direction.getDirections(markerPoints.get(0), markerPoints.get(1), 4);
				return false;
			}
		});
	}

	
	@Override
	protected void onRestart() {
		super.onRestart();
		checkState(null);
	}

	private void checkState(LatLng point){
		if(dangerLocation!=null){
			dangerState();
			return;
		}
		pollutionService = new PollutionService(context);
		if (point != null) {
			pollution = pollutionService.GetPollution(map, point);
		} else if (pollution == null)
			pollution = pollutionService.GetPollution(map);
	}

	private void dangerState(){
		MarkerOptions marker = new MarkerOptions()
		.position(dangerLocation)
		.title("Unhealthy State")
		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		
		// adding marker
		map.addMarker(marker);
		
		String loc = Double.toString(dangerLocation.latitude) + ","
				+ Double.toString(dangerLocation.longitude);
		
		direction.SearchHospital(loc, directionMode);
	}

}
