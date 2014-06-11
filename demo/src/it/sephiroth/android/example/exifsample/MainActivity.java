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
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import it.sephiroth.android.example.exifsample.utils.IOUtils;
import it.sephiroth.android.library.exif2.BuildConfig;
import it.sephiroth.android.library.exif2.ExifInterface;
import it.sephiroth.android.library.exif2.ExifTag;
import it.sephiroth.android.library.exif2.ExifUtil;
import it.sephiroth.android.library.exif2.IfdId;
import it.sephiroth.android.library.exif2.Rational;

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

		Log.i( LOG_TAG, "ExifInterfaceExtended.Version: " + BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE );
		processAsset( "image.jpg" );
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

			File dst_file = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "exif.jpg" );

			if( null != mUri ) {

				ExifInterface exif = new ExifInterface();
				exif.setTags( mExif.getAllTags() );
				exif.setCompressedThumbnail( mExif.getThumbnail() );

				exif.deleteTag( ExifInterface.TAG_COLOR_SPACE );
				exif.deleteTag( ExifInterface.TAG_ORIENTATION );

				String uuid = UUID.randomUUID().toString();

				if( uuid.length() > 32 ) {
					uuid = uuid.substring( 0, 32 );
				} else if( uuid.length() < 32 ) {
					uuid = StringUtils.leftPad( uuid, 32 );
				}

				exif.setTag( exif.buildTag( ExifInterface.TAG_IMAGE_UNIQUE_ID, uuid ) );
				exif.setTag( exif.buildTag( ExifInterface.TAG_ARTIST, "Alessandro Crugnola" ) );
				exif.addDateTimeStampTag( ExifInterface.TAG_DATE_TIME, new Date().getTime(), TimeZone.getDefault() );

				InputStream is = openInputStream( mUri );
				OutputStream os = new FileOutputStream( dst_file );
				org.apache.commons.io.IOUtils.copy( is, os );
				is.close();
				os.close();

				long t1 = SystemClock.uptimeMillis();

				// exif.writeExif( tmp_file.getAbsolutePath(), dst_file.getAbsolutePath() );
				// exif.writeExif( openInputStream( mUri ), dst_file.getAbsolutePath() );

				exif.writeExif( dst_file.getAbsolutePath() );

				long t2 = SystemClock.uptimeMillis();
				Log.d( LOG_TAG, "saveImage time: " + (t2-t1) + "ms" );

			}

		}
	}

	private void dumpToFile( ExifInterface exif ) {
		if( null == exif ) return;

		try {
			File file = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "exif.txt" );
			Log.d( LOG_TAG, "writing to " + file.getAbsolutePath() );

			FileOutputStream stream = new FileOutputStream( file );
			List<ExifTag> tags = exif.getAllTags();
			if( null == tags ) return;

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

	private void processUri( Uri uri ) {
		mUri = uri;
		try {
			processInputStream( openInputStream( uri ) );
		} catch( FileNotFoundException e ) {
			e.printStackTrace();
		}
	}

	private InputStream openInputStream( Uri uri ) throws FileNotFoundException {
		String filename = IOUtils.getRealFilePath( this, uri );
		if( null == filename ) {
			return getContentResolver().openInputStream( uri );
		}
		else {
			return new FileInputStream( filename );
		}
	}

	int mTagsCount = 0;

	private String createStringFromIfFound( ExifInterface exif, int key, String label, final List<ExifTag> all_tags ) {
		String exifString = "";
		ExifTag tag = exif.getTag( key );
		if( null != tag ) {

			all_tags.remove( tag );

			int ifid = tag.getIfd();
			String ifdid_str = "";

			switch( ifid ) {
				case IfdId.TYPE_IFD_0:
					ifdid_str = "ifd0";
					break;

				case IfdId.TYPE_IFD_1:
					ifdid_str = "ifd1";
					break;

				case IfdId.TYPE_IFD_EXIF:
					ifdid_str = "exif";
					break;

				case IfdId.TYPE_IFD_GPS:
					ifdid_str = "gps";
					break;

				case IfdId.TYPE_IFD_INTEROPERABILITY:
					ifdid_str = "interop";
					break;
			}

			mTagsCount++;
			exifString += "<b>" + label + "(" + ifdid_str + "): </b>";

			if( key == ExifInterface.TAG_DATE_TIME || key == ExifInterface.TAG_DATE_TIME_DIGITIZED || key == ExifInterface.TAG_DATE_TIME_ORIGINAL ) {
				Date date = ExifInterface.getDateTime( tag.getValueAsString(), TimeZone.getDefault() );
				if( null != date ) {
					exifString += java.text.DateFormat.getDateTimeInstance().format( date );
				}
				else {
					Log.e( LOG_TAG, "failed to format the date" );
				}
			} else {
				exifString += tag.forceGetValueAsString();
			}
			exifString += "<br>";
		}
		else {
//			Log.w( LOG_TAG, "'" + label + "' not found" );
		}
		return exifString;
	}



	private void processAsset( String assetname ) {
		InputStream stream;
		try {
			stream = getAssets().open( assetname );
		} catch( IOException e ) {
			e.printStackTrace();
			return;
		}
		processInputStream( stream );
	}

	private void processInputStream( InputStream stream ) {

		mExif = new ExifInterface();
		List<ExifTag> all_tags = null;

		if( null != stream ) {
			long t1 = System.currentTimeMillis();
			try {
				mExif.readExif( stream, ExifInterface.Options.OPTION_ALL );
			} catch( IOException e ) {
				e.printStackTrace();
				mExif = null;
			}
			long t2 = System.currentTimeMillis();
			Log.d( LOG_TAG, "parser time: " + ( t2 - t1 ) + "ms" );

			if(null != mExif) {
				all_tags = mExif.getAllTags();
				Log.d(LOG_TAG, "total tags: " + (all_tags != null ? all_tags.size() : 0));
			}
		}

		image.setImageBitmap( null );
		exifText.setText( "" );

		if( null != mExif ) {
			mTagsCount = 0;
			StringBuilder string = new StringBuilder();
			NumberFormat numberFormatter = DecimalFormat.getNumberInstance();

			// dumpToFile( mExif );
			exifText.setText( "<h2>JPEG Info<h2><br/>" );

			new LoadThumbnailTask().execute( mExif );

			if( mExif.getQualityGuess() > 0 ) {
				string.append( "<b>JPEG quality:</b> " + mExif.getQualityGuess() + "<br>" );
			}

			int[] imagesize = mExif.getImageSize();
			if( imagesize[0] > 0 && imagesize[1] > 0 ) {
				string.append( "<b>Image Size: </b>" + imagesize[0] + "x" + imagesize[1] + "<br>" );
			}

			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_IMAGE_WIDTH, "TAG_IMAGE_WIDTH", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_IMAGE_LENGTH, "TAG_IMAGE_LENGTH", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_BITS_PER_SAMPLE, "TAG_BITS_PER_SAMPLE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COMPRESSION, "TAG_COMPRESSION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION, "TAG_PHOTOMETRIC_INTERPRETATION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_IMAGE_DESCRIPTION, "TAG_IMAGE_DESCRIPTION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_MAKE, "TAG_MAKE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_MODEL, "TAG_MODEL", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_STRIP_OFFSETS, "TAG_STRIP_OFFSETS", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_ORIENTATION, "TAG_ORIENTATION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SAMPLES_PER_PIXEL, "TAG_SAMPLES_PER_PIXEL", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_ROWS_PER_STRIP, "TAG_ROWS_PER_STRIP", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_STRIP_BYTE_COUNTS, "TAG_STRIP_BYTE_COUNTS", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_X_RESOLUTION, "TAG_X_RESOLUTION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_Y_RESOLUTION, "TAG_Y_RESOLUTION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PLANAR_CONFIGURATION, "TAG_PLANAR_CONFIGURATION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_RESOLUTION_UNIT, "TAG_RESOLUTION_UNIT", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_TRANSFER_FUNCTION, "TAG_TRANSFER_FUNCTION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SOFTWARE, "TAG_SOFTWARE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DATE_TIME, "TAG_DATE_TIME", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_ARTIST, "TAG_ARTIST", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_WHITE_POINT, "TAG_WHITE_POINT", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PRIMARY_CHROMATICITIES, "TAG_PRIMARY_CHROMATICITIES", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_Y_CB_CR_COEFFICIENTS, "TAG_Y_CB_CR_COEFFICIENTS", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING, "TAG_Y_CB_CR_SUB_SAMPLING", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_Y_CB_CR_POSITIONING, "TAG_Y_CB_CR_POSITIONING", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_REFERENCE_BLACK_WHITE, "TAG_REFERENCE_BLACK_WHITE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COPYRIGHT, "TAG_COPYRIGHT", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXIF_IFD, "TAG_EXIF_IFD", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_IFD, "TAG_GPS_IFD", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT, "TAG_JPEG_INTERCHANGE_FORMAT", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, "TAG_JPEG_INTERCHANGE_FORMAT_LENGTH", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_TIME, "TAG_EXPOSURE_TIME", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_F_NUMBER, "TAG_F_NUMBER", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_PROGRAM, "TAG_EXPOSURE_PROGRAM", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SPECTRAL_SENSITIVITY, "TAG_SPECTRAL_SENSITIVITY", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_ISO_SPEED_RATINGS, "TAG_ISO_SPEED_RATINGS", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_OECF, "TAG_OECF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXIF_VERSION, "TAG_EXIF_VERSION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DATE_TIME_ORIGINAL, "TAG_DATE_TIME_ORIGINAL", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DATE_TIME_DIGITIZED, "TAG_DATE_TIME_DIGITIZED", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COMPONENTS_CONFIGURATION, "TAG_COMPONENTS_CONFIGURATION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL, "TAG_COMPRESSED_BITS_PER_PIXEL", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SHUTTER_SPEED_VALUE, "TAG_SHUTTER_SPEED_VALUE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_APERTURE_VALUE, "TAG_APERTURE_VALUE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_BRIGHTNESS_VALUE, "TAG_BRIGHTNESS_VALUE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_BIAS_VALUE, "TAG_EXPOSURE_BIAS_VALUE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_MAX_APERTURE_VALUE, "TAG_MAX_APERTURE_VALUE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUBJECT_DISTANCE, "TAG_SUBJECT_DISTANCE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_METERING_MODE, "TAG_METERING_MODE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_LIGHT_SOURCE, "TAG_LIGHT_SOURCE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FLASH, "TAG_FLASH", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_LENGTH, "TAG_FOCAL_LENGTH", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUBJECT_AREA, "TAG_SUBJECT_AREA", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_MAKER_NOTE, "TAG_MAKER_NOTE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_USER_COMMENT, "TAG_USER_COMMENT", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUB_SEC_TIME, "TAG_SUB_SEC_TIME", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUB_SEC_TIME_ORIGINAL, "TAG_SUB_SEC_TIME_ORIGINAL", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUB_SEC_TIME_DIGITIZED, "TAG_SUB_SEC_TIME_DIGITIZED", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FLASHPIX_VERSION, "TAG_FLASHPIX_VERSION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_COLOR_SPACE, "TAG_COLOR_SPACE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PIXEL_X_DIMENSION, "TAG_PIXEL_X_DIMENSION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_PIXEL_Y_DIMENSION, "TAG_PIXEL_Y_DIMENSION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_RELATED_SOUND_FILE, "TAG_RELATED_SOUND_FILE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_INTEROPERABILITY_IFD, "TAG_INTEROPERABILITY_IFD", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FLASH_ENERGY, "TAG_FLASH_ENERGY", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE, "TAG_SPATIAL_FREQUENCY_RESPONSE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION, "TAG_FOCAL_PLANE_X_RESOLUTION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION, "TAG_FOCAL_PLANE_Y_RESOLUTION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT, "TAG_FOCAL_PLANE_RESOLUTION_UNIT", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUBJECT_LOCATION, "TAG_SUBJECT_LOCATION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_INDEX, "TAG_EXPOSURE_INDEX", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SENSING_METHOD, "TAG_SENSING_METHOD", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FILE_SOURCE, "TAG_FILE_SOURCE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SCENE_TYPE, "TAG_SCENE_TYPE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_CFA_PATTERN, "TAG_CFA_PATTERN", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_CUSTOM_RENDERED, "TAG_CUSTOM_RENDERED", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_EXPOSURE_MODE, "TAG_EXPOSURE_MODE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_WHITE_BALANCE, "TAG_WHITE_BALANCE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DIGITAL_ZOOM_RATIO, "TAG_DIGITAL_ZOOM_RATIO", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_FOCAL_LENGTH_IN_35_MM_FILE, "TAG_FOCAL_LENGTH_IN_35_MM_FILE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SCENE_CAPTURE_TYPE, "TAG_SCENE_CAPTURE_TYPE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GAIN_CONTROL, "TAG_GAIN_CONTROL", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_CONTRAST, "TAG_CONTRAST", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SATURATION, "TAG_SATURATION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SHARPNESS, "TAG_SHARPNESS", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION, "TAG_DEVICE_SETTING_DESCRIPTION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SUBJECT_DISTANCE_RANGE, "TAG_SUBJECT_DISTANCE_RANGE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_IMAGE_UNIQUE_ID, "TAG_IMAGE_UNIQUE_ID", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_VERSION_ID, "TAG_GPS_VERSION_ID", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_LATITUDE_REF, "TAG_GPS_LATITUDE_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_LATITUDE, "TAG_GPS_LATITUDE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_LONGITUDE_REF, "TAG_GPS_LONGITUDE_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_LONGITUDE, "TAG_GPS_LONGITUDE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_ALTITUDE_REF, "TAG_GPS_ALTITUDE_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_ALTITUDE, "TAG_GPS_ALTITUDE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_TIME_STAMP, "TAG_GPS_TIME_STAMP", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_SATTELLITES, "TAG_GPS_SATTELLITES", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_STATUS, "TAG_GPS_STATUS", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_MEASURE_MODE, "TAG_GPS_MEASURE_MODE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DOP, "TAG_GPS_DOP", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_SPEED_REF, "TAG_GPS_SPEED_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_SPEED, "TAG_GPS_SPEED", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_TRACK_REF, "TAG_GPS_TRACK_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_TRACK, "TAG_GPS_TRACK", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_IMG_DIRECTION_REF, "TAG_GPS_IMG_DIRECTION_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_IMG_DIRECTION, "TAG_GPS_IMG_DIRECTION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_MAP_DATUM, "TAG_GPS_MAP_DATUM", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_LATITUDE_REF, "TAG_GPS_DEST_LATITUDE_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_LATITUDE, "TAG_GPS_DEST_LATITUDE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, "TAG_GPS_DEST_LONGITUDE_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_LONGITUDE, "TAG_GPS_DEST_LONGITUDE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_BEARING_REF, "TAG_GPS_DEST_BEARING_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_BEARING, "TAG_GPS_DEST_BEARING", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_DISTANCE_REF, "TAG_GPS_DEST_DISTANCE_REF", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DEST_DISTANCE, "TAG_GPS_DEST_DISTANCE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_PROCESSING_METHOD, "TAG_GPS_PROCESSING_METHOD", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_AREA_INFORMATION, "TAG_GPS_AREA_INFORMATION", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DATE_STAMP, "TAG_GPS_DATE_STAMP", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_GPS_DIFFERENTIAL, "TAG_GPS_DIFFERENTIAL", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_INTEROPERABILITY_INDEX, "TAG_INTEROPERABILITY_INDEX", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_LENS_MAKE, "TAG_LENS_MAKE", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_LENS_MODEL, "TAG_LENS_MODEL", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_LENS_SPECS, "TAG_LENS_SPECS", all_tags ) );
			string.append( createStringFromIfFound( mExif, ExifInterface.TAG_SENSITIVITY_TYPE, "TAG_SENSITIVITY_TYPE", all_tags ) );

			List<ExifTag> tags = mExif.getTagsForTagId( mExif.getTrueTagKey( ExifInterface.TAG_ORIENTATION ) );
			Log.d( LOG_TAG, "tags: " + tags );

			string.append( "<br>--------------<br>" );
			string.append( "<b>Total tags parsed:</b> " + mTagsCount + "<br>" );
			string.append( "<b>Remaining tags:</b> " + ( all_tags != null ? all_tags.size() : 0 ) + "<br>" );
			string.append( "<b>Has Thumbnail:</b> " + mExif.hasThumbnail() + "<br>" );


			ExifTag tag = mExif.getTag( ExifInterface.TAG_EXIF_VERSION );
			if( null != tag ) {
				string.append( "<b>Exif version: </b> " + tag.getValueAsString() + "<br>" );
			}

			String latitude = mExif.getLatitude();
			String longitude = mExif.getLongitude();

			if( null != latitude && null != longitude ) {
				string.append( "<b>Latitude: </b> " + latitude + "<br>" );
				string.append( "<b>Longitude: </b> " + longitude + "<br>" );
			}

			Integer val = mExif.getTagIntValue( ExifInterface.TAG_ORIENTATION );
			int orientation = 0;
			if( null != val ) {
				orientation = ExifInterface.getRotationForOrientationValue( val.shortValue() );
			}
			string.append( "<b>Orientation: </b> " + orientation + "<br>" );


			double aperture = mExif.getApertureSize();
			if( aperture > 0 ) {
				string.append( "<b>Aperture Size: </b> " + String.format( "%.2f", aperture ) + "<br>" );
			}

			ExifTag shutterSpeed = mExif.getTag( ExifInterface.TAG_SHUTTER_SPEED_VALUE );
			if( null != shutterSpeed ) {
				double speed = shutterSpeed.getValueAsRational( 0 ).toDouble();
				Log.d( LOG_TAG, "speed: " + speed );

				NumberFormat decimalFormatter = DecimalFormat.getNumberInstance();
				decimalFormatter.setMaximumFractionDigits( 1 );
				String speedString = "1/" + decimalFormatter.format( Math.pow( 2, speed ) ) + "s";
				string.append( "<b>Shutter Speed: </b> " + speedString + "<br>" );
			}

			String lensModel = mExif.getLensModelDescription();
			if( null != lensModel ) {
				string.append( "<b>Lens Specifications: </b> " + lensModel + "<br>" );
			}


			short process = mExif.getJpegProcess();
			string.append( "<b>JPEG Process: </b> " + process + "<br>" );
			exifText.setText( Html.fromHtml( string.toString() ) );

			if( null != all_tags ) {
				Log.i( LOG_TAG, "---- remaining tags ---" );
				for( ExifTag remaining_tag : all_tags ) {
					Log.v( LOG_TAG, "tag: " + String.format( "0x%X", remaining_tag.getTagId() ) + ", value: " + remaining_tag.forceGetValueAsString() );
				}
			}

			double[] latlon = mExif.getLatLongAsDoubles();
			if( null != latlon ) {
				GetGeoLocationTask task = new GetGeoLocationTask();
				task.execute( latlon[0], latlon[1] );
			}
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
				processUri( data.getData() );
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
			} catch( Exception e ) {
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

	private class GetGeoLocationTask extends AsyncTask<Double, Void, Address> {

		@Override
		protected Address doInBackground( Double... params ) {

			double lat = params[0];
			double lon = params[1];

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
