package edu.mit.media.hlt.workshop.workout;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Preferences extends PreferenceActivity implements OnPreferenceChangeListener{
	
	private static final String KEY_THRESHOLD = "threshold";
	
	Preference thresholdPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		thresholdPref = findPreference(KEY_THRESHOLD);
		thresholdPref.setOnPreferenceChangeListener(this);
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		thresholdPref.setSummary(this.getString(R.string.pref_threshold_summary, getThreshold(this)));
	}
	
	
	public static int getThreshold(Context context){
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
				.getString(KEY_THRESHOLD, context.getString(R.string.pref_threshold_default)).trim());
	}
	
	public static void setThreshold(Context context, int value){
		PreferenceManager.getDefaultSharedPreferences(context)
		.edit()
		.putString(KEY_THRESHOLD, String.valueOf(value))
		.commit();
	}



	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		if (KEY_THRESHOLD.equals(key)){
			thresholdPref.setSummary(this.getString(R.string.pref_threshold_summary, (String) newValue));
		}
		return true;
	}


}
