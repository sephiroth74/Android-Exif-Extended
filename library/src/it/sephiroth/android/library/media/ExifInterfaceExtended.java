package it.sephiroth.android.library.media;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import android.util.Log;

public class ExifInterfaceExtended {

	private static final String LOG_TAG = "ExifInterfaceExtended";
	private static final Object sLock = new Object();
	private static SimpleDateFormat sFormatter;

	// The Exif tag names

	/**
	 * The exposure bias. The unit is the APEX value. Ordinarily it is given in the range of -99.99 to 99.99. Type is float.
	 */
	public static final String TAG_EXPOSURE_BIAS = "ExposureBias";

	/**
	 * The manufacturer of the recording equipment. Type is String.
	 */
	public static final String TAG_CAMERA_MAKE = "CameraMake";

	/**
	 * The model name or model number of the equipment. Type is String.
	 */
	public static final String TAG_CAMERA_MODEL = "CameraModel";

	/**
	 * The date and time of image creation. In this standard it is the date and time the file was changed. The format is
	 * "YYYY:MM:DD HH:MM:SS" with time shown in 24-hour format, Type is String
	 */
	public static final String TAG_DATETIME = "DateTime";

	/** Type is int. */
	public static final String TAG_IMAGE_WIDTH = "Width";

	/** Type is int. */
	public static final String TAG_IMAGE_HEIGHT = "Height";

	/**
	 * The image orientation viewed in terms of rows and columns. Type is int (from 1 to 8).
	 */
	public static final String TAG_ORIENTATION = "Orientation";

	/**
	 * This tag indicates the status of flash when the image was shot. The specification defines these combined values:
	 * <ul>
	 * <li>hex 0000 = Flash did not fire</li>
	 * <li>hex 0001 = Flash fired</li>
	 * <li>hex 0005 = Strobe return light not detected</li>
	 * <li>hex 0007 = Strobe return light detected</li>
	 * <li>hex 0009 = Flash fired, compulsory flash mode</li>
	 * <li>hex 000D = Flash fired, compulsory flash mode, return light not detected</li>
	 * <li>hex 000F = Flash fired, compulsory flash mode, return light detected</li>
	 * <li>hex 0010 = Flash did not fire, compulsory flash mode</li>
	 * <li>hex 0018 = Flash did not fire, auto mode</li>
	 * <li>hex 0019 = Flash fired, auto mode</li>
	 * <li>hex 001D = Flash fired, auto mode, return light not detected</li>
	 * <li>hex 001F = Flash fired, auto mode, return light detected</li>
	 * <li>hex 0020 = No flash function</li>
	 * <li>hex 0041 = Flash fired, red-eye reduction mode</li>
	 * <li>hex 0045 = Flash fired, red-eye reduction mode, return light not detected</li>
	 * <li>hex 0047 = Flash fired, red-eye reduction mode, return light detected</li>
	 * <li>hex 0049 = Flash fired, compulsory flash mode, red-eye reduction mode</li>
	 * <li>hex 004D = Flash fired, compulsory flash mode, red-eye reduction mode, return light not detected</li>
	 * <li>hex 004F = Flash fired, compulsory flash mode, red-eye reduction mode, return light detected</li>
	 * <li>hex 0059 = Flash fired, auto mode, red-eye reduction mode</li>
	 * <li>hex 005D = Flash fired, auto mode, return light not detected, red-eye reduction mode</li>
	 * <li>hex 005F = Flash fired, auto mode, return light detected, red-eye reduction mode</li>
	 * </ul>
	 * Type is int.
	 * 
	 * @see <a href="http://www.exif.org/Exif2-2.PDF">http://www.exif.org/Exif2-2.PDF</a>
	 */
	public static final String TAG_FLASH = "FlashUsed";

	/**
	 * Type is float.<br />
	 * The actual focal length of the lens, in mm. Conversion is not made to the focal length of a 35 mm film camera
	 */
	public static final String TAG_FOCAL_LENGTH = "FocalLength";

	/**
	 * Type is float.<br />
	 * This tag indicates the equivalent focal length assuming a 35mm film camera, in mm.<br />
	 * A value of 0 means the focal length is unknown.<br />
	 * Note that this tag differs from the {@link #TAG_FOCAL_LENGTH} tag.
	 */
	public static final String TAG_FOCAL_LENGTH_35_MM = "FocalLength35mmEquiv";

	/**
	 * Type is float.<br />
	 * Exposure time, given in seconds (sec).
	 */
	public static final String TAG_EXPOSURE_TIME = "ExposureTime";

	/**
	 * Type is float.<br />
	 * The actual F-number(F-stop) of lens when the image was taken.
	 */
	public static final String TAG_APERTURE = "ApertureFNumber";

	/**
	 * Type is float.<br />
	 * The distance to the subject, given in meters. Note that if the numerator of the recorded value is
	 * FFFFFFFF.H, Infinity shall be indicated; and if the numerator is 0, Distance unknown shall be indicated.
	 */
	public static final String TAG_DISTANCE = "Distance";

	/**
	 * Type is int.<br />
	 * This tag indicates the distance to the subject.
	 */
	public static final String TAG_DISTANCE_RANGE = "DistanceRange";

	/**
	 * Type is int.<br />
	 * Indicates the ISO Speed and ISO Latitude of the camera or input device as specified in ISO 12232
	 */
	public static final String TAG_ISO_EQUIVALENT = "ISOequivalent";

	/**
	 * Type is int.<br />
	 */
	public static final String TAG_WHITE_BALANCE = "Whitebalance";

	/**
	 * Type is int.<br />
	 * The kind of light source.
	 */
	public static final String TAG_LIGHTSOURCE = "LightSource";

	/**
	 * Type is int.<br />
	 * The metering mode.
	 */
	public static final String TAG_METERING_MODE = "MeteringMode";

	/**
	 * Type is int.<br />
	 * The class of the program used by the camera to set exposure when the picture is taken
	 */
	public static final String TAG_EXPOSURE_PROGRAM = "ExposureProgram";

	/**
	 * Type is int.<br />
	 * This tag indicates the exposure mode set when the image was shot.
	 */
	public static final String TAG_EXPOSURE_MODE = "ExposureMode";

	/**
	 * Type is float.<br />
	 * This tag indicates the digital zoom ratio when the image was shot. If the numerator of the recorded value is 0, this indicates
	 * that digital zoom was not used.
	 */
	public static final String TAG_DIGITAL_ZOOM = "DigitalZoomRatio";

	/**
	 * Type is string.<br />
	 * Copyright informations
	 */
	public static final String TAG_COPYRIGHT = "Copyright";

	/**
	 * Type is string.<br />
	 * This tag records the name and version of the software or firmware of the camera or image input device used to generate the
	 * image
	 */
	public static final String TAG_SOFTWARE = "Software";

	/**
	 * Type is stringb<br />
	 * This tag records the name of the camera owner, photographer or image creator
	 */
	public static final String TAG_ARTIST = "Artist";

	/**
	 * A tag for Exif users to write keywords or comments on the image besides those in ImageDescription. Type is String
	 */
	public static final String TAG_COMMENT = "Comments";
	
	/**
	 * Type is int.<br />
	 * Jpeg image quality (guess)
	 */
	public static final String TAG_JPEG_QUALITY = "QualityGuess";

	/**
	 * Type is string.<br />
	 * The latitude is expressed as degrees, minutes, and seconds, respectively.
	 * Example: N 40d 43m  1.4712s
	 */
	public static final String TAG_GPS_LATITUDE = "GpsLat";

	/**
	 * Type is string.<br />
	 * The longitude is expressed as degrees, minutes, and seconds, respectively.
	 * Example: W 73d 57m 20.4235s
	 */
	public static final String TAG_GPS_LONGITUDE = "GpsLong";

	/**
	 * Type is string.<br />
	 * The altitude (in meters)<br />
	 * Example: -6.50m

	 */
	public static final String TAG_GPS_ALTITUDE = "GpsAlt";

	/**
	 * A character string recording date and time information relative to UTC (Coordinated Universal Time). The format is
	 * "YYYY:MM:DD." Type is rational.
	 */
	public static final String TAG_GPS_DATESTAMP = "GPSDateStamp";

	/**
	 * Type is int.<br />
	 * The unit for measuring XResolution and YResolution. The same unit is used for both XResolution and YResolution. If the image
	 * resolution in unknown, 2 (inches) is designated
	 */
	public static final String TAG_RESOLUTION_UNIT = "ResolutionUnit";

	/**
	 * Type is float.<br />
	 * The number of pixels per ResolutionUnit in the ImageWidth direction. When the image resolution is unknown, 72 [dpi] is
	 * designated
	 */
	public static final String TAG_X_RESOLUTION = "xResolution";

	/**
	 * Type is float.<br />
	 * The number of pixels per ResolutionUnit in the ImageWidth direction. When the image resolution is unknown, 72 [dpi] is
	 * designated.
	 */
	public static final String TAG_Y_RESOLUTION = "yResolution";
	
	/**
	 * Type is int.<br />
	 * Indicates the type of scene that was shot.<br
	 * <ul>
	 * <li>0 = Standard</li>
	 * <li>1 = Landscape</li>
	 * <li>2 = Portrait</li>
	 * <li>3 = Night scene</li>
	 * </ul>
	 */
	public static final String TAG_SCENE_CAPTURE_TYPE = "SceneCaptureType";
	

	/**
	 * Hidden tag for file date time getter/setter
	 */
	public static final String TAG_FILEDATETIME = "FileDateTime";

	private HashMap<String, String> mAttributes;

	private boolean mHasThumbnail;

	private String mFilename;

	static {
		System.loadLibrary( "exif_extended" );
		sFormatter = new SimpleDateFormat( "yyyy:MM:dd HH:mm:ss", Locale.US );
		sFormatter.setTimeZone( TimeZone.getDefault() );
	}

	public ExifInterfaceExtended( String filename ) throws IOException {
		Log.i( LOG_TAG, "filename: " + filename );
		mFilename = filename;
		loadAttributes();
	}

	/**
	 * Returns a set containing all the attributes available
	 * 
	 * @return
	 */
	public Set<String> keySet() {
		return new HashSet<String>( mAttributes.keySet() );
	}

	/**
	 * Returns the value of the specified tag or {@code null} if there is no such tag in the JPEG file.
	 * 
	 * @param tag
	 *           the name of the tag.
	 */
	public String getAttribute( String tag ) {
		return mAttributes.get( tag );
	}

	private void loadAttributes() throws IOException {
		Log.i( LOG_TAG, "loadAttributes" );

		mAttributes = new HashMap<String, String>();

		String attrStr;
		synchronized ( sLock ) {
			attrStr = getAttributesNative( mFilename );
		}

		int ptr = attrStr.indexOf( ' ' );
		int count = Integer.parseInt( attrStr.substring( 0, ptr ) );
		++ptr;

		for ( int i = 0; i < count; i++ ) {
			int equalPos = attrStr.indexOf( '=', ptr );
			String attrName = attrStr.substring( ptr, equalPos );
			ptr = equalPos + 1; // skip past =

			// extract the attribute value length
			int lenPos = attrStr.indexOf( ' ', ptr );
			int attrLen = Integer.parseInt( attrStr.substring( ptr, lenPos ) );
			ptr = lenPos + 1; // skip pas the space

			// extract the attribute value
			String attrValue = attrStr.substring( ptr, ptr + attrLen );
			ptr += attrLen;

			if ( attrName.equals( "hasThumbnail" ) ) {
				mHasThumbnail = attrValue.equalsIgnoreCase( "true" );
			} else {
				mAttributes.put( attrName, attrValue );
			}
		}
	}

	public void saveAttributes() throws IOException {
		Log.i( LOG_TAG, "saveAttributes" );

		StringBuilder sb = new StringBuilder();
		int size = mAttributes.size();
		if ( mAttributes.containsKey( "hasThumbnail" ) ) {
			--size;
		}
		sb.append( size + " " );
		for ( Map.Entry<String, String> iter : mAttributes.entrySet() ) {
			String key = iter.getKey();
			if ( key.equals( "hasThumbnail" ) ) {
				// this is a fake attribute not saved as an exif tag
				continue;
			}
			String val = iter.getValue();
			sb.append( key + "=" );
			sb.append( val.length() + " " );
			sb.append( val );
		}
		String s = sb.toString();
		synchronized ( sLock ) {
			saveAttributesNative( mFilename, s );
			commitChangesNative( mFilename );
		}
	}

	public byte[] getThumbnail() {
		Log.i( LOG_TAG, "getThumbnail" );
		synchronized ( sLock ) {
			return getThumbnailNative( mFilename );
		}
	}

	private native boolean appendThumbnailNative( String fileName, String thumbnailFileName );

	private native void saveAttributesNative( String fileName, String compressedAttributes );

	private native String getAttributesNative( String fileName );

	private native void commitChangesNative( String fileName );

	private native byte[] getThumbnailNative( String fileName );
}
