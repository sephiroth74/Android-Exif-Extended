package it.sephiroth.android.example.exifsample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import it.sephiroth.android.example.exifsample.utils.IOUtils;
import it.sephiroth.android.library.exif2.BuildConfig;
import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;

public class MainActivity extends Activity implements OnClickListener {

	static final String LOG_TAG = "MainActivity";
	static final int REQUEST_FILE = 1;
	Button button1, button2;
	ImageView image;
	TextView exifText;
	ExifInterface mExif;
	Uri mUri;

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

		Log.i( LOG_TAG, "ExifInterfaceExtended.Version: " + BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE );

		Uri uri = Uri.parse( uriString );
		processFile( uri );
	}

	private void loadImage() {
		Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
		intent.setType( "image/*" );
		Intent chooser = Intent.createChooser( intent, "Choose picture" );
		startActivityForResult( chooser, REQUEST_FILE );
	}

	public void saveImage() throws IOException {
		if( null != mExif ) {
			Log.i( LOG_TAG, "saving: " + mExif );

			File file = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "exif.jpg" );

			ExifTag newTag = mExif.buildTag( ExifInterface.TAG_ARTIST, "Alessandro Crugnola" );
			mExif.setTag( newTag );

			if( null != mUri ) {

				InputStream in = null;
				OutputStream out = null;

				in = getContentResolver().openInputStream( mUri );
				out = new FileOutputStream( file );

				if( null != in && null != out ) {
					mExif.writeExif( in, out );
				}
				out.close();
				in.close();
			}


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

			//			mExif.setAttribute( ExifInterfaceExtended.TAG_EXIF_DATETIME, ExifInterfaceExtended.formatDate( new Date() ) );
			//			mExif.setAttribute( ExifInterfaceExtended.TAG_EXIF_ARTIST, "Alessandro Crugnola" );

			//			try {
			//				mExif.saveAttributes();
			//			} catch ( IOException e ) {
			//				e.printStackTrace();
			//			}
		}
	}

	private void dumpToFile( ExifInterface exif ) {
		try {
			File file = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "exif.txt" );
			Log.d( LOG_TAG, "writing to " + file.getAbsolutePath() );

			FileOutputStream stream = new FileOutputStream( file );
			List<ExifTag> tags = exif.getAllTags();

			for( ExifTag key : tags ) {
				String line = key.toString() + "\n";
				stream.write( line.getBytes() );
			}
			stream.flush();
			stream.close();

		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

	private void processUri( Uri uri, ExifInterface exif ) throws IOException {
		String filename = IOUtils.getRealFilePath( this, uri );

		if( null == filename ) {
			Log.w( LOG_TAG, "filename is null" );
			InputStream stream = getContentResolver().openInputStream( uri );
			exif.readExif( stream );
		}
		else {
			exif.readExif( filename );
		}
	}

	private String createStringFromIfFound(ExifInterface exif, int key, String label) {
		String exifString = "";
		ExifTag tag = exif.getTag( key );
		if (null != tag ) {
			exifString += "<b>" + label + ": </b>";
			exifString += tag.forceGetValueAsString();
			exifString += "<br>";
		} else {
			Log.w( LOG_TAG, "'" + label + "' not found" );
		}
		return exifString;
	}

	private void processFile( Uri uri ) {

		Log.i( LOG_TAG, "processFile: " + uri );

		mExif = new ExifInterface();

		try {
			processUri( uri, mExif );
			mUri = uri;
		} catch( IOException e ) {
			e.printStackTrace();
			mExif = null;
			Toast.makeText( this, e.getMessage(), Toast.LENGTH_SHORT ).show();
		}

		image.setImageBitmap( null );
		exifText.setText( "" );

		if( null != mExif ) {
			StringBuilder string = new StringBuilder();
			NumberFormat numberFormatter = DecimalFormat.getNumberInstance();

			dumpToFile( mExif );
			exifText.setText( "<h2>JPEG Info<h2><br/>" );

			new LoadThumbnailTask().execute( mExif );

			List<ExifTag> list = mExif.getAllTags();


			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_IMAGE_WIDTH, "TAG_IMAGE_WIDTH" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_IMAGE_LENGTH, "TAG_IMAGE_LENGTH" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_BITS_PER_SAMPLE, "TAG_BITS_PER_SAMPLE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COMPRESSION, "TAG_COMPRESSION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION, "TAG_PHOTOMETRIC_INTERPRETATION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_IMAGE_DESCRIPTION, "TAG_IMAGE_DESCRIPTION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_MAKE, "TAG_MAKE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_MODEL, "TAG_MODEL" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_STRIP_OFFSETS, "TAG_STRIP_OFFSETS" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_ORIENTATION, "TAG_ORIENTATION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SAMPLES_PER_PIXEL, "TAG_SAMPLES_PER_PIXEL" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_ROWS_PER_STRIP, "TAG_ROWS_PER_STRIP" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_STRIP_BYTE_COUNTS, "TAG_STRIP_BYTE_COUNTS" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_X_RESOLUTION, "TAG_X_RESOLUTION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_Y_RESOLUTION, "TAG_Y_RESOLUTION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PLANAR_CONFIGURATION, "TAG_PLANAR_CONFIGURATION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_RESOLUTION_UNIT, "TAG_RESOLUTION_UNIT" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_TRANSFER_FUNCTION, "TAG_TRANSFER_FUNCTION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SOFTWARE, "TAG_SOFTWARE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DATE_TIME, "TAG_DATE_TIME" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_ARTIST, "TAG_ARTIST" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_WHITE_POINT, "TAG_WHITE_POINT" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PRIMARY_CHROMATICITIES, "TAG_PRIMARY_CHROMATICITIES" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_Y_CB_CR_COEFFICIENTS, "TAG_Y_CB_CR_COEFFICIENTS" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING, "TAG_Y_CB_CR_SUB_SAMPLING" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_Y_CB_CR_POSITIONING, "TAG_Y_CB_CR_POSITIONING" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_REFERENCE_BLACK_WHITE, "TAG_REFERENCE_BLACK_WHITE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COPYRIGHT, "TAG_COPYRIGHT" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXIF_IFD, "TAG_EXIF_IFD" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_IFD, "TAG_GPS_IFD" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT, "TAG_JPEG_INTERCHANGE_FORMAT" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH	, "TAG_JPEG_INTERCHANGE_FORMAT_LENGTH	" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_TIME, "TAG_EXPOSURE_TIME" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_F_NUMBER, "TAG_F_NUMBER" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_PROGRAM, "TAG_EXPOSURE_PROGRAM" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SPECTRAL_SENSITIVITY, "TAG_SPECTRAL_SENSITIVITY" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_ISO_SPEED_RATINGS, "TAG_ISO_SPEED_RATINGS" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_OECF, "TAG_OECF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXIF_VERSION, "TAG_EXIF_VERSION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DATE_TIME_ORIGINAL, "TAG_DATE_TIME_ORIGINAL" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DATE_TIME_DIGITIZED, "TAG_DATE_TIME_DIGITIZED" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COMPONENTS_CONFIGURATION, "TAG_COMPONENTS_CONFIGURATION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL, "TAG_COMPRESSED_BITS_PER_PIXEL" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SHUTTER_SPEED_VALUE, "TAG_SHUTTER_SPEED_VALUE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_APERTURE_VALUE, "TAG_APERTURE_VALUE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_BRIGHTNESS_VALUE, "TAG_BRIGHTNESS_VALUE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_BIAS_VALUE, "TAG_EXPOSURE_BIAS_VALUE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_MAX_APERTURE_VALUE, "TAG_MAX_APERTURE_VALUE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUBJECT_DISTANCE, "TAG_SUBJECT_DISTANCE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_METERING_MODE, "TAG_METERING_MODE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_LIGHT_SOURCE, "TAG_LIGHT_SOURCE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FLASH, "TAG_FLASH" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_LENGTH, "TAG_FOCAL_LENGTH" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUBJECT_AREA, "TAG_SUBJECT_AREA" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_MAKER_NOTE, "TAG_MAKER_NOTE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_USER_COMMENT, "TAG_USER_COMMENT" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUB_SEC_TIME, "TAG_SUB_SEC_TIME" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUB_SEC_TIME_ORIGINAL, "TAG_SUB_SEC_TIME_ORIGINAL" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUB_SEC_TIME_DIGITIZED, "TAG_SUB_SEC_TIME_DIGITIZED" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FLASHPIX_VERSION, "TAG_FLASHPIX_VERSION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COLOR_SPACE, "TAG_COLOR_SPACE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PIXEL_X_DIMENSION, "TAG_PIXEL_X_DIMENSION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PIXEL_Y_DIMENSION, "TAG_PIXEL_Y_DIMENSION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_RELATED_SOUND_FILE, "TAG_RELATED_SOUND_FILE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_INTEROPERABILITY_IFD, "TAG_INTEROPERABILITY_IFD" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FLASH_ENERGY, "TAG_FLASH_ENERGY" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE, "TAG_SPATIAL_FREQUENCY_RESPONSE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION, "TAG_FOCAL_PLANE_X_RESOLUTION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION, "TAG_FOCAL_PLANE_Y_RESOLUTION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT, "TAG_FOCAL_PLANE_RESOLUTION_UNIT" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUBJECT_LOCATION, "TAG_SUBJECT_LOCATION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_INDEX, "TAG_EXPOSURE_INDEX" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SENSING_METHOD, "TAG_SENSING_METHOD" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FILE_SOURCE, "TAG_FILE_SOURCE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SCENE_TYPE, "TAG_SCENE_TYPE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_CFA_PATTERN, "TAG_CFA_PATTERN" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_CUSTOM_RENDERED, "TAG_CUSTOM_RENDERED" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_MODE, "TAG_EXPOSURE_MODE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_WHITE_BALANCE, "TAG_WHITE_BALANCE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DIGITAL_ZOOM_RATIO, "TAG_DIGITAL_ZOOM_RATIO" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_LENGTH_IN_35_MM_FILE, "TAG_FOCAL_LENGTH_IN_35_MM_FILE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SCENE_CAPTURE_TYPE, "TAG_SCENE_CAPTURE_TYPE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GAIN_CONTROL, "TAG_GAIN_CONTROL" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_CONTRAST, "TAG_CONTRAST" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SATURATION, "TAG_SATURATION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SHARPNESS, "TAG_SHARPNESS" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION, "TAG_DEVICE_SETTING_DESCRIPTION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUBJECT_DISTANCE_RANGE, "TAG_SUBJECT_DISTANCE_RANGE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_IMAGE_UNIQUE_ID, "TAG_IMAGE_UNIQUE_ID" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_VERSION_ID, "TAG_GPS_VERSION_ID" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_LATITUDE_REF, "TAG_GPS_LATITUDE_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_LATITUDE, "TAG_GPS_LATITUDE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_LONGITUDE_REF, "TAG_GPS_LONGITUDE_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_LONGITUDE, "TAG_GPS_LONGITUDE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_ALTITUDE_REF, "TAG_GPS_ALTITUDE_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_ALTITUDE, "TAG_GPS_ALTITUDE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_TIME_STAMP, "TAG_GPS_TIME_STAMP" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_SATTELLITES, "TAG_GPS_SATTELLITES" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_STATUS, "TAG_GPS_STATUS" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_MEASURE_MODE, "TAG_GPS_MEASURE_MODE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DOP, "TAG_GPS_DOP" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_SPEED_REF, "TAG_GPS_SPEED_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_SPEED, "TAG_GPS_SPEED" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_TRACK_REF, "TAG_GPS_TRACK_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_TRACK, "TAG_GPS_TRACK" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_IMG_DIRECTION_REF, "TAG_GPS_IMG_DIRECTION_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_IMG_DIRECTION, "TAG_GPS_IMG_DIRECTION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_MAP_DATUM, "TAG_GPS_MAP_DATUM" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_LATITUDE_REF, "TAG_GPS_DEST_LATITUDE_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_LATITUDE, "TAG_GPS_DEST_LATITUDE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, "TAG_GPS_DEST_LONGITUDE_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_LONGITUDE, "TAG_GPS_DEST_LONGITUDE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_BEARING_REF, "TAG_GPS_DEST_BEARING_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_BEARING, "TAG_GPS_DEST_BEARING" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_DISTANCE_REF, "TAG_GPS_DEST_DISTANCE_REF" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_DISTANCE, "TAG_GPS_DEST_DISTANCE" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_PROCESSING_METHOD, "TAG_GPS_PROCESSING_METHOD" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_AREA_INFORMATION, "TAG_GPS_AREA_INFORMATION" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DATE_STAMP, "TAG_GPS_DATE_STAMP" ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DIFFERENTIAL, "TAG_GPS_DIFFERENTIAL" ) );


			Integer val = mExif.getTagIntValue( ExifInterface.TAG_ORIENTATION );
			short orientation = 0;
			if( null != val ) {
				orientation = ExifInterface.getOrientationValueForRotation( val.shortValue() );
			}

			string.append( "<b>Orientation: </b> " + orientation + "<br>" );

			exifText.setText( Html.fromHtml( string.toString() ) );

			/*

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
			*/
		}
	}

	private String processSharpness( int value ) {
		switch( value ) {
			case 0:
				return "Normal";
			case 1:
				return "Soft";
			case 2:
				return "Hard";
			default:
				return "Unknown";
		}
	}

	private String processContrast( int value ) {
		switch( value ) {
			case 0:
				return "Normal";
			case 1:
				return "Soft";
			case 2:
				return "Hard";
			default:
				return "Unknown";
		}
	}

	private String processSaturation( int value ) {
		switch( value ) {
			case 0:
				return "Normal";
			case 1:
				return "Low Saturation";
			case 2:
				return "High Saturation";
			default:
				return "Unknown";
		}
	}

	private String processGainControl( int value ) {
		switch( value ) {
			case 0:
				return "None";
			case 1:
				return "Low Gain Up";
			case 2:
				return "High Gain Up";
			case 3:
				return "Low Gain Down";
			case 4:
				return "High Gain Down";
			default:
				return "Unknown";
		}
	}


	private String processSceneCaptureType( int value ) {
		switch( value ) {
			case 0:
				return "Standard";
			case 1:
				return "Landscape";
			case 2:
				return "Portrait";
			case 3:
				return "Night scene";
			default:
				return "Unknown";
		}
	}

	private String processSensingMethod( int value ) {
		switch( value ) {
			case 1:
				return "Not defined";
			case 2:
				return "One-chip color area sensor";
			case 3:
				return "Two-chip color area sensor JEITA CP-3451 - 41";
			case 4:
				return "Three-chip color area sensor";
			case 5:
				return "Color sequential area sensor";
			case 7:
				return "Trilinear sensor";
			case 8:
				return "Color sequential linear sensor";
			default:
				return "Unknown";
		}
	}

	private String processColorSpace( int value ) {
		switch( value ) {
			case 1:
				return "sRGB";
			case 0xFFFF:
				return "Uncalibrated";
			default:
				return "Unknown";
		}
	}

	private String processExposureMode( int mode ) {
		switch( mode ) {
			case 0:
				return "Auto exposure";
			case 1:
				return "Manual exposure";
			case 2:
				return "Auto bracket";
			default:
				return "Unknown";
		}
	}

	private String processExposureProgram( int program ) {
		switch( program ) {
			case 1:
				return "Manual control";
			case 2:
				return "Program normal";
			case 3:
				return "Aperture priority";
			case 4:
				return "Shutter priority";
			case 5:
				return "Program creative (slow program)";
			case 6:
				return "Program action(high-speed program)";
			case 7:
				return "Portrait mode";
			case 8:
				return "Landscape mode";
			default:
				return "Unknown";
		}
	}

	private String processMeteringMode( int mode ) {
		switch( mode ) {
			case 1:
				return "Average";
			case 2:
				return "CenterWeightedAverage";
			case 3:
				return "Spot";
			case 4:
				return "MultiSpot";
			case 5:
				return "Pattern";
			case 6:
				return "Partial";
			case 255:
				return "Other";
			default:
				return "Unknown";
		}
	}

	private String processLightSource( int value ) {
		switch( value ) {
			case 0:
				return "Auto";
			case 1:
				return "Daylight";
			case 2:
				return "Fluorescent";
			case 3:
				return "Tungsten (incandescent light)";
			case 4:
				return "Flash";
			case 9:
				return "Fine weather";
			case 10:
				return "Cloudy weather";
			case 11:
				return "Shade";
			case 12:
				return "Daylight fluorescent (D 5700 - 7100K)";
			case 13:
				return "Day white fluorescent (N 4600 - 5400K)";
			case 14:
				return "Cool white fluorescent (W 3900 - 4500K)";
			case 15:
				return "White fluorescent (WW 3200 - 3700K)";
			case 17:
				return "Standard light A";
			case 18:
				return "Standard light B";
			case 19:
				return "Standard light C";
			case 20:
				return "D55";
			case 21:
				return "D65";
			case 22:
				return "D75";
			case 23:
				return "D50";
			case 24:
				return "ISO studio tungsten";
			case 255:
				return "Other light source";
			default:
				return "Unknown";
		}
	}

	private String processWhiteBalance( int value ) {
		switch( value ) {
			case 0:
				return "Auto";
			case 1:
				return "Manual";
			default:
				return "Unknown";
		}
	}

	private String processSubjectDistanceRange( int value ) {
		switch( value ) {
			case 1:
				return "Macro";
			case 2:
				return "Close View";
			case 3:
				return "Distant View";
			default:
				return "Unknown";
		}
	}

	private String processFlash( int flash ) {
		Log.i( LOG_TAG, "flash: " + flash + ", " + ( flash & 1 ) );
		switch( flash ) {
			case 0x0000:
				return "Flash did not fire";
			case 0x0001:
				return "Flash fired";
			case 0x0005:
				return "Strobe return light not detected";
			case 0x0007:
				return "Strobe return light detected";
			case 0x0009:
				return "Flash fired, compulsory flash mode";
			case 0x000D:
				return "Flash fired, compulsory flash mode, return light not detected";
			case 0x000F:
				return "Flash fired, compulsory flash mode, return light detected";
			case 0x0010:
				return "Flash did not fire, compulsory flash mode";
			case 0x0018:
				return "Flash did not fire, auto mode";
			case 0x0019:
				return "Flash fired, auto mode";
			case 0x001D:
				return "Flash fired, auto mode, return light not detected";
			case 0x001F:
				return "Flash fired, auto mode, return light detected";
			case 0x0020:
				return "No flash function";
			case 0x0041:
				return "Flash fired, red-eye reduction mode";
			case 0x0045:
				return "Flash fired, red-eye reduction mode, return light not detected";
			case 0x0047:
				return "Flash fired, red-eye reduction mode, return light detected";
			case 0x0049:
				return "Flash fired, compulsory flash mode, red-eye reduction mode";
			case 0x004D:
				return "Flash fired, compulsory flash mode, red-eye reduction mode, return light not detected";
			case 0x004F:
				return "Flash fired, compulsory flash mode, red-eye reduction mode, return light detected";
			case 0x0059:
				return "Flash fired, auto mode, red-eye reduction mode";
			case 0x005D:
				return "Flash fired, auto mode, return light not detected, red-eye reduction mode";
			case 0x005F:
				return "Flash fired, auto mode, return light detected, red-eye reduction mode";
			default:
				return "Reserved";
		}
	}

	private String parseProcess( int process ) {
		switch( process ) {
			case 192:
				return "Baseline";
			case 193:
				return "Extended sequential";
			case 194:
				return "Progressive";
			case 195:
				return "Lossless";
			case 197:
				return "Differential sequential";
			case 198:
				return "Differential progressive";
			case 199:
				return "Differential lossless";
			case 201:
				return "Extended sequential, arithmetic coding";
			case 202:
				return "Progressive, arithmetic coding";
			case 203:
				return "Lossless, arithmetic coding";
			case 205:
				return "Differential sequential, arithmetic coding";
			case 206:
				return "Differential progressive, arithmetic codng";
			case 207:
				return "Differential lossless, arithmetic coding";
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

		if( id == button1.getId() ) {
			loadImage();
		}
		else if( id == button2.getId() ) {
			try {
				saveImage();
			} catch( Exception e ){
				e.printStackTrace();
				Toast.makeText( this, e.getMessage(), Toast.LENGTH_SHORT ).show();
			}
		}
	}


	private class LoadThumbnailTask extends AsyncTask<ExifInterface, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground( ExifInterface... params ) {

			ExifInterface exif = params[0];

			if( exif.hasThumbnail() ) {
				return exif.getThumbnailBitmap();
			}

			return null;
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
				if( Geocoder.isPresent() ) {
					Geocoder geo = new Geocoder( MainActivity.this );
					result = geo.getFromLocation( lat, lon, 1 );
				}
			} catch( Exception e ) {
				e.printStackTrace();
				return null;
			}

			Log.d( LOG_TAG, "result: " + result );

			if( null != result && result.size() > 0 ) {
				return result.get( 0 );
			}

			return null;
		}

		@Override
		protected void onPostExecute( Address result ) {
			super.onPostExecute( result );

			if( isCancelled() || isFinishing() ) return;

			if( null != result ) {

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
				}
				else {
					if( null != result.getLocality() ) {
						finalString.append( result.getLocality() + "\n" );
					}
				}

				if( null != result.getCountryName() ) {
					finalString.append( result.getCountryName() );
				}
				else if( null != result.getCountryCode() ) {
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
