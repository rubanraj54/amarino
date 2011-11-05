package at.abraxas.amarino.example.plugin.compass;

import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewStub;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MyEditActivity extends AbstractEditActivity {
	
	public static final String PREF_FREQUENCY = "frequency";
	
	private static final String TAG = "MyEditActivity";
	
	TextView frequencyTV;
	SeekBar frequencySB;

	
	@Override
	public void init() {
		ViewStub stub = (ViewStub)findViewById(R.id.stub);
		stub.inflate();
		
		frequencySB = (SeekBar)findViewById(R.id.seekBar);
        frequencyTV = (TextView)findViewById(R.id.seekBar_value);
        
        int lastValue = PreferenceManager.getDefaultSharedPreferences(this).getInt(PREF_FREQUENCY, 50);
        frequencySB.setProgress(lastValue);
        int rate = MyEditActivity.getRate(lastValue);
		frequencyTV.setText(getRateText(rate));
        
        frequencySB.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				int rate = MyEditActivity.getRate(progress);
				frequencyTV.setText(getRateText(rate));
			}
		});
	}
	
	@Override
	public void onSaveBtnClick(View view) {
		sendResult(R.string.plugin_name, R.string.plugin_desc, R.string.service_class_name, 0, 359);
	}


	@Override
	public void onCancelBtnClick(View view) {
		// nothing to do here in this example
	}
	



	private String getRateText(int rate){
		String text = new String();
		
		switch(rate){
		case 8: text = getString(R.string.very_slow); break;
		case 4: text = getString(R.string.slow); break;
		case 2: text = getString(R.string.medium); break;
		case 1: text = getString(R.string.fast); break;
		case 0: text = getString(R.string.very_fast); break;
		}
		return text;
	}
	
	protected static int getRate(int frequency) {
		int rate = 0;
		if (frequency < 20) 		rate = 8;
		else if (frequency < 40) 	rate = 4;
		else if (frequency < 60) 	rate = 2;
		else if (frequency < 80) 	rate = 1;
		else 						rate = 0;
		return rate;
	}

  
}
