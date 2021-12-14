package com.sajadian.ubiquitous;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class PrefsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	Preference p;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
		String s = sp.getString("private_number", "");
		
		p=findPreference("private_number");
		p.setSummary(s);
		p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				pickContact();
				return false;
			}
		});
	}
	    
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference pref = findPreference(key);
		if (pref instanceof EditTextPreference) {
			EditTextPreference etp = (EditTextPreference) pref;
			pref.setSummary(etp.getText());
		}
	}
	
	static final int PICK_CONTACT_REQUEST = 1;  // The request code

	private void pickContact() {
	    Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
	    pickContactIntent.setType(Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
	    startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    // Check which request we're responding to
	    if (requestCode == PICK_CONTACT_REQUEST) {
	        // Make sure the request was successful
	        if (resultCode ==Activity.RESULT_OK) {
	        	// Get the URI that points to the selected contact
	            Uri contactUri = data.getData();
	            // We only need the NUMBER column, because there will be only one row in the result
	            String[] projection = {Phone.NUMBER};

	            // Perform the query on the contact to get the NUMBER column
	            Cursor cursor = getActivity().getContentResolver()
	                    .query(contactUri, projection, null, null, null);
	            cursor.moveToFirst();

	            // Retrieve the phone number from the NUMBER column
	            int column = cursor.getColumnIndex(Phone.NUMBER);
	            String number = cursor.getString(column);
	            p.setSummary(number.toString());
	            
	            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
	            sp.edit().putString("private_number", number.toString()).commit();
	        }
	    }
	}
}
