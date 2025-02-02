/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII            General Purpose Amusement Park App             SSSSSSSSS        **/
/**           III                                                               SSS               **/
/**           III                    Original Creator: John Fertita            SSS                **/
/**           III                    Edits By:         Jeremy Poulin,           SSS               **/
/**           III                                      Michael Stowell           SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Special Thanks:   Don Rhine                         SSS      **/
/**           III                    Group:            ECG, iSENSE                      SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/
 
  
package edu.uml.cs.isense.amusement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.comm.RestAPI;

public class AmusementPark extends Activity implements SensorEventListener, LocationListener {

	private static EditText experimentInput;
	private static EditText seats;
	private static Spinner rides;
	private static TextView rideName;
	private static CheckBox canobieCheck;
	
	private Button startStop;
	private Button browseButton;
	private TextView values;
	private Boolean running = false;
	private Vibrator vibrator;
	private TextView picCount;
	private TextView loginInfo;	
	private String rideNameString = "NOT SET";
	private String seatString = "1";
	private SensorManager mSensorManager;
	private LocationManager mLocationManager;
	private LocationManager mRoughLocManager;
	private Location loc;
	private Location roughLoc;
	private Timer timeTimer;
	
	private float rawAccel[];
	private float rawMag[];
	private float accel[];
	private float orientation[];
	
	private static final int INTERVAL = 200;
	private static final int MENU_ITEM_SETUP = 1;
	private static final int MENU_ITEM_LOGIN = 2;
	private static final int MENU_ITEM_UPLOAD = 3;
	private static final int SAVE_DATA = 4;
	private static final int DIALOG_SUMMARY = 5;
	private static final int DIALOG_CHOICE = 6;
	private static final int EXPERIMENT_CODE = 7;
	private static final int DIALOG_NO_ISENSE = 8;
	private static final int RECORDING_STOPPED = 9;
	private static final int DIALOG_NO_GPS = 10;
	private static final int DIALOG_FORCE_STOP = 11;

    public static final int DIALOG_CANCELED = 0;
    public static final int DIALOG_OK = 1;
    public static final int DIALOG_PICTURE = 2;
    
    public static final int CAMERA_PIC_REQUESTED = 1;
    public static final int CAMERA_VID_REQUESTED = 2;
    	
    private int count = 0;
    private String data;
    
    private Uri imageUri; 
    private Uri videoUri;
    
    private MediaPlayer mMediaPlayer;
    
    private ArrayList<File> pictures;
    private ArrayList<File> videos;
    
    private static int    rideIndex      =  0 ;
    private static String studentNumber  = "1";
 
    private int    elapsedMinutes = 0;
    private int    elapsedSeconds = 0;
    private int    elapsedMillis  = 0;
    private int    totalMillis    = 0;
    private int    dataPointCount = 0;
    private int    i              = 0;  
    private int    len            = 0;  
    private int    len2           = 0;

    private long   currentTime    = 0;
    
    private String dateString, s_elapsedSeconds, s_elapsedMillis, s_elapsedMinutes;;
    RestAPI rapi ;
    
    DecimalFormat toThou = new DecimalFormat("#,###,##0.000");

    ProgressDialog dia;
    double partialProg = 1.0;
    

    
    private EditText sessionName; 
    String nameOfSession = "";
    String partialSessionName = "";
    
    public static boolean inPausedState     = false;
    
	private static int     mwidth            = 1;
    private static int     mediaCount        = 0;
    private static boolean useMenu           = true ;
    private static boolean beginWrite        = true ;
    private static boolean setupDone         = false;
    private static boolean choiceViaMenu     = false;
    private static boolean dontToastMeTwice  = false;
    private static boolean exitAppViaBack    = false;
    private static boolean canobieIsChecked  = true;
    private static boolean canobieBackup     = true;
    private static boolean successLogin      = false;
    
    private Handler mHandler;
    private boolean throughHandler = false;
    
    File SDFile;
    FileWriter gpxwriter;
    BufferedWriter out;
    
    public static String textToSession = "";
    public static String toSendOut = "";
    public static String loginName = "";
    public static String loginPass = "";
    public static String experimentId = "";
    public static JSONArray dataSet;
	
	public static Context mContext;
	
	private static ArrayList<File> pictureArray = new ArrayList<File>();
	private static ArrayList<File> videoArray   = new ArrayList<File>();

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mContext = this;
        
        //initialize everything you're going to need
        initVars();
               
        // Display the End User Agreement
        displayEula();
        
        //This block useful for if onBackPressed - retains some things from previous session
        if(running)
    		showDialog(DIALOG_FORCE_STOP);
        
        startStop.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
        startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				
				if(!setupDone || rideName.getText().toString() == null) { 
					
					showDialog(MENU_ITEM_SETUP); 
					Toast.makeText(AmusementPark.this, "You must setup before recording data.", Toast.LENGTH_LONG).show(); 
				
				} else {
					
					vibrator.vibrate(300);
					mMediaPlayer.setLooping(false);  
					mMediaPlayer.start();
					
					if (running) {
						
						writeToSDCard(null, 'f');
						setupDone = false;
						useMenu = true;
					
						mSensorManager.unregisterListener(AmusementPark.this);
						running = false;
						startStop.setText(R.string.startString);
						 
						timeTimer.cancel();
						count++;
						startStop.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
						choiceViaMenu = false;
						if(throughHandler)
							showDialog(RECORDING_STOPPED);
						else
							showDialog(DIALOG_CHOICE);
						
					} else {
						
						dataSet = new JSONArray();
						elapsedMillis = 0; totalMillis    = 0;
						len = 0; len2 = 0; dataPointCount = 0;
						i   = 0;
						beginWrite = true;
						currentTime = getUploadTime();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Toast.makeText( getBaseContext() , "Data recording interrupted! Time values may be inconsistent." , Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}

						useMenu = false;
						
						if (mSensorManager != null) {
							mSensorManager.registerListener(AmusementPark.this, 
									mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),  
									SensorManager.SENSOR_DELAY_FASTEST);
							mSensorManager.registerListener(AmusementPark.this, 
									mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 
									SensorManager.SENSOR_DELAY_FASTEST);
						}
						
						data = "X Acceleration, Y Acceleration, Z Acceleration, Acceleration, " +
								"Latitude, Longitude, Heading, Magnetic X, Magnetic Y, Magnetic Z, Time\n";
						running = true;
						startStop.setText(R.string.stopString);
			    	
						timeTimer = new Timer();
						timeTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {
							
								dataPointCount++;
								count = (count + 1) % 2;
								elapsedMillis += INTERVAL;
								totalMillis = elapsedMillis;
			
								if(i >= 3000) {
								
									timeTimer.cancel();
									
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											throughHandler = true;
											startStop.performLongClick();
										}
									});
								
								} else {
								
									i++; len++; len2++;	
									
									data =  toThou.format(accel[0]) + ", " + 
											toThou.format(accel[1]) + ", " + 
											toThou.format(accel[2]) + ", " +
											toThou.format(accel[3]) + ", " + 
											loc.getLatitude() + ", " + 
											loc.getLongitude() + ", " + 
											toThou.format(orientation[0]) + ", " + 
											rawMag[0] + ", " + 
											rawMag[1] + ", " + 
											rawMag[2] + ", " + 
											currentTime + elapsedMillis + "\n";
									
									JSONArray dataJSON = new JSONArray();
									
									try {
										
										/* Accel-x    */ dataJSON.put(toThou.format(accel[0]));
										/* Accel-y    */ dataJSON.put(toThou.format(accel[1]));
										/* Accel-z    */ dataJSON.put(toThou.format(accel[2]));
										/* Accel-Total*/ dataJSON.put(toThou.format(accel[3]));
										/* Latitude   */ dataJSON.put(loc.getLatitude());
										/* Longitude  */ dataJSON.put(loc.getLongitude());
										/* Heading    */ dataJSON.put(toThou.format(orientation[0]));
										/* Magnetic-x */ dataJSON.put(rawMag[0]);
										/* Magnetic-y */ dataJSON.put(rawMag[1]);
										/* Magnetic-z */ dataJSON.put(rawMag[2]);
										/* Time       */ dataJSON.put(currentTime + elapsedMillis); 
										                 
										dataSet.put(dataJSON);
									
									} catch (JSONException e) {
										e.printStackTrace();
									}
									
								
									if(beginWrite) {
										writeToSDCard(data, 's');
									} else {
										writeToSDCard(data, 'u');
									}
									
						
								}
							
							}
						}, 0, INTERVAL);
						startStop.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
					}	
					return running;
				
				} running = false; return running;
			} 
        	
        });
                      
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && mRoughLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        	mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(c, true), 0, 0, AmusementPark.this);
        	mRoughLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, AmusementPark.this);
        } else {
        	showDialog(DIALOG_NO_GPS);
        }
        
        accel       = new float[4];
        orientation = new float[3];
        rawAccel    = new float[3];
        rawMag      = new float[3];
        loc         = new Location(mLocationManager.getBestProvider(c, true));
        
        mMediaPlayer = MediaPlayer.create(this, R.raw.beep); 
        
    }
    
    // (s)tarts, (u)pdates, and (f)inishes writing the .csv to the SD Card containing "data"
    public void writeToSDCard(String data, char code) {
    	switch (code) {
    	case 's':
    		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
        	Date dt = new Date();
        	
        	dateString = sdf.format(dt);

        	File folder = new File(Environment.getExternalStorageDirectory() + "/iSENSE");
        	
        	if(!folder.exists()) {
        	    folder.mkdir();
        	}     
        	
        	SDFile = new File(folder,  rides.getSelectedItem() + "-" + seats.getText().toString() + "-" + dateString + ".csv");
     	
        	try {
                gpxwriter = new FileWriter(SDFile);
                out = new BufferedWriter(gpxwriter);
                out.write(data);
                beginWrite = false;
        	} catch (IOException e) {
        		Toast.makeText(this, "Error creating the SD Card file.", Toast.LENGTH_LONG).show();
        	}
    		
    		break;
    		
    	case 'u':
    		try {
                out.append(data);
        	} catch (IOException e) {
        		Toast.makeText(this, "Error updating the SD Card file.", Toast.LENGTH_LONG).show();
        	}
    		
    		break;
    		
    	case 'f':
    		try {
        		if(out != null)
        			out.close();
                if(gpxwriter != null)
                	gpxwriter.close();
        	} catch (IOException e) {
        		Toast.makeText(this, "Error completing the SD Card file.", Toast.LENGTH_LONG).show();
        	}
    		
    		break;
    		
    	default:
    		Toast.makeText(mContext, "Fatal error writing file to SD Card!", Toast.LENGTH_LONG).show();
    		break;
    	}
    }
    
    // Adds pictures to the SD Card
    public void pushPicture() {
    	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
    	Date dt = new Date();
    	
    	dateString = sdf.format(dt);
    	
    	if(rideNameString.equals("NOT SET"))
    		rideNameString = "Unspecified Ride";

    	File folder = new File(Environment.getExternalStorageDirectory() + "/iSENSE");
    	
    	if(!folder.exists()) {
    	    folder.mkdir();
    	}
    	
    	for(int i = 0; i < pictures.size(); i++) {
    		File f = pictures.get(i);
    		File newFile = new File(folder, rideNameString + "-" + seatString + "-" + dateString + "-" + (i+1) + ".jpeg");
    		f.renameTo(newFile);
    		pictureArray.add(newFile);
    	}
    	
    	pictures.clear();
    }
    
    // Adds videos to the SD Card
    public void pushVideo() {
    	SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss");
    	Date dt = new Date();
    	
    	dateString = sdf.format(dt);

    	File folder = new File(Environment.getExternalStorageDirectory() + "/iSENSE");
    	
    	if(!folder.exists()) {
    	    folder.mkdir();
    	}     
    	
    	
    	for(int i = 0; i < videos.size(); i++) {
    		File f = videos.get(i);
    		File newFile = new File(folder, rideNameString + "-" + seatString + "-" + dateString + "-" + (i+1) + ".3gp");
    		f.renameTo(newFile);
    		videoArray.add(newFile);
    	}
    	
    	videos.clear();
    }

	@Override
    public void onPause() {
    	super.onPause();
    	mLocationManager.removeUpdates(AmusementPark.this);
    	mRoughLocManager.removeUpdates(AmusementPark.this);
    	mSensorManager.unregisterListener(AmusementPark.this);
    	if (timeTimer != null) timeTimer.cancel();
    	inPausedState = true;
    	if(pictures.size() > 0)
  			pushPicture();
  		if(videos.size() > 0)
  			pushVideo();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	mLocationManager.removeUpdates(AmusementPark.this);
    	mRoughLocManager.removeUpdates(AmusementPark.this);
    	mSensorManager.unregisterListener(AmusementPark.this);
    	if (timeTimer != null) timeTimer.cancel();
    	inPausedState = true;
    	if(pictures.size() > 0)
  			pushPicture();
  		if(videos.size() > 0)
  			pushVideo();
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	inPausedState = false;	
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	inPausedState = false;
    	if(running)
    		showDialog(DIALOG_FORCE_STOP);
    	
    	//will call the login dialogue if necessary and update UI
    	if(loginName != "") login();
    	
    	picCount.setText(getString(R.string.picAndVidCount) + mediaCount);
    }
    
    // Overridden to prevent user from exiting app unless back button is pressed twice
    @Override
    public void onBackPressed() {
    	if(!dontToastMeTwice) {
    		if(running) 
    			Toast.makeText(this, "Cannot exit via BACK while recording data; use HOME instead.",
    					Toast.LENGTH_LONG).show();
    		else
    			Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show();
    		new NoToastTwiceTask().execute();
    	} else if(exitAppViaBack && !running) {
    		setupDone = false;
    		super.onBackPressed();	
    	}
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ITEM_SETUP,  Menu.NONE, "Setup" );
		menu.add(Menu.NONE, MENU_ITEM_LOGIN,  Menu.NONE, "Login" );
		menu.add(Menu.NONE, MENU_ITEM_UPLOAD, Menu.NONE, "Upload");
		return true;
	}
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (!useMenu) {
            menu.getItem(MENU_ITEM_SETUP  - 1).setEnabled(false);
            menu.getItem(MENU_ITEM_LOGIN  - 1).setEnabled(false);
            menu.getItem(MENU_ITEM_UPLOAD - 1).setEnabled(false);
        } else {
        	menu.getItem(MENU_ITEM_SETUP  - 1).setEnabled(true );
            menu.getItem(MENU_ITEM_LOGIN  - 1).setEnabled(true );
            menu.getItem(MENU_ITEM_UPLOAD - 1).setEnabled(true );
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case MENU_ITEM_SETUP:
        		showDialog(MENU_ITEM_SETUP);
        		return true;
        	case MENU_ITEM_LOGIN:
        		showDialog(MENU_ITEM_LOGIN);
        		return true;
        	case MENU_ITEM_UPLOAD:
        		choiceViaMenu = true;
        		showDialog(DIALOG_CHOICE);
        		return true;
        }
        return false;
    }
    
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {	
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		DecimalFormat oneDigit = new DecimalFormat("#,##0.0");

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			rawAccel = event.values.clone();
			accel[0] = event.values[0];
			accel[1] = event.values[1];
			accel[2] = event.values[2];
			
			String xPrepend = accel[0] > 0 ? "+" : "";
			String yPrepend = accel[1] > 0 ? "+" : "";
			String zPrepend = accel[2] > 0 ? "+" : "";

			if (count == 0) {
				values.setText("X: " + xPrepend + oneDigit.format(accel[0]) + ", Y: " + 
						yPrepend + oneDigit.format(accel[1]) + ", Z: " + 
						zPrepend + oneDigit.format(accel[2]));
			}
			
			accel[3] = (float) Math.sqrt(Math.pow(accel[0], 2) + Math.pow(accel[1], 2) + Math.pow(accel[2], 2));

		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			rawMag = event.values.clone();
			
			float rotation[] = new float[9];
			
			if (SensorManager.getRotationMatrix(rotation, null, rawAccel, rawMag)) {
				orientation = new float[3];
				SensorManager.getOrientation(rotation, orientation);
			}
			
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
		roughLoc = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
    
	protected Dialog onCreateDialog(final int id) {
	    
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	Dialog dialog;// = builder.setView(new View(this)).create();
    	
    	//dialog.show();
    	
    	WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    	
	    switch(id) {
	    case MENU_ITEM_SETUP:
	    	
	    	dialog = getSavePrompt(new Handler() {
				public void handleMessage(Message msg) { 
					switch (msg.what) {
			    		case DIALOG_OK:
			    	  		partialSessionName = sessionName.getText().toString();
			    	  		setupDone = true;
			    	  		canobieBackup = canobieIsChecked;
			    	  		if(pictures.size() > 0)
			    	  			pushPicture();
			    	  		if(videos.size() > 0)
			    	  			pushVideo();
			    			break;
			    	  	case DIALOG_CANCELED:
			    	  		canobieIsChecked = canobieBackup;
			    	  		if(pictures.size() > 0)
			    	  			pushPicture();
			    	  		if(videos.size() > 0)
			    	  			pushVideo();
			    	  		break;
			    	  }
			          rideName.setText("Ride Name: " + rideNameString);

			      } 
			}, "Configure Options");
	    	dialog.setCancelable(true);
	    	dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
	    		@Override
	    		public void onCancel(DialogInterface dialog) {
	    			if(pictures.size() > 0)
	    	  			pushPicture();
	    	  		if(videos.size() > 0)
	    	  			pushVideo();
	    		}
	    	});
	    	
	    	sessionName.setText(partialSessionName);
	        break;
	        
	    case MENU_ITEM_LOGIN:
	    	LoginActivity la = new LoginActivity(this);
	        dialog = la.getDialog(new Handler() {
	        	  public void handleMessage(Message msg) { 
			    	  switch (msg.what) {
			    	  	case LoginActivity.LOGIN_SUCCESSFULL:
			    	  	  loginInfo.setText(" " + loginName);
			    	  	  loginInfo.setTextColor(Color.GREEN);
			    	  	  successLogin = true;
			    	  	  Toast.makeText(AmusementPark.this, "Login successful", Toast.LENGTH_LONG).show();
			    	  	  break;
			    	  	case LoginActivity.LOGIN_CANCELED:
			    		  break;
			    	  	case LoginActivity.LOGIN_FAILED:
				    	  successLogin = false;
			    	  	  showDialog(MENU_ITEM_LOGIN);
			    		  break;
			    	  }
			      }
        	});
	        break; 
	    	
	    case SAVE_DATA:
	    	
	    	dialog = getSavePrompt(new Handler() {
				public void handleMessage(Message msg) { 
					switch (msg.what) {
			    		case DIALOG_OK:
			    			if(len == 0 || len2 == 0)
			    				Toast.makeText(AmusementPark.this, "There is no data to upload!", Toast.LENGTH_LONG).show();
			    			showDialog(DIALOG_CHOICE);
			    			partialSessionName = sessionName.getText().toString();
			    	  		break;
			    		case DIALOG_CANCELED:
			    	  		break;
					}
					rideName.setText("Ride Name: " + rideNameString);

				}
	    	}, "Final Step");
	    	sessionName.setText(partialSessionName);
			break;
	    case DIALOG_SUMMARY:
	    	
	    	mediaCount = 0;
	    	elapsedMillis   = totalMillis          ;
	    	elapsedSeconds  = elapsedMillis / 1000 ;
	    	elapsedMillis  %= 1000                 ;
	    	elapsedMinutes  = elapsedSeconds / 60  ;
	    	elapsedSeconds %= 60                   ;
	    	
	    	if( elapsedSeconds < 10 ) { s_elapsedSeconds = "0" + elapsedSeconds;    }
	    	else { s_elapsedSeconds = "" + elapsedSeconds;                          }
	    	
	    	if( elapsedMillis < 10 )  { s_elapsedMillis  = "00" + elapsedMillis;    }
	    	else if( elapsedMillis < 100 ) { s_elapsedMillis = "0" + elapsedMillis; }
	    	else { s_elapsedMillis  = "" + elapsedMillis;                           }
	    	
	    	if( elapsedMinutes < 10 ) { s_elapsedMinutes = "0" + elapsedMinutes;    }
	    	else { s_elapsedMinutes = "" + elapsedMinutes;                          }
	    	
	    	
	    	builder.setTitle("Session Summary")
	    	.setMessage("Elapsed time: " + s_elapsedMinutes + ":" + s_elapsedSeconds + "." + s_elapsedMillis + "\n"
	    				+ "Data points: " + dataPointCount + "\n"
	    				+ "End date and time: \n" + dateString + "\n"
	    				+ "Filename: \n" + rideNameString + "-" + seatString + "-" + dateString)
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialoginterface,int i) {
	    			dialoginterface.dismiss();
	    			picCount.setText(getString(R.string.picAndVidCount) + mediaCount);
	    		}
	    	})
	    	.setCancelable(true);
	    	
	    	
	    	dialog = builder.create();
	    	break;
	    	
	    case DIALOG_CHOICE:
	    	
			builder.setTitle("Select An Action:")
	    	.setMessage("Would you like to upload your data and media to iSENSE?")
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialoginterface,int i) {
	    			
	    			dialoginterface.dismiss();
	    			
	    			if(len == 0 || len2 == 0)
	    				Toast.makeText(AmusementPark.this, "There is no data to upload!", Toast.LENGTH_LONG).show();
	    			
	    			else {
	    				
	    				String isValid = experimentInput.getText().toString();
		    			if( successLogin  && (isValid.length() > 0) ) {
 		    				//executeIsenseTask = true;
			    			dialoginterface.dismiss();
			    			new Task().execute();
			    		} else {
			    			showDialog(DIALOG_NO_ISENSE);
			    		}
	    		
	    			}
	    		}
	    	})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
     		   	public void onClick(DialogInterface dialoginterface,int i) {
     		   		
     		   		dialoginterface.dismiss();
     		   		if( !choiceViaMenu ) showDialog(DIALOG_SUMMARY);
     		   	} 
     	   	}) 
     	   .setCancelable(true);
	    	
	    	dialog = builder.create();
	    	
	    	break;
	    	
	    case DIALOG_NO_ISENSE:
	    	
	    	builder.setTitle("Cannot Upload to iSENSE")
	    	.setMessage("You are either not logged into iSENSE, or you have not provided a valid Experiment ID to upload your data to. " +
	    			"You will be returned to the main screen, but you may go to Menu -> Upload to upload this data set once you log in " +
	    			"to iSENSE and provide a valid Experiment ID.  You are permitted to continue recording data; however if " +
	    			"you choose to do so, you will not be able to upload the previous data set to iSENSE afterwards.")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialoginterface,int i) {
	    			dialoginterface.dismiss();
	    			if( !choiceViaMenu ) showDialog(DIALOG_SUMMARY);
	    		}
	    	})
     	   .setCancelable(true);
	    	
	    	dialog = builder.create();
	    
	    	break;
	    	
	    case RECORDING_STOPPED:
	    	
	    	throughHandler = false;
	    	
	    	builder.setTitle("Time Up")
	    	.setMessage("You have been recording data for more than 10 minutes.  For the sake of memory, we have capped your maximum " +
	    			"recording time at 10 minutes and have stopped recording for you.  Press OK to continue.")
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialoginterface,int i) {
	    			dialoginterface.dismiss();
	    			showDialog(DIALOG_CHOICE);
	    		}
	    	})
     	   .setCancelable(false);
	    	
	    	dialog = builder.create();
	    
	    	break;
	    	
	    case DIALOG_NO_GPS:
	    	
	    	builder.setTitle("No GPS Provider Found")
	    	.setMessage("Enabling GPS satellites is recommended for this application.  Would you like to enable GPS?")
	    	.setCancelable(false)
	    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.cancel();
	            	   startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	                    dialoginterface.cancel();
	               }
	           });

	    	dialog = builder.create();
	    
	    	break;
	    	
	    case DIALOG_FORCE_STOP:
	    	
	    	builder.setTitle("Data Recording Halted")
	    	.setMessage("You exited the app while data was still being recorded.  Data has stopped recording.")
	    	.setCancelable(false)
	    	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialoginterface, final int id) {
	            	   dialoginterface.dismiss();
	            	   startStop.performLongClick();
	               }
	         });
	           
	    	dialog = builder.create();
	    
	    	break;
	    	
	    default:
	    	dialog = null;
	    	break;
	    }
	    
	    return apiDialogCheckerCase(dialog, lp, id);    	    
	    
	}

	// Method to create a data-saving prompt with "message" as the message
    private AlertDialog getSavePrompt(final Handler h, String message) {	
    	
    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	    	
      	canobieIsChecked = canobieBackup;
    	        
        LayoutInflater vi = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = vi.inflate(R.layout.setup, null);
		
        builder.setView(v);
        
		rides = (Spinner) v.findViewById(R.id.rides);
		
		final ArrayAdapter<CharSequence> generalAdapter = ArrayAdapter.createFromResource(this, R.array.rides_array, android.R.layout.simple_spinner_item);
        generalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        final ArrayAdapter<CharSequence> canobieAdapter = ArrayAdapter.createFromResource(this, R.array.canobie_array, android.R.layout.simple_spinner_item);
        canobieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        if(canobieIsChecked)
        	rides.setAdapter(canobieAdapter);
        else
        	rides.setAdapter(generalAdapter);
        
        seats = (EditText) v.findViewById(R.id.studentNumber);
        
        sessionName = (EditText) v.findViewById(R.id.sessionName);
        
        experimentInput = (EditText) v.findViewById(R.id.ExperimentInput);
        experimentInput.setEnabled(false);
        experimentInput.setFocusable(false);
        if(experimentId != "") experimentInput.setText(experimentId);
        
        experimentInput.setKeyListener(new NumberKeyListener() {
        	@Override 
        	public int getInputType(){
        		return InputType.TYPE_CLASS_PHONE;
        	}
        	@Override
        	protected char[] getAcceptedChars() {
        		return new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        	}
        });
        
        browseButton = (Button) v.findViewById(R.id.BrowseButton);
        browseButton.setOnClickListener(new OnClickListener() {

			
			@Override
			public void onClick(View v) {
				
				if(!rapi.isConnectedToInternet()) {
					Toast.makeText(AmusementPark.this, "You must enable wifi or mobile connectivity to do this.", Toast.LENGTH_SHORT).show();	
				} else {
				
					Intent experimentIntent = new Intent(getApplicationContext(), Experiments.class);
					experimentIntent.putExtra("edu.uml.cs.isense.amusement.experiments.propose", EXPERIMENT_CODE);
				
					Log.w("JSON", "EXPERIMENT_CODE: " + EXPERIMENT_CODE); //honk
				
					startActivityForResult(experimentIntent, EXPERIMENT_CODE);
				}
				
			}
			
		});
        
        canobieCheck = (CheckBox) v.findViewById(R.id.isCanobie);
        if (canobieIsChecked)
        	canobieCheck.setChecked(true);
        else
        	canobieCheck.setChecked(false);
        
        canobieCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				
				if (canobieCheck.isChecked()) {
					canobieIsChecked = true;
					rides.setAdapter(canobieAdapter);
				} else {
					canobieIsChecked = false;
					rides.setAdapter(generalAdapter);
				}
				
			}
        	
        });
        
        // ride adapter WAS here! honk. oh, and it was also NOT a 'final' variable

        rides.setSelection(rideIndex);
        seats.setText(studentNumber);
        
        Button b = (Button) v.findViewById(R.id.pictureButton);
		
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					
					ContentValues values = new ContentValues();
					
					imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
					Log.e("Uri", "imageUri: " + imageUri); //honk
					
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					startActivityForResult(intent, CAMERA_PIC_REQUESTED);
				
				} else {
					if(!dontToastMeTwice) {
						Toast.makeText(AmusementPark.this, 
								"Permission isn't granted to write to external storage.  Please enable to take pictures.", 
								Toast.LENGTH_LONG).show();
						new NoToastTwiceTask().execute();
					}
				}
				
				
			}
			
		});
		
		Button bv = (Button) v.findViewById(R.id.videoButton);
		
		bv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
				
					ContentValues valuesVideos = new ContentValues();
				
					videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, valuesVideos);
					Log.e("Uri", "videoUri: " + videoUri); //honk
				
					Intent intentVid = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					intentVid.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
					intentVid.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					startActivityForResult(intentVid, CAMERA_VID_REQUESTED);
					
				} else {
					if(!dontToastMeTwice) {
						Toast.makeText(AmusementPark.this, 
								"Permission isn't granted to write to external storage.  Please enable to record videos.", 
								Toast.LENGTH_LONG).show();
						new NoToastTwiceTask().execute();
					}
				}
			}
		});
		
        builder.setTitle(message)
        	   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
        		   public void onClick(DialogInterface dialog, int id) {
        			   rideIndex = rides.getSelectedItemPosition();
        			   studentNumber = seats.getText().toString();
        			   
        			   nameOfSession = sessionName.getText().toString();
        			   
 			    	   rideNameString = (String) rides.getSelectedItem();
 			    	   
 			    	   experimentId = (String) experimentInput.getText().toString();
 			    	   
        			   final Message dialogOk = Message.obtain();
        			   dialogOk.setTarget(h);
        			   dialogOk.what = DIALOG_OK;
        				
        			   dialogOk.sendToTarget();
        			   
        			   dialog.dismiss();
        			  
        		   }
        	   })
        	   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        		   public void onClick(DialogInterface dialoginterface,int i) {
        			   final Message dialogOk = Message.obtain();
        			   dialogOk.setTarget(h);
        			   dialogOk.what = DIALOG_CANCELED;
        			   dialogOk.sendToTarget();
        			   
        			   dialoginterface.dismiss();
        		   }
        	   	})
        	   .setCancelable(false);
        	   
        
        return builder.create();
	}    
    
    // Converts the captured picture's uri to a file that is save-able to the SD Card
    public static File convertImageUriToFile (Uri imageUri, Activity activity)  {
		
    	int apiLevel = getApiLevel();
    	if (apiLevel >= 11) {
    		
    		String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
    		String selection = null;
    		String[] selectionArgs = null;
    		String sortOrder = null;

    		CursorLoader cursorLoader = new CursorLoader(
    		        mContext,
    		        imageUri, 
    		        proj, 
    		        selection, 
    		        selectionArgs, 
    		        sortOrder);

    		Cursor cursor = cursorLoader.loadInBackground();
    		
    		int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
	    	if (cursor.moveToFirst()) {
	    		@SuppressWarnings("unused")
	    			String orientation =  cursor.getString(orientation_ColumnIndex);
	    		return new File(cursor.getString(file_ColumnIndex));
	    	}
	    	return null;
		    
    	} else {
    		
    		Cursor cursor = null;
    		try {
    			String [] proj={MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
    			cursor = activity.managedQuery( imageUri,
    					proj, 		// Which columns to return
    					null,       // WHERE clause; which rows to return (all rows)
    					null,       // WHERE clause selection arguments (none)
    					null); 		// Order-by clause (ascending by name)
    			int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    			int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
		    	if (cursor.moveToFirst()) {
		    		@SuppressWarnings("unused")
		    			String orientation =  cursor.getString(orientation_ColumnIndex);
		    		return new File(cursor.getString(file_ColumnIndex));
		    	}
		    	return null;
    		} finally {
    			if (cursor != null) {
    				cursor.close();
    			}
    		}
    	}
	}
    
    // Converts the recorded video's uri to a file that is save-able to the SD Card
    public static File convertVideoUriToFile (Uri videoUri, Activity activity)  {
    	
    	int apiLevel = getApiLevel();
    	if (apiLevel >= 11) {
    		
    		String [] proj={MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID};
    		String selection = null;
    		String[] selectionArgs = null;
    		String sortOrder = null;

    		CursorLoader cursorLoader = new CursorLoader(
    		        mContext,
    		        videoUri, 
    		        proj, 
    		        selection, 
    		        selectionArgs, 
    		        sortOrder);

    		Cursor cursor = cursorLoader.loadInBackground();
    		int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
		    if (cursor.moveToFirst()) {
		        return new File(cursor.getString(file_ColumnIndex));
		    }
		    return null;
		    
    	} else {
    		
    		Cursor cursor = null;
    		
    		try {
    		    String [] proj={MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID};
    		    cursor = activity.managedQuery(videoUri,
    		            proj, 		// Which columns to return
    		            null,       // WHERE clause; which rows to return (all rows)
    		            null,       // WHERE clause selection arguments (none)
    		            null); 		// Order-by clause (ascending by name)
    		    int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
    		    if (cursor.moveToFirst()) {
    		        return new File(cursor.getString(file_ColumnIndex));
    		    }
    		    return null;
    		} finally {
    		    if (cursor != null) {
    		        cursor.close();
    		    }
    		}
    	}
		
	}
    
  	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == CAMERA_PIC_REQUESTED) {
			if(resultCode == RESULT_OK) {				
	            File f = convertImageUriToFile(imageUri, this);
	            pictures.add(f);
				mediaCount++;
				picCount.setText(getString(R.string.picAndVidCount) + mediaCount);
			}
		} else if (requestCode == CAMERA_VID_REQUESTED) {
			if(resultCode == RESULT_OK) {
				File f = convertVideoUriToFile(videoUri, this);
				videos.add(f);
				mediaCount++;
				picCount.setText("" + getString(R.string.picAndVidCount) + mediaCount);
			}
		} else if (requestCode == EXPERIMENT_CODE) {
    		if (resultCode == Activity.RESULT_OK) {
    			experimentInput.setText("" + data.getExtras().getInt("edu.uml.cs.isense.pictures.experiments.exp_id"));
    		}
		}
		
	}
	
  	// Assists with differentiating between displays for dialogues
  	private static int getApiLevel()
  	{
    	return Integer.parseInt(android.os.Build.VERSION.SDK);
    }
  	
  	// Calls the rapi primitives for actual uploading
	private Runnable uploader = new Runnable()
	{
		
		@Override
		public void run()
		{
		
			int sessionId = -1;
			String city = "", state = "", country = "", addr = "";
			List<Address> address = null;
			
			try {
				if (roughLoc != null) {
					address = new Geocoder(AmusementPark. this,Locale.getDefault())
						.getFromLocation(roughLoc.getLatitude(), roughLoc.getLongitude(), 1);
					if (address.size() > 0) {
						city    = address.get(0).getLocality();
						state   = address.get(0).getAdminArea();
						country = address.get(0).getCountryName();
						addr    = address.get(0).getAddressLine(0);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(nameOfSession.equals("")) {
				if(address == null || address.size() <= 0) {
					sessionId = rapi.createSession(experimentInput.getText().toString(), 
							"*Session Name Not Provided*", 
							"Automated Submission Through Android App", 
							"N/A", "N/A", "United States");
				} else {
					sessionId = rapi.createSession(experimentInput.getText().toString(), 
							"*Session Name Not Provided*", 
							"Automated Submission Through Android App", 
							addr, city + ", " + state, country);
				}
			} else {
				if(address == null || address.size() <= 0) {
					sessionId = rapi.createSession(experimentInput.getText().toString(), 
							nameOfSession, 
							"Automated Submission Through Android App", 
							"N/A", "N/A", "United States");
				} else {
					sessionId = rapi.createSession(experimentInput.getText().toString(), 
							nameOfSession, 
							"Automated Submission Through Android App", 
							addr, city + ", " + state, country);
				}
			}
			
			
			rapi.putSessionData(sessionId, experimentInput.getText().toString(), dataSet);
			
			int pic = pictureArray.size();
			
			while(pic > 0) {
				if(nameOfSession.equals(""))
					rapi.uploadPictureToSession(pictureArray.get(pic - 1),
							experimentInput.getText().toString(), 
							sessionId, "*Session Name Not Provided*", "N/A");
				else
					rapi.uploadPictureToSession(pictureArray.get(pic - 1),
							experimentInput.getText().toString(), 
							sessionId, sessionName.getText().toString(), "N/A");

				pic--;
				
			}
			
			pictureArray.clear();
			videoArray.clear();
					
		}
		
	};

	// Control task for uploading data
	private class Task extends AsyncTask <Void, Integer, Void> {
	    
	    @Override protected void onPreExecute()
	    {
	    	
	        dia = new ProgressDialog(AmusementPark.this);
	        dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        dia.setMessage("Please wait while your data are uploaded to iSENSE...");
	        dia.setCancelable(false);
	        dia.show();
	        
	    }

	    @Override protected Void doInBackground(Void... voids)
	    {

	        uploader.run();
	        publishProgress(100);
	        return null;
	        
	    }

	    @Override  protected void onPostExecute(Void voids)
	    {
	        
	    	dia.setMessage("Done");
	        dia.cancel();
	        
	        len = 0; len2 = 0; mediaCount = 0;
	        
	        Toast.makeText(AmusementPark.this, "Upload Success", Toast.LENGTH_SHORT).show();
	        picCount.setText(getString(R.string.picAndVidCount) + mediaCount);
	        showDialog(DIALOG_SUMMARY);
	        
	    }
	}	

	// Prevents toasts from being queued infinitesimally
	private class NoToastTwiceTask extends AsyncTask <Void, Integer, Void> {
	    @Override protected void onPreExecute() {
	    	dontToastMeTwice = true;
	    	exitAppViaBack   = true;
	    }
		@Override protected Void doInBackground(Void... voids) {
	    	try {
	    		Thread.sleep(1500);
	    		exitAppViaBack = false;
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				exitAppViaBack = false;
				e.printStackTrace();
			}
	        return null;
		}
	    @Override  protected void onPostExecute(Void voids) {
	    	dontToastMeTwice = false;
	    }
	}
	
	// Everything needed to be initialized for onCreate
	private void initVars() {
		
        mHandler = new Handler();
        
        Display deviceDisplay = getWindowManager().getDefaultDisplay(); 
    	mwidth  = deviceDisplay.getWidth();
        
        rapi = RestAPI.getInstance((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE), getApplicationContext());

        pictures = new ArrayList<File>();
        videos   = new ArrayList<File>();

        startStop = (Button) findViewById(R.id.startStop);
              
        values = (TextView) findViewById(R.id.values);
        rideName = (TextView) findViewById(R.id.ridename);
        
        rideName.setText("Ride Name: " + rideNameString);
        
        picCount = (TextView) findViewById(R.id.pictureCount);
    	picCount.setText(getString(R.string.picAndVidCount) + mediaCount);
        
        loginInfo = (TextView) findViewById(R.id.loginInfo);
        loginInfo.setText(R.string.notLoggedIn);
        loginInfo.setTextColor(Color.RED);
        
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mRoughLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}
	
	// Takes care of everything to do with EULA
	private void displayEula() {
		
		AlertDialog.Builder adb = new SimpleEula(this).show();
		if(adb != null) {
			Dialog dialog = adb.create();	
			dialog.show();

			apiTabletDisplay(dialog);
		}
	}
	
	// apiTabletDisplay for Dialog Building on Tablets
	static boolean apiTabletDisplay(Dialog dialog) {
		int apiLevel = getApiLevel();
		if(apiLevel >= 11) {
			dialog.show();
			
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	
			lp.copyFrom(dialog.getWindow().getAttributes());
			lp.width = mwidth;
			lp.height = WindowManager.LayoutParams.MATCH_PARENT;;
			lp.gravity = Gravity.CENTER_VERTICAL;
			lp.dimAmount=0.7f;
	
			dialog.getWindow().setAttributes(lp);
			dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			
			return true;
		} else return false;
		
	}

	// Deals with login and UI display
	void login()
	{
		boolean success = rapi.login(loginName, loginPass); 
		if(success) {                                       
			loginInfo.setText(" "+ loginName);
			loginInfo.setTextColor(Color.GREEN);
			successLogin = true;
		}
	}
	
    // Gets the milliseconds since Epoch
	private long getUploadTime()
    {
    		Calendar c = Calendar.getInstance();
    		return ((long) c.getTimeInMillis());
    }
	
	// Deals with Dialog creation whether api is tablet or not
	Dialog apiDialogCheckerCase(Dialog dialog, LayoutParams lp, final int id) {
		if(apiTabletDisplay(dialog)) {
		    	
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					removeDialog(id);
				}
			});
		    return null;
		    
		} else {
	    		
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
	            public void onDismiss(DialogInterface dialog) {
					removeDialog(id);
				}
			});
	    		
			return dialog;
		}
	}
		
}