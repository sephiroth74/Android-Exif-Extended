package it.sephiroth.android.example.exifsample;

import it.sephiroth.android.example.exifsample.utils.IOUtils;
import it.sephiroth.android.library.media.ExifInterfaceExtended;
import java.io.IOException;
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
		uriString = "content://media/external/images/media/25470";
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
			Set<String> keys = exif.keySet();
			
			for( String key : keys ) {
				exifText.append( key + " = " + exif.getAttribute( key ) + "\n" );
			}
			
			float[] output = new float[2];
			if( exif.getLatLong( output ) ) {
				GetGeoLocationTask task = new GetGeoLocationTask();
				task.execute( output[0], output[1] );
			}
			
			double altitude = exif.getAltitude( 0 );
			Log.d( LOG_TAG, "alt: " + altitude );
			
			
			Date datetime = new Date( exif.getDateTime( exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME ) ) );
			Date datetimeDigitized = new Date( exif.getDateTime( exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_DIGITIZED ) ) );
			Date datetimeOriginal = new Date( exif.getDateTime( exif.getAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME_ORIGINAL ) ) );
			Date datetimeFile = new Date( exif.getDateTime( exif.getAttribute( ExifInterfaceExtended.TAG_JPEG_FILE_DATETIME ) ) );
			
			Log.d( LOG_TAG, "date time: " + datetime );
			Log.d( LOG_TAG, "date time digitized: " + datetimeDigitized );
			Log.d( LOG_TAG, "date time original: " + datetimeOriginal );
			Log.d( LOG_TAG, "date time file: " + datetimeFile );
			
			
		}
		
		
		new LoadThumbnailTask().execute( filename );
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
					exifText.append( "Address:\n" );
					exifText.append( finalString );
				}
			}
		}
	}	
}
