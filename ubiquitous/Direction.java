package com.sajadian.ubiquitous;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Direction {

	private final GoogleMap map;
	private int mMode = 0;
	private Marker marker;
	private final FragmentActivity activity;
    private ProgressDialog progressDialog;

	public Direction(GoogleMap map, FragmentActivity activity) {
		this.map = map;
		this.activity=activity;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public void SearchHospital(String location, int mode) {
		RouteTask route = new RouteTask();
		if(route.getStatus()==AsyncTask.Status.PENDING)
			route.execute("http://aasare.ir/msrouting.php?loc=" + location + "&mode=" + mode);
	}

	public void getDirections(LatLng origin, LatLng dest, int mode) {
		mMode=mode;
		// Origin of route
		String parameters = "from=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		parameters += "&to=" + dest.latitude + "," + dest.longitude;

		// Building the url to the web service
		String url = "http://aasare.ir/msrouting.php?" + parameters;

		RouteTask route = new RouteTask();
		if(route.getStatus()==AsyncTask.Status.PENDING)
			route.execute(url);
	}

	// A method to download JSON data from url
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuilder sb = new StringBuilder();

			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			assert iStream != null;
			iStream.close();
			assert urlConnection != null;
			urlConnection.disconnect();
		}
		return data;
	}

	private class RouteTask extends AsyncTask<String, Void, String> {
		
		@Override
		public void onPreExecute()
	    {
	        progressDialog = new ProgressDialog(activity);
	        progressDialog.setMessage("Calculating directions...");
	        progressDialog.show();
	    }
		
		@Override
		protected String doInBackground(String... url) {
			// For storing data from web service
			String data = "";
			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}
		
		@Override
		protected void onPostExecute(String json){
			try {
				JSONArray jsonArray = new JSONArray(json);
				BitmapDescriptor hospitalIcon = BitmapDescriptorFactory
						.fromResource(R.drawable.hospital);
				PolylineOptions lineOptions = new PolylineOptions();

				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject h = jsonArray.getJSONObject(i);
					if(h.has("name")){
						MarkerOptions markerOptions = new MarkerOptions();
						markerOptions.title("\u200e" + h.getString("name"));
						markerOptions.icon(hospitalIcon);
						LatLng ps = new LatLng(h.getDouble("lat"), h.getDouble("lng"));
						markerOptions.position(ps);
						if(h.has("points"))
							marker=map.addMarker(markerOptions);
						else
							map.addMarker(markerOptions);
					}
					
					if (h.has("points")) {
						JSONArray points = h.getJSONArray("points");
						for (int j = 0; j < points.length(); j++) {
							JSONObject point = points.getJSONObject(j);
							LatLng lp = new LatLng(point.getDouble("lat"), point.getDouble("lng"));
							lineOptions.add(lp);
						}
						lineOptions.width(5);
						if (mMode == 0)
							lineOptions.color(Color.RED);
						else if (mMode == 1)
							lineOptions.color(Color.GREEN);
						else if (mMode == 2)
							lineOptions.color(Color.BLUE);
						else{
							Random rand = new Random();
							int r = rand.nextInt(255);
							int g = rand.nextInt(255);
							int b = rand.nextInt(255);
							lineOptions.color(Color.rgb(r, g, b));
						}
						map.addPolyline(lineOptions);
						
						int duration = h.getInt("duration");
						int distance = h.getInt("distance");
						String snp = formatDuration(duration) + " "	+ formatDistance(distance);
						marker.setSnippet(snp);
						marker.showInfoWindow();
						LatLngBounds.Builder bc = new LatLngBounds.Builder();
						LatLng p1=new LatLng(points.getJSONObject(0).getDouble("lat"), points.getJSONObject(0).getDouble("lng"));
						bc.include(p1);
						LatLng p2=new LatLng(points.getJSONObject(points.length()-1).getDouble("lat"), points.getJSONObject(points.length()-1).getDouble("lng"));
						bc.include(p2);
						MapActivity.markerPoints.clear();
						MapActivity.markerPoints.add(p1);
						MapActivity.markerPoints.add(p2);
						PollutionService.AddProximityAlert(p2.latitude, p2.longitude, h.getString("name"), h.getInt("duration"));
						map.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 100));
					}
				}
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
			}
			progressDialog.dismiss();
		}
		
		private String formatDuration(int duration) {
			if (duration < 60) {
				return "Duration: " + duration + " sec";
			}
			int d=duration/60;
			if(duration % 60>30)
				d+=1;
			return "Duration: " +  d + " min";
		}

		private String formatDistance(int distance) {
			if (distance < 1000) {
				return  "Distance: " +  distance + " m";
			}
			double d=distance;
		 	d /= 1000;
		 	DecimalFormat df=new DecimalFormat("#,###,###,##0.0" );
			return "Distance: " +  df.format(d) + " km";
		}
	}
}