package it.sephiroth.android.example.exifsample;

import it.sephiroth.android.example.exifsample.utils.IOUtils;
import it.sephiroth.android.library.media.ExifInterfaceExtended;
import java.io.IOException;
import java.util.Set;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
}
