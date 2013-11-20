package it.sephiroth.android.example.exifsample;

import it.sephiroth.android.example.exifsample.utils.IOUtils;
import it.sephiroth.android.library.media.ExifInterfaceExtended;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	static final String LOG_TAG = "MainActivity";
	static final int REQUEST_FILE = 1; 
	Button button1, button2;
	ImageView image;
	TextView exifText;
	ExifInterfaceExtended mExif;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		button1 = (Button) findViewById( R.id.button1 );
		button2 = (Button) findViewById( R.id.button2 );
		image = (ImageView) findViewById( R.id.image1 );
		exifText = (TextView) findViewById( R.id.exif );

		button1.setOnClickListener( this );
		button2.setOnClickListener( this );
		
		String uriString = "content://media/external/images/media/32706";
		uriString = "content://media/external/images/media/41402";
		// uriString = "content://media/external/images/media/25470";
		// String uriString = "content://media/external/images/media/32705";
		// String uriString = ( "content://media/external/images/media/18937";

		Log.i( LOG_TAG, "ExifInterfaceExtended.Version: " + ExifInterfaceExtended.VERSION );
		
		Uri uri = Uri.parse( uriString );
		processFile( uri );
	}

	private void loadImage() {
		Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
		intent.setType( "image/*" );
		Intent chooser = Intent.createChooser( intent, "Choose picture" );
		startActivityForResult( chooser, REQUEST_FILE );
	}
	
	public void saveImage() {
		if( null != mExif ) {
			Log.i( LOG_TAG, "saving: " + mExif );

			/*
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_VERSION );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_MAKE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_MODEL );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_SOFTWARE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_COPYRIGHT );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_ORIENTATION );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_DIGITIZED );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_ORIGINAL );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_FLASH );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGHT );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGTH_35_MM );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_APERTURE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_BRIGHTNESS );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_COLOR_SPACE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_ISO_SPEED_RATINGS );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_LIGHT_SOURCE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_METERING_MODE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_PROGRAM );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_MODE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_SHUTTER_SPEED_VALUE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_SENSING_METHOD );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_SCENE_CAPTURE_TYPE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_MAXAPERTURE );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_PIXEL_X_DIMENSION );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_PIXEL_Y_DIMENSION );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_X_RESOLUTION );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_Y_RESOLUTION );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_FOCAL_PLANE_X_RESOLUTION );
			mExif.removeAttribute( ExifInterfaceExtended.TAG_EXIF_COMPRESSION );
			*/
			
			mExif.setAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME, ExifInterfaceExtended.formatDate( new Date() ) );
			mExif.setAttribute( ExifInterfaceExtended.TAG_EXIF_ARTIST, "Alessandro Crugnola" );
			
			try {
				mExif.saveAttributes();
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}
	
	private void dumpToFile( ExifInterfaceExtended exif ) {
		File dir = new File( Environment.getExternalStorageDirectory(), getPackageName() );
		if( !dir.exists() ) {
			dir.mkdirs();
		}
		
		try {
			File file = new File( dir, "exif.txt");
			FileOutputStream stream = new FileOutputStream( file );
			
			Set<String> keys = exif.keySet();
			for( String key : keys ) {
				String line = key + " = " + exif.getAttribute( key ) + "\n";
				stream.write( line.getBytes() );
			}
			stream.flush();
			stream.close();
			
		} catch ( IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void processFile( Uri uri ) {
		
		Log.i( LOG_TAG, "processFile: " + uri );
		
		String filename = IOUtils.getRealFilePath( this, uri );
		
		if( null == filename ) {
			return;
		}
		
		image.setImageBitmap( null );
		exifText.setText( "" );

		mExif = null;
		
		try {
			mExif = new ExifInterfaceExtended( filename );
			dumpToFile( mExif );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		if( null != mExif ) {
			
			NumberFormat numberFormatter = DecimalFormat.getNumberInstance();
			
			exifText.setText( "JPEG Info:\n" );
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_FILESIZE )) {
				String value = DecimalFormat.getInstance().format( ((double) mExif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_FILESIZE, 0 ) / 1024.0 ) );
				exifText.append( "File size: " + value + "Kb\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_FILE_DATETIME )) {
				Date datetimeFile = new Date( mExif.getDateTime( mExif.getAttribute( ExifInterfaceExtended.TAG_JPEG_FILE_DATETIME ) ) );
				exifText.append( "File datetime: " + datetimeFile + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_IMAGE_WIDTH ) && mExif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_IMAGE_HEIGHT )) {
				exifText.append( "Image size: " + mExif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_IMAGE_WIDTH, 0 ) + "x" + mExif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_IMAGE_HEIGHT, 0 ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_PROCESS )) {
				int process = mExif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_PROCESS, 0 );
				exifText.append( "Process: " + parseProcess( process ) + "\n" );
			}
			
			int quality = mExif.getJpegQuality();
			if( quality > 0 ) {
				exifText.append( "JPEG Quality: " + quality + "\n" );
			}
			
			exifText.append( "\nEXIF Tags:\n" );
			
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_VERSION )){
				exifText.append( "Exif Version: " + mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_VERSION ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_MAKE )) {
				exifText.append( "Camera: " + mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_MAKE ) + "\n" );
			}
			
			if(mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_MODEL )) {
				exifText.append( "Model: " + mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_MODEL ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SOFTWARE )) {
				exifText.append( "Software: " + mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_SOFTWARE ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_ARTIST )) {
				exifText.append( "Artist: " + mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_ARTIST ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_COPYRIGHT )) {
				exifText.append( "Copyright: " + mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_COPYRIGHT ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_ORIENTATION )) {
				exifText.append( "Orientation: " + mExif.getOrientation() + "°\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME )) {
				Date datetime = new Date( mExif.getDateTime( mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME ) ) );
				exifText.append( "DateTime: " + datetime + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_DIGITIZED )) {
				Date datetime = new Date( mExif.getDateTime( mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_DIGITIZED ) ) );
				exifText.append( "DateTime Digitized: " + datetime + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_ORIGINAL )) {
				Date datetime = new Date( mExif.getDateTime( mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_ORIGINAL ) ) );
				exifText.append( "DateTime Original: " + datetime + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_FLASH )) {
				int flash = mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_FLASH, 0 );
				exifText.append( "Flash: " + processFlash(flash) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGHT ) ) {
				exifText.append( "Focal Length: " + mExif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGHT, 0 ) + "mm\n" );
				
				if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGTH_35_MM )) {
					exifText.append( "35mm Equivalent: " + mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGTH_35_MM, 0 ) + "mm\n" );
				}
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_DIGITAL_ZOOM_RATIO )) {
				exifText.append( "Digital Zoom: " + mExif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_DIGITAL_ZOOM_RATIO, 0 ) + "X\n" );
			}
			
			double ccd_width = mExif.getCCDWidth();
			if( ccd_width > 0 ) {
				exifText.append( "CCD Width: " + DecimalFormat.getNumberInstance().format( ccd_width ) + "mm\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_TIME )) {
				exifText.append( "Exposure Time: " + mExif.getAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_TIME ) + "s\n" );
			}
			
			double fNumber = mExif.getApertureSize();
			if( fNumber > 0 ) {
				exifText.append( "Aperture Size: f/" + fNumber + "\n" );
			}

			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_BRIGHTNESS ) ) {
				exifText.append( "Brightness: " + mExif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_BRIGHTNESS, 0 ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_COLOR_SPACE ) ) {
				exifText.append( "Color Space: " + processColorSpace( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_COLOR_SPACE, 0 ) ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SUBJECT_DISTANCE )) {
				double distance = mExif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_SUBJECT_DISTANCE, 0 );
				if( distance > 0 ) {
					exifText.append( "Subject Distance: " + distance + "m\n" );
				} else {
					exifText.append( "Subject Distance: Infinite\n" );
				}
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SUBJECT_DISTANCE_RANGE )) {
				int value = mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_SUBJECT_DISTANCE_RANGE, 0 );
				exifText.append( "Subject Distance Range: " + processSubjectDistanceRange( value ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_ISO_SPEED_RATINGS )) {
				exifText.append( "ISO equiv. " + mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_ISO_SPEED_RATINGS, 0 ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_BIAS )) {
				exifText.append( "Exposure Bias: " + mExif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_BIAS, 0 ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_WHITE_BALANCE )) {
				exifText.append( "White Balance: " + processWhiteBalance( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_WHITE_BALANCE, 0 )) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_LIGHT_SOURCE )) {
				exifText.append( "Light Source: " + processLightSource( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_LIGHT_SOURCE, 0 ) ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_METERING_MODE )) {
				exifText.append( "Metering Mode: " + processMeteringMode( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_METERING_MODE, 0 ) ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_PROGRAM )) {
				exifText.append( "Exposure Program: " + processExposureProgram( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_PROGRAM, 0 ) ) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_MODE )) {
				exifText.append( "Exposure Mode: " + processExposureMode( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_MODE, 0 ) ) + "\n" );
			}
			
			if( mExif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_SHUTTER_SPEED_VALUE, 0 ) > 0 ) {
				double value = mExif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_SHUTTER_SPEED_VALUE, 0 );
				
				numberFormatter.setMaximumFractionDigits( 0 );
				String string = "1/" + numberFormatter.format( Math.pow( 2, value )) + "s";
				exifText.append( "Shutter Speed: " + string + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SENSING_METHOD )) {
				exifText.append( "Sensing Method: " + processSensingMethod( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_SENSING_METHOD, 0 )) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SCENE_CAPTURE_TYPE )) {
				exifText.append( "Scene Capture Type: " + processSceneCaptureType( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_SCENE_CAPTURE_TYPE, 0 )) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SHARPNESS )) {
				exifText.append( "Sharpness: " + processSharpness( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_SHARPNESS, 0 )) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_CONTRAST )) {
				exifText.append( "Contrast: " + processContrast( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_CONTRAST, 0 )) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SATURATION )) {
				exifText.append( "Saturation: " + processSaturation( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_SATURATION, 0 )) + "\n" );
			}
			
			if( mExif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_GAIN_CONTROL )) {
				exifText.append( "Gain Control: " + processGainControl( mExif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_GAIN_CONTROL, 0 )) + "\n" );
			}
			
			// GPS
			float[] output = new float[2];
			if( mExif.getLatLong( output ) ) {
				
				exifText.append( "\nGPS Info:\n" );
				
				double altitude = mExif.getAltitude( 0 );
				if( altitude != 0 ) {
					exifText.append( "Altitude: " + altitude + "m\n" );
				}
				
				String latitude = mExif.getLatitude();
				String longitude = mExif.getLongitude();
				if( null != latitude && null != longitude ) {
					exifText.append( "Latitude: " + latitude + "\n" );
					exifText.append( "Longitude: " + longitude + "\n" );
				}
				
				String speed = mExif.getGpsSpeed();
				if( null != speed ) {
					exifText.append( "Speed: " + speed + "\n" );
				}
				
				GetGeoLocationTask task = new GetGeoLocationTask();
				task.execute( output[0], output[1] );
			}
			
			
			
		}
		new LoadThumbnailTask().execute( filename );
	}
	
	private String processSharpness( int value ){
		switch ( value ) {
			case 0: return "Normal";
			case 1: return "Soft";
			case 2: return "Hard";
			default: return "Unknown";
		}
	}
	
	private String processContrast( int value ){
		switch ( value ) {
			case 0: return "Normal";
			case 1: return "Soft";
			case 2: return "Hard";
			default: return "Unknown";
		}
	}
	
	private String processSaturation( int value ){
		switch ( value ) {
			case 0: return "Normal";
			case 1: return "Low Saturation";
			case 2: return "High Saturation";
			default: return "Unknown";
		}
	}
	
	private String processGainControl( int value ){
		switch ( value ) {
			case 0: return "None";
			case 1: return "Low Gain Up";
			case 2: return "High Gain Up";
			case 3: return "Low Gain Down";
			case 4: return "High Gain Down";
			default: return "Unknown";
		}
	}	
	
	
	private String processSceneCaptureType( int value ){
		switch( value ) {
			case 0: return "Standard";
			case 1: return "Landscape";
			case 2: return "Portrait";
			case 3: return "Night scene";
			 default: return "Unknown";
		}
	}
	
	private String processSensingMethod( int value ) {
		switch( value ) {
			case 1: return "Not defined";
			 case 2: return "One-chip color area sensor";
			 case 3: return "Two-chip color area sensor JEITA CP-3451 - 41";
			 case 4: return "Three-chip color area sensor";
			 case 5: return "Color sequential area sensor";
			 case 7: return "Trilinear sensor";
			 case 8: return "Color sequential linear sensor";
			 default: return "Unknown";
		}
	}
	
	private String processColorSpace( int value ) {
		switch( value ) {
			case 1: return "sRGB";
			case 0xFFFF: return "Uncalibrated";
			default: return "Unknown";
		}
	}
	
	private String processExposureMode( int mode ) {
		switch( mode ) {
			case 0: return "Auto exposure";
			case 1: return "Manual exposure";
			case 2: return "Auto bracket";
			default: return "Unknown";
		}
	}
	
	private String processExposureProgram( int program ) {
		switch( program ) {
			case 1: return "Manual control";
			case 2: return "Program normal";
			case 3: return "Aperture priority";
			case 4: return "Shutter priority";
			case 5: return "Program creative (slow program)";
			case 6: return "Program action(high-speed program)";
			case 7: return "Portrait mode";
			case 8: return "Landscape mode";
			default: return "Unknown";
		}
	}
	
	private String processMeteringMode( int mode ) {
		switch( mode ) {
			case 1: return "Average";
			case 2: return "CenterWeightedAverage";
			case 3: return "Spot";
			case 4: return "MultiSpot";
			case 5: return "Pattern";
			case 6: return "Partial";
			case 255: return "Other";			
			default: return "Unknown";
		}
	}
	
	private String processLightSource( int value ) {
		switch( value ) {
			case 0: return "Auto";
			case 1: return "Daylight";
			case 2: return "Fluorescent";
			case 3: return "Tungsten (incandescent light)";
			case 4: return "Flash";
			case 9: return "Fine weather";
			case 10: return "Cloudy weather";
			case 11: return "Shade";
			case 12: return "Daylight fluorescent (D 5700 - 7100K)";
			case 13: return "Day white fluorescent (N 4600 - 5400K)";
			case 14: return "Cool white fluorescent (W 3900 - 4500K)";
			case 15: return "White fluorescent (WW 3200 - 3700K)";
			case 17: return "Standard light A";
			case 18: return "Standard light B";
			case 19: return "Standard light C";
			case 20: return "D55";
			case 21: return "D65";
			case 22: return "D75";
			case 23: return "D50";
			case 24: return "ISO studio tungsten";
			case 255: return "Other light source";
			default: return "Unknown";		
		}
	}
	
	private String processWhiteBalance( int value ) {
		switch( value ){
			case 0: return "Auto";
			case 1: return "Manual";
			default: return "Unknown";
		}
	}
	
	private String processSubjectDistanceRange( int value ) {
		switch( value ) {
			case 1: return "Macro";
			case 2: return "Close View";
			case 3: return "Distant View";
			default: return "Unknown";
		}
	}
	
	private String processFlash( int flash ) {
		Log.i( LOG_TAG, "flash: " + flash + ", " + ( flash & 1 ) );
		switch( flash ) {
			case 0x0000: return "Flash did not fire";
			case 0x0001: return "Flash fired";
			case 0x0005: return "Strobe return light not detected";
			case 0x0007: return "Strobe return light detected";
			case 0x0009: return "Flash fired, compulsory flash mode";
			case 0x000D: return "Flash fired, compulsory flash mode, return light not detected";
			case 0x000F: return "Flash fired, compulsory flash mode, return light detected";
			case 0x0010: return "Flash did not fire, compulsory flash mode";
			case 0x0018: return "Flash did not fire, auto mode";
			case 0x0019: return "Flash fired, auto mode";
			case 0x001D: return "Flash fired, auto mode, return light not detected";
			case 0x001F: return "Flash fired, auto mode, return light detected";
			case 0x0020: return "No flash function";
			case 0x0041: return "Flash fired, red-eye reduction mode";
			case 0x0045: return "Flash fired, red-eye reduction mode, return light not detected";
			case 0x0047: return "Flash fired, red-eye reduction mode, return light detected";
			case 0x0049: return "Flash fired, compulsory flash mode, red-eye reduction mode";
			case 0x004D: return "Flash fired, compulsory flash mode, red-eye reduction mode, return light not detected";
			case 0x004F: return "Flash fired, compulsory flash mode, red-eye reduction mode, return light detected";
			case 0x0059: return "Flash fired, auto mode, red-eye reduction mode";
			case 0x005D: return "Flash fired, auto mode, return light not detected, red-eye reduction mode";
			case 0x005F: return "Flash fired, auto mode, return light detected, red-eye reduction mode";
			default: return "Reserved";			
		}
	}
	
	private String parseProcess( int process ) {
		switch( process ) {
			case 192: return "Baseline";
			case 193: return "Extended sequential";
			case 194: return "Progressive";
			case 195: return "Lossless";
			case 197: return "Differential sequential";
			case 198: return "Differential progressive";
			case 199: return "Differential lossless";
			case 201: return "Extended sequential, arithmetic coding";
			case 202: return "Progressive, arithmetic coding";
			case 203: return "Lossless, arithmetic coding";
			case 205: return "Differential sequential, arithmetic coding";
			case 206: return "Differential progressive, arithmetic codng";
			case 207: return "Differential lossless, arithmetic coding";
		}
		return "Unknown";
	}
	
	
	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
		
		Log.d( LOG_TAG, "data: " + data );
		
		if( resultCode == RESULT_OK ) {
			if( requestCode == REQUEST_FILE ) {
				processFile( data.getData() );
			}
		}
	}

	@Override
	public void onClick( View v ) {
		final int id = v.getId();

		if ( id == button1.getId() ) {
			loadImage();
		} else if ( id == button2.getId() ) {
			saveImage();
		}
	}

	
	private class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground( String... params ) {
			
			String filename = params[0];
			Bitmap thumbnail = null;
			ExifInterfaceExtended exif = null;
			
			try {
				exif = new ExifInterfaceExtended( filename );
			} catch ( IOException e ) {
				e.printStackTrace();
			}

			
			if( null != exif ) {
				if( exif.hasThumbnail() ) {
					byte[] data = exif.getThumbnail();
					if( null != data ) {
						thumbnail = BitmapFactory.decodeByteArray( data, 0, data.length );
					}
				}
			}
			
//			if( thumbnail == null ) {
//				Options options = new BitmapFactory.Options();
//				options.inSampleSize = 2;
//				options.inPreferQualityOverSpeed = false;
//				options.inPreferredConfig = Bitmap.Config.RGB_565;
//				Bitmap bitmap = BitmapFactory.decodeFile( filename, options );
//				thumbnail = ThumbnailUtils.extractThumbnail( bitmap, 200, 200 );
//			}
			
			return thumbnail;
		}
		
		@Override
		protected void onPostExecute( Bitmap result ) {
			super.onPostExecute( result );
			image.setImageBitmap( result );
		}
	}
	
	private class GetGeoLocationTask extends AsyncTask<Float, Void, Address> {

		@Override
		protected Address doInBackground( Float... params ) {

			float lat = params[0];
			float lon = params[1];
			
			Log.d( LOG_TAG, "lat: " + lat + ", lon: " + lon );

			List<Address> result = null;

			try {
				if( Geocoder.isPresent() ){
					Geocoder geo = new Geocoder( MainActivity.this );
					result = geo.getFromLocation( lat, lon, 1 );
				}
			} catch ( Exception e ) {
				e.printStackTrace();
				return null;
			}
			
			Log.d( LOG_TAG, "result: " + result );

			if ( null != result && result.size() > 0 ) {
				return result.get( 0 );
			}

			return null;
		}

		@Override
		protected void onPostExecute( Address result ) {
			super.onPostExecute( result );
			
			if ( isCancelled() || isFinishing() ) return;

			if ( null != result ) {
				
				StringBuilder finalString = new StringBuilder();
				
				if( null != result.getThoroughfare() ) {
					finalString.append( result.getThoroughfare() );
					
					if( null != result.getSubThoroughfare() ) {
						finalString.append( " " + result.getSubThoroughfare() );
					}
					
					finalString.append( "\n" );
				}
				
				if( null != result.getPostalCode() ) {
					finalString.append( result.getPostalCode() );
					
					if( null != result.getLocality() ) {
						finalString.append( " - " + result.getLocality() + "\n" );
					}
				} else {
					if( null != result.getLocality() ) {
						finalString.append( result.getLocality() + "\n" );
					}
				}
				
				if( null != result.getCountryName() ) {
					finalString.append( result.getCountryName() );
				} else if( null != result.getCountryCode() ) {
					finalString.append( result.getCountryCode() );
				}
				
				if( finalString.length() > 0 ) {
					finalString.append( "\n" );
					exifText.append( "\nAddress:\n" );
					exifText.append( finalString );
				}
			}
		}
	}	
}
