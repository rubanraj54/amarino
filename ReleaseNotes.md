# Amarino 0.53 #
  * fixed bug with sendDataToArduino functions in Amarino API (Amarino.java)
  * added background image which appears on the start screen when device list is empty
  * updated the AmarinoLibrary.jar file to match the latest version

# Amarino 0.52 #
  * added fall back to open directly bluetooth port 1 when SDP was not successful (i.e. on older phones)
  * updated AmarinoPluginBundle: receive sms event has been added and xml file refactoring (added styles to reduce code)
  * MeetAndroid library got a convenient getString() method to retrieve strings sent by Android (see **ConvertEvents** example shipped with the MeetAndroid library to get an idea of how to use it)

# Amarino 0.5 #
  * this version uses UUID and SDP lookup to find a free port for opening a socket to a Bluetooth module
  * modified underlying Bluetooth library (AndroidBluetoothLibrary.jar) to work with UUID
  * added a release notes section in about dialog
  * shows a user message instead of an empty box when add event is clicked but no plugins are installed

# Amarino 0.4 #
  * first public release