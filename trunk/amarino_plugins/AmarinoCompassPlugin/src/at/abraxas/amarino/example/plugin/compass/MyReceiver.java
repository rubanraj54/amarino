package at.abraxas.amarino.example.plugin.compass;

import at.abraxas.amarino.plugin.PluginReceiver;

public class MyReceiver extends PluginReceiver {

	public MyReceiver() {
		// TODO change name of your background service class if necessary
		serviceClass = MyBackgroundService.class;
	}

}
