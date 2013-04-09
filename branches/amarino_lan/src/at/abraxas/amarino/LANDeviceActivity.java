package at.abraxas.amarino;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LANDeviceActivity extends Activity {
	
		private Button connect;
		private TextView tv;
		private Socket socket;
		private String serverIpAddress = "";
		
		protected static String ADDRESS_EXTRA = "device_address";
		protected static String TYPE_EXTRA = "device_lan";
		//warum verwenden wir nicht die AmarinoIntent klasse für diese Extras? 
		//bzw. die bereits vorhanden extras der klasse?


		@Override
		public void onCreate(Bundle savedInstanceState) {
			
			super.onCreate(savedInstanceState);
			setContentView(R.layout.lan_device);
			connect = (Button) findViewById(R.id.connect);
			tv = (TextView) findViewById(R.id.myTextView);

			connect.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					
						serverIpAddress = ((EditText) findViewById(R.id.IPText01)).getText().toString().replace("/", "");
						final String address = serverIpAddress;
						Intent i = new Intent();
						i.putExtra(ADDRESS_EXTRA, address);
						i.putExtra(TYPE_EXTRA, Device.LANDEVICE);

						//in den intent die ein architecture extra einfügen
						//wer fängt den intent??
						setResult(RESULT_OK, i);
						finish();
					
				}
			});
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			try {
				if(socket!=null)
				{
					socket.close();
				}
			} catch (IOException e) {
				tv.setText("socket close error");
				e.printStackTrace();
			}
		}
}


