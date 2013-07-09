package it.sephiroth.android.example.exifsample;

import it.sephiroth.android.example.exifsample.utils.IOUtils;
import it.sephiroth.android.library.media.ExifInterfaceExtended;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
		// uriString = "content://media/external/images/media/25470";
		// String uriString = "content://media/external/images/media/32705";
		// String uriString = ( "content://media/external/images/media/18937";
		
		Uri uri = Uri.parse( uriString );
		processFile( uri );
	}

	private void loadImage() {
		Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
		intent.setType( "image/*" );
		Intent chooser = Intent.createChooser( intent, "Choose picture" );
		startActivityForResult( chooser, REQUEST_FILE );
	}
	
	private void processFile( Uri uri ) {
		String filename = IOUtils.getRealFilePath( this, uri );
		
		if( null == filename ) {
			return;
		}
		
		image.setImageBitmap( null );
		exifText.setText( "" );

		ExifInterfaceExtended exif = null;
		
		try {
			exif = new ExifInterfaceExtended( filename );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		
		if( null != exif ) {
			
			NumberFormat numberFormatter = DecimalFormat.getNumberInstance();
			
			exifText.setText( "JPEG Info:\n" );
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_FILESIZE )) {
				String value = DecimalFormat.getInstance().format( ((double) exif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_FILESIZE, 0 ) / 1024.0 ) );
				exifText.append( "File size: " + value + "Kb\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_FILE_DATETIME )) {
				Date datetimeFile = new Date( exif.getDateTime( exif.getAttribute( ExifInterfaceExtended.TAG_JPEG_FILE_DATETIME ) ) );
				exifText.append( "File datetime: " + datetimeFile + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_IMAGE_WIDTH ) && exif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_IMAGE_HEIGHT )) {
				exifText.append( "Image size: " + exif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_IMAGE_WIDTH, 0 ) + "x" + exif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_IMAGE_HEIGHT, 0 ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_PROCESS )) {
				int process = exif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_PROCESS, 0 );
				exifText.append( "Process: " + parseProcess( process ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_JPEG_QUALITY )) {
				exifText.append( "JPEG Quality: " + exif.getAttributeInt( ExifInterfaceExtended.TAG_JPEG_QUALITY, 0 ) + "\n" );
			}
			
			exifText.append( "\nEXIF Tags:\n" );
			
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_VERSION )){
				exifText.append( "Exif Version: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_VERSION ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_MAKE )) {
				exifText.append( "Camera: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_MAKE ) + "\n" );
			}
			
			if(exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_MODEL )) {
				exifText.append( "Model: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_MODEL ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SOFTWARE )) {
				exifText.append( "Software: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_SOFTWARE ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_ARTIST )) {
				exifText.append( "Artist: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_ARTIST ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_COPYRIGHT )) {
				exifText.append( "Copyright: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_COPYRIGHT ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_ORIENTATION )) {
				exifText.append( "Orientation: " + exif.getOrientation() + "¡\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME )) {
				Date datetime = new Date( exif.getDateTime( exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME ) ) );
				exifText.append( "DateTime: " + datetime + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_DIGITIZED )) {
				Date datetime = new Date( exif.getDateTime( exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_DIGITIZED ) ) );
				exifText.append( "DateTime Digitized: " + datetime + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_ORIGINAL )) {
				Date datetime = new Date( exif.getDateTime( exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_ORIGINAL ) ) );
				exifText.append( "DateTime Original: " + datetime + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_FLASH )) {
				int flash = exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_FLASH, 0 );
				exifText.append( "Flash: " + processFlash(flash) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGHT ) ) {
				exifText.append( "Focal Length: " + exif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGHT, 0 ) + "mm\n" );
				
				if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGTH_35_MM )) {
					exifText.append( "35mm Equivalent: " + exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_FOCAL_LENGTH_35_MM, 0 ) + "mm\n" );
				}
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_DIGITAL_ZOOM_RATIO )) {
				exifText.append( "Digital Zoom: " + exif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_DIGITAL_ZOOM_RATIO, 0 ) + "X\n" );
			}
			
			double ccd_width = exif.getCCDWidth();
			if( ccd_width > 0 ) {
				exifText.append( "CCD Width: " + DecimalFormat.getNumberInstance().format( ccd_width ) + "mm\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_TIME )) {
				exifText.append( "Exposure Time: " + exif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_TIME, 0 ) + "s\n" );
			}
			
			double fNumber = exif.getApertureSize();
			if( fNumber > 0 ) {
				exifText.append( "Aperture Size: f/" + fNumber + "\n" );
			}

			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_BRIGHTNESS ) ) {
				exifText.append( "Brightness: " + exif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_BRIGHTNESS, 0 ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_COLOR_SPACE ) ) {
				exifText.append( "Color Space: " + processColorSpace( exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_COLOR_SPACE, 0 ) ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SUBJECT_DISTANCE )) {
				double distance = exif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_SUBJECT_DISTANCE, 0 );
				if( distance > 0 ) {
					exifText.append( "Subject Distance: " + distance + "m\n" );
				} else {
					exifText.append( "Subject Distance: Infinite\n" );
				}
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SUBJECT_DISTANCE_RANGE )) {
				int value = exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_SUBJECT_DISTANCE_RANGE, 0 );
				exifText.append( "Subject Distance Range: " + processSubjectDistanceRange( value ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_ISO_SPEED_RATINGS )) {
				exifText.append( "ISO equiv. " + exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_ISO_SPEED_RATINGS, 0 ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_BIAS )) {
				exifText.append( "Exposure Bias: " + exif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_BIAS, 0 ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_WHITE_BALANCE )) {
				exifText.append( "White Balance: " + processWhiteBalance( exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_WHITE_BALANCE, 0 )) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_LIGHT_SOURCE )) {
				exifText.append( "Light Source: " + processLightSource( exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_LIGHT_SOURCE, 0 ) ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_METERING_MODE )) {
				exifText.append( "Metering Mode: " + processMeteringMode( exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_METERING_MODE, 0 ) ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_PROGRAM )) {
				exifText.append( "Exposure Program: " + processExposureProgram( exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_PROGRAM, 0 ) ) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_MODE )) {
				exifText.append( "Exposure Mode: " + processExposureMode( exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_EXPOSURE_MODE, 0 ) ) + "\n" );
			}
			
			if( exif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_SHUTTER_SPEED_VALUE, 0 ) > 0 ) {
				double value = exif.getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_SHUTTER_SPEED_VALUE, 0 );
				
				numberFormatter.setMaximumFractionDigits( 1 );
				String string = "1/" + numberFormatter.format( Math.pow( 2, value )) + "s";
				exifText.append( "Shutter Speed: " + string + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SENSING_METHOD )) {
				exifText.append( "Sensing Method: " + processSensingMethod( exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_SENSING_METHOD, 0 )) + "\n" );
			}
			
			if( exif.hasAttribute( ExifInterfaceExtended.TAG_EXIF_SCENE_CAPTURE_TYPE )) {
				exifText.append( "Scene Capture Type: " + processSceneCaptureType( exif.getAttributeInt( ExifInterfaceExtended.TAG_EXIF_SCENE_CAPTURE_TYPE, 0 )) + "\n" );
			}
			
			// GPS
			float[] output = new float[2];
			if( exif.getLatLong( output ) ) {
				
				exifText.append( "\nGPS Info:\n" );
				exifText.append( "Latitude: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_GPS_LATITUDE ) + "\n" );
				exifText.append( "Longitude: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_GPS_LATITUDE ) + "\n" );
				exifText.append( "Altitude: " + exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_GPS_ALTITUDE ) + "\n" );
				
				GetGeoLocationTask task = new GetGeoLocationTask();
				task.execute( output[0], output[1] );
			}
			
		}
		new LoadThumbnailTask().execute( filename );
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
			case 12: return "Daylight fluorescent (D 5700 Ð 7100K)";
			case 13: return "Day white fluorescent (N 4600 Ð 5400K)";
			case 14: return "Cool white fluorescent (W 3900 Ð 4500K)";
			case 15: return "White fluorescent (WW 3200 Ð 3700K)";
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

			List<Address> result;

			try {
				Geocoder geo = new Geocoder( MainActivity.this );
				result = geo.getFromLocation( lat, lon, 1 );
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
