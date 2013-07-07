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
	 * int<br />
	 * The file size in bytes
	 */
	public static final String TAG_FILE_SIZE = "FileSize";
	
   /**
    * ASCII string<br />
    * The manufacturer of the recording equipment. This is the manufacturer of the DSC, scanner, video digitizer or other
    * equipment that generated the image. When the field is left blank, it is treated as unknown.
    */
	public static final String TAG_MAKE = "Make";
	
   /**
    * ASCII string<br />
    * The model name or model number of the equipment. This is the model name of number of the DSC, scanner, video
    * digitizer or other equipment that generated the image. When the field is left blank, it is treated as unknown.
    */
	public static final String TAG_MODEL = "Model";
	
	/**
	 * ASCII string (20)<br />
	 * Date/Time of image was last modified. Data format is "YYYY:MM:DD HH:MM:SS"+0x00, total 20bytes.
	 * In usual, it has the same value of DateTimeOriginal(0x9003)
	 */
	public static final String TAG_DATETIME = "DateTime";
	
   /**
    * ASCII string (20)<br />
    * Date/Time of image digitized. Usually, it contains the same value of DateTimeOriginal(0x9003).
    */
   public static final String TAG_DATETIME_DIGITIZED = "DateTimeDigitized";	
	
   /**
    * ASCII string (20)<br />
    * Date/Time of original image taken. This value should not be modified by user program.
    */
   public static final String TAG_DATETIME_ORIGINAL = "DateTimeOriginal";
   
   /**
    * ASCII string<br />
    * Shows copyright information
    */
   public static final String TAG_COPYRIGHT = "Copyright";
   
   /**
    * ASCII String<br />
    * This tag records the name of the camera owner, photographer or image creator. The detailed format is not specified,
    * but it is recommended that the information be written as in the example below for ease of Interoperability. When the
    * field is left blank, it is treated as unknown.
    */
   public static final String TAG_ARTIST = "Artist";
   
   /**
    * ASCII string<br />
    * Shows firmware(internal software of digicam) version number.
    */
   public static final String TAG_SOFTWARE = "Software";
   
   /**
    * int<br />
    * The number of columns of image data, equal to the number of pixels per row. In JPEG compressed data a JPEG  marker is used instead of this tag.
    */
   public static final String TAG_IMAGE_WIDTH = "ImageWidth";

   /**
    * int<br />
    * The number of rows of image data. In JPEG compressed data a JPEG marker is used instead of this tag.
    */
   public static final String TAG_IMAGE_HEIGHT = "ImageHeight";
	
   /**
    * int<br />
    * The orientation of the camera relative to the scene, when the image was captured. The start point of stored data is:
    * <ul>
    * <li>'0' undefined</li>
    * <li>'1' normal</li>
    * <li>'2' flip horizontal</li>
    * <li>'3' rotate 180</li>
    * <li>'4' flip vertical</li>
    * <li>'5' transpose, flipped about top-left <--> bottom-right axis</li>
    * <li>'6' rotate 90 cw</li>
    * <li>'7' transverse, flipped about top-right <--> bottom-left axis</li>
    * <li>'8' rotate 270</li>
    * <li>'9' undefined</li>
    * </ul>
    */
   public static final String TAG_ORIENTATION = "Orientation";
   
   /**
    * int<br />
    * Not an exif tag, this is extracted from the jpeg file.
    * It gives information about the process used to create the JPEG file.
    * Possible values are:
    * <ul>
    * <li>'192' Baseline</li>
    * <li>'193' Extended sequential</li>
    * <li>'194' Progressive</li>
    * <li>'195' Lossless</li>
    * <li>'197' Differential sequential</li>
    * <li>'198' Differential progressive</li>
    * <li>'199' Differential lossless</li>
    * <li>'201' Extended sequential, arithmetic coding</li>
    * <li>'202' Progressive, arithmetic coding</li>
    * <li>'203' Lossless, arithmetic coding</li>
    * <li>'205' Differential sequential, arithmetic coding</li>
    * <li>'206' Differential progressive, arithmetic codng</li>
    * <li>'207' Differential lossless, arithmetic coding</li>
    * </ul>
    */
	public static final String TAG_PROCESS = "Process";
	
   /**
    * Value is unsigned integer<br />
    * The 8 bits can be extracted and evaluated in this way:<br />
    * <ol>
    * <li>Bit 0 indicates the flash firing status</li>
    * <li>bits 1 and 2 indicate the flash return status</li>
    * <li>bits 3 and 4 indicate the flash mode</li>
    * <li>bit 5 indicates whether the flash function is present</li>
    * <li>and bit 6 indicates "red eye" mode</li>
    * <li>bit 7 unused</li>
    * </ol>
    *
    * Resulting Flash tag values are:<br />
    * <ul>
    * <li>0000.H = Flash did not fire</li>
    * <li>0001.H = Flash fired</li>
    * <li>0005.H = Strobe return light not detected</li>
    * <li>0007.H = Strobe return light detected</li>
    * <li>0009.H = Flash fired, compulsory flash mode</li>
    * <li>000D.H = Flash fired, compulsory flash mode, return light not detected</li>
    * <li>000F.H = Flash fired, compulsory flash mode, return light detected</li>
    * <li>0010.H = Flash did not fire, compulsory flash mode</li>
    * <li>0018.H = Flash did not fire, auto mode</li>
    * <li>0019.H = Flash fired, auto mode</li>
    * <li>001D.H = Flash fired, auto mode, return light not detected</li>
    * <li>001F.H = Flash fired, auto mode, return light detected</li>
    * <li>0020.H = No flash function</li>
    * <li>0041.H = Flash fired, red-eye reduction mode</li>
    * <li>0045.H = Flash fired, red-eye reduction mode, return light not detected</li>
    * <li>0047.H = Flash fired, red-eye reduction mode, return light detected</li>
    * <li>0049.H = Flash fired, compulsory flash mode, red-eye reduction mode</li>
    * <li>004D.H = Flash fired, compulsory flash mode, red-eye reduction mode, return light not detected</li>
    * <li>004F.H = Flash fired, compulsory flash mode, red-eye reduction mode, return light detected</li>
    * <li>0059.H = Flash fired, auto mode, red-eye reduction mode</li>
    * <li>005D.H = Flash fired, auto mode, return light not detected, red-eye reduction mode</li>
    * <li>005F.H = Flash fired, auto mode, return light detected, red-eye reduction mode</li>
    * <li>Other = reserved</li>
    * </ul>
    * 
    * @see <a href="http://www.exif.org/Exif2-2.PDF">http://www.exif.org/Exif2-2.PDF</a>
    */
   public static final String TAG_FLASH = "Flash";	
	
   /**
    * Value is unsigned double<br />
    * Focal length of lens used to take image. Unit is millimeter.
    */
   public static final String TAG_FOCAL_LENGHT = "FocalLength";
   
   /**
    * Value is unsigned double<br />
    * Exposure time (reciprocal of shutter speed). Unit is second
    */
   public static final String TAG_EXPOSURE_TIME = "ExposureTime";
   
   /**
    * Value is unsigned double<br />
    * The actual F-number(F-stop) of lens when the image was taken
    */
   public static final String TAG_FNUMBER = "FNumber";
   
   /**
    * Value is unsigned double<br />
    * The actual aperture value of lens when the image was taken.<br />
    * To convert this value to ordinary F-number(F-stop), calculate this value's power of root 2 (=1.4142).<br />
    * For example, if value is '5', F-number is 1.4142^5 = F5.6<br />
    * <pre>FNumber = Math.exp(ApertureValue * Math.log(2) * 0.5);</pre>
    */
   public static final String TAG_APERTURE_VALUE = "ApertureValue";   
	
   /**
    * Value is signed double<br />
    * Brightness of taken subject, unit is EV.
    */
   public static final String TAG_BRIGHTNESS_VALUE = "BrightnessValue";
   
   /**
    * Value is unsigned double.<br />
    * Maximum aperture value of lens.<br />
    * You can convert to F-number by calculating power of root 2 (same process of ApertureValue(0x9202).<br />
    * <pre>FNumber = Math.exp(MaxApertureValue * Math.log(2) * 0.5)</pre>
    */
   public static final String TAG_MAXAPERTURE_VALUE = "MaxApertureValue";
   
   /**
    * Value if signed double.<br />
    * Distance to focus point, unit is meter.
    * If value < 0 then focus point is infinite
    */
   public static final String TAG_SUBJECT_DISTANCE = "SubjectDistance";
   
   /**
    * Value is signed double.<br />
    * The exposure bias. The unit is the APEX value. Ordinarily it is given in the range of -99.99 to 99.99
    */
   public static final String TAG_EXPOSURE_BIAS_VALUE = "ExposureBiasValue";
   
   /**
    * Value is double.<br />
    * This tag indicates the digital zoom ratio when the image was shot. If the numerator of the recorded value is 0, this
    * indicates that digital zoom was not used
    */
   public static final String TAG_DIGITAL_ZOOM_RATIO = "DigitalZoomRatio";   
   
   /**
    * Value is unsigned int.<br />
    * This tag indicates the equivalent focal length assuming a 35mm film camera, in mm.<br />
    * Exif 2.2 tag, usually not present, it can be calculated by:
    * <pre>
    * CCDWidth = (PixelXDimension * FocalplaneUnits / FocalplaneXRes);
    * FocalLengthIn35mmFilm = (FocalLength / CCDWidth * 36 + 0.5);
    * </pre>
    */
   public static final String TAG_FOCAL_LENGTH_35_MM = "FocalLengthIn35mmFilm";
   
   /**
    * Value is unsigned int.<br />
    * Indicates the image sensor type on the camera or input device. The values are as follows:
    * <ul>
    * <li>1 = Not defined</li>
    * <li>2 = One-chip color area sensor</li>
    * <li>3 = Two-chip color area sensor JEITA CP-3451 - 41</li>
    * <li>4 = Three-chip color area sensor</li>
    * <li>5 = Color sequential area sensor</li>
    * <li>7 = Trilinear sensor</li>
    * <li>8 = Color sequential linear sensor</li>
    * <li>Other = reserved</li>
    * </ul>
    */
   public static final String TAG_SENSING_METHOD = "SensingMethod"; 

   /**
    * Value is int.<br />
    * This tag indicates the white balance mode set when the image was shot:
    * <ul>
    * <li>0 = Auto white balance</li>
    * <li>1 = Manual white balance</li>
    * <li>Other = reserved</li>
    * </ul>
    */
   public static final String TAG_WHITE_BALANCE = "Whitebalance";
   
   /**
    * Value is unsigned int.<br />
    * Exposure metering method:
    * <ul>
    * <li>0 = unknown</li>
    * <li>1 = Average</li>
    * <li>2 = CenterWeightedAverage</li>
    * <li>3 = Spot</li>
    * <li>4 = MultiSpot</li>
    * <li>5 = Pattern</li>
    * <li>6 = Partial</li>
    * <li>Other = reserved</li>
    * <li>255 = other</li>
    * </ul>
    */
   public static final String TAG_METERING_MODE=  "MeteringMode";   

   /**
    * Value is unsigned int.<br />
    * Exposure program that the camera used when image was taken.
    * <ul>
    * <li>'1' means manual control</li>
    * <li>'2' program normal</li>
    * <li>'3' aperture priority</li>
    * <li>'4' shutter priority</li>
    * <li>'5' program creative (slow program)</li>
    * <li>'6' program action(high-speed program)</li>
    * <li>'7' portrait mode</li>
    * <li>'8' landscape mode.</li>
    * </ul>
    */
   public static final String TAG_EXPOSURE_PROGRAM = "ExposureProgram";
   
   /**
    * Value is int.<br />
    * This tag indicates the exposure mode set when the image was shot. In auto-bracketing mode, the camera shoots a
    * series of frames of the same scene at different exposure settings.
    * <ul>
    * <li>0 = Auto exposure</li>
    * <li>1 = Manual exposure</li>
    * <li>2 = Auto bracket</li>
    * <li>Other = reserved</li>
    * </ul>
    */
   public static final String TAG_EXPOSURE_MODE = "ExposureMode";
   
   /**
    * Value is unsigned int.<br />
    * CCD sensitivity equivalent to Ag-Hr film speedrate.<br />
    * Indicates the ISO Speed and ISO Latitude of the camera or input device as specified in ISO 12232
    */
   public static final String TAG_ISO_SPEED_RATINGS = "ISOSpeedRatings";   
   

   /**
    * Value is unsigned int.<br />
    * Light source, actually this means white balance setting.
    * <ul>
    * <li>0 = means auto</li>
    * <li>1 = Daylight</li>
    * <li>2 = Fluorescent</li>
    * <li>3 = Tungsten (incandescent light)</li>
    * <li>4 = Flash</li>
    * <li>9 = Fine weather</li>
    * <li>10 = Cloudy weather</li>
    * <li>11 = Shade</li>
    * <li>12 = Daylight fluorescent (D 5700 Ð 7100K)</li>
    * <li>13 = Day white fluorescent (N 4600 Ð 5400K)</li>
    * <li>14 = Cool white fluorescent (W 3900 Ð 4500K)</li>
    * <li>15 = White fluorescent (WW 3200 Ð 3700K)</li>
    * <li>17 = Standard light A</li>
    * <li>18 = Standard light B</li>
    * <li>19 = Standard light C</li>
    * <li>20 = D55</li>
    * <li>21 = D65</li>
    * <li>22 = D75</li>
    * <li>23 = D50</li>
    * <li>24 = ISO studio tungsten</li>
    * <li>255 = other light source</li>
    * <li>Other = reserved</li>
    * </ul>
    */
   public static final String TAG_LIGHT_SOURCE = "LightSource";
   
   /**
    * Value is int.<br />
    * This tag indicates the distance to the subject.
    * <ul>
    * <li>0 = unknown</li>
    * <li>1 = Macro</li>
    * <li>2 = Close view</li>
    * <li>3 = Distant view</li>
    * <li>Other = reserved</li>
    * </ul>
    */
   public static final String TAG_SUBJECT_DISTANCE_RANGE = "SubjectDistanceRange";
   
   /**
    * Value is unsigned double.<br />
    * Display/Print resolution of image. Large number of digicam uses 1/72inch,
    * but it has no mean because personal computer doesn't use this value to display/print out.
    */
   public static final String TAG_X_RESOLUTION = "XResolution";

   /**
    * @see #TAG_X_RESOLUTION
    */
   public static final String TAG_Y_RESOLUTION = "YResolution";
   
   /**
    * Value is unsigned int.<br />
    * Unit of XResolution(0x011a)/YResolution(0x011b)
    * <ul>
    * <li>'1' means no-unit ( use inch )</li>
    * <li>'2' inch</li>
    * <li>'3' centimeter</li>
    * <li>'4' millimeter</li>
    * <li>'5' micrometer</li>
    * </ul>
    */
   public static final String TAG_RESOLUTION_UNIT = "ResolutionUnit";
   
   /**
    * Value is unsigned int.<br />
    * Specific to compressed data; the valid width of the meaningful image.
    * When a compressed file is recorded, the valid width of the meaningful image shall be recorded in this tag, 
    * whether or not there is padding data or a restart marker. This tag should not exist in an uncompressed file.
    */
   public static final String TAG_PIXEL_X_DIMENSION = "PixelXDimension";
   
   /**
    * Value is unsigned int.<br />
    * @see #TAG_PIXEL_X_DIMENSION
    */
   public static final String TAG_PIXEL_Y_DIMENSION = "PixelYDimension";
   
   /**
    * Value is unsigned double.<br />
    * Indicates the number of pixels in the image width (X) direction per FocalPlaneResolutionUnit on the camera focal plane.
    * CCD's pixel density 
    * @see #TAG_FOCAL_PLANE_RESOLUTION_UNIT
    */
   public static final String TAG_FOCAL_PLANE_X_RESOLUTION = "FocalPlaneXResolution";
   
   /**
    * Value is unsigned int.<br />
    * Unit of FocalPlaneXResoluton/FocalPlaneYResolution.
    * <ul>
    * <li>'1' means no-unit</li>
    * <li>'2' inch</li>
    * <li>'3' centimeter</li>
    * <li>'4' millimeter</li>
    * <li>'5' micrometer</li>
    * </ul>
    *
    * This tag can be used to calculate the CCD Width:
    * <pre>CCDWidth = (PixelXDimension * FocalPlaneResolutionUnit / FocalPlaneXResolution)</pre>
    */
   public static final String TAG_FOCAL_PLANE_RESOLUTION_UNIT = "FocalPlaneResolutionUnit";   
   
   /**
    * Value is unsigned double.<br />
    * Indicates the number of pixels in the image height (Y) direction per FocalPlaneResolutionUnit on the camera focal plane.
    * CCD's pixel density.
    */
   public static final String TAG_FOCAL_PLANE_Y_RESOLUTION = "FocalPlaneYResolution";

   /**
    * Value is int.<br />
    * This tag indicates the type of scene that was shot. It can also be used to record the mode in which the image was
    * shot. Note that this differs from the scene type (SceneType) tag.
    * <ul>
    * <li>0 = Standard</li>
    * <li>1 = Landscape</li>
    * <li>2 = Portrait</li>
    * <li>3 = Night scene</li>
    * <li>Other = reserved</li>
    * </ul>
    */
   public static final String TAG_SCENE_CAPTURE_TYPE = "SceneCaptureType";
   
   /**
    * Value is signed double.<br />
    * Shutter speed. To convert this value to ordinary 'Shutter Speed'; calculate this value's power of 2, then reciprocal.
    * For example, if value is '4', shutter speed is 1/(2^4)=1/16 second.
    */
   public static final String TAG_SHUTTER_SPEED_VALUE = "ShutterSpeedValue";   
   
   /**
    * ASCII string (4).<br />
    * The version of this standard supported. Nonexistence of this field is taken to mean nonconformance to the standard
    * (see section 4.2). Conformance to this standard is indicated by recording "0220" as 4-byte ASCII
    */
   public static final String TAG_EXIF_VERSION = "ExifVersion";
   
   /**
    * Value is int.<br />
    * Normally sRGB (=1) is used to define the color space based on the PC monitor conditions and environment. If a
    * color space other than sRGB is used, Uncalibrated (=FFFF.H) is set. Image data recorded as Uncalibrated can be
    * treated as sRGB when it is converted to Flashpix. On sRGB see Annex E.
    * <ul>
    * <li>'1' = sRGB</li>
    * <li>'FFFF' = Uncalibrated</li>
    * <li>'other' = Reserved</li>
    * </ul>
    */
   public static final String TAG_COLOR_SPACE = "ColorSpace";   
   
   /**
    * Value is unsigned int.<br />
    * (Read only tag) The compression scheme used for the image data. When a primary image is JPEG compressed, this designation is
    * not necessary and is omitted. When thumbnails use JPEG compression, this tag value is set to 6.
    * <ul>
    * <li>1 = uncompressed</li>
    * <li>6 = JPEG compression (thumbnails only)</li>
    * <li>Other = reserved</li>
    */
   public static final String TAG_COMPRESSION = "Compression";   

   /**
	 * Value is string.<br />
	 * The latitude is expressed as degrees, minutes, and seconds, respectively.<br />
	 * Example: N 40d 43m  1.4712s
	 */
	public static final String TAG_GPS_LATITUDE = "GpsLat";

	/**
	 * Value is string.<br />
	 * The longitude is expressed as degrees, minutes, and seconds, respectively.<br />
	 * Example: W 73d 57m 20.4235s
	 */
	public static final String TAG_GPS_LONGITUDE = "GpsLong";

	/**
	 * Value is string.<br />
	 * The altitude (in meters), example: -6.50m
	 */
	public static final String TAG_GPS_ALTITUDE = "GpsAlt";

	/**
	 * Value is string(20).<br />
	 * Not an exif tag, this indicates the file date time ( as stored in the file system )
	 */
	public static final String TAG_FILE_DATETIME = "FileDateTime";
	
	
	
	
	

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
	
   /**
    * Returns the integer value of the specified tag. If there is no such tag
    * in the JPEG file or the value cannot be parsed as integer, return
    * <var>defaultValue</var>.
    *
    * @param tag the name of the tag.
    * @param defaultValue the value to return if the tag is not available.
    */
   public int getAttributeInt(String tag, int defaultValue) {
       String value = mAttributes.get(tag);
       if (value == null) return defaultValue;
       try {
           return Integer.valueOf(value);
       } catch (NumberFormatException ex) {
           return defaultValue;
       }
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
	
   /**
    * Returns true if the JPEG file has a thumbnail.
    */
   public boolean hasThumbnail() {
       return mHasThumbnail;
   }
   
   /**
    * Returns the quality used to generate the JPEG, if present
    * @return
    */
   public int getQuality() {
   	if( mAttributes.containsKey( "QualityGuess" )) {
   		return getAttributeInt( "QualityGuess", -1 );
   	}
   	return -1;
   }

	private native boolean appendThumbnailNative( String fileName, String thumbnailFileName );

	private native void saveAttributesNative( String fileName, String compressedAttributes );

	private native String getAttributesNative( String fileName );

	private native void commitChangesNative( String fileName );

	private native byte[] getThumbnailNative( String fileName );
}
