/**
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 */

package it.sephiroth.android.library.media;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
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

	// ----------------------
	// Exif tag names
	// ----------------------

	/**
	 * Value is int<br />
	 * Not an exif tag, this indicates the file size in bytes
	 */
	public static final String TAG_JPEG_FILESIZE = "FileSize";
	
	/**
	 * Value is string(20).<br />
	 * Not an exif tag, this indicates the file date time ( as stored in the file system )
	 */
	public static final String TAG_JPEG_FILE_DATETIME = "FileDateTime";
	
	/**
	 * Value is int<br />
	 * The number of columns of image data, equal to the number of pixels per row. In JPEG compressed data a JPEG marker is used
	 * instead of this tag.
	 */
	public static final String TAG_JPEG_IMAGE_WIDTH = "ImageWidth";

	/**
	 * Value is int<br />
	 * The number of rows of image data. In JPEG compressed data a JPEG marker is used instead of this tag.
	 */
	public static final String TAG_JPEG_IMAGE_HEIGHT = "ImageHeight";
	
	/**
	 * Value is int.<br />
	 * If present gives the quality used to compress the jpeg file
	 */
	public static final String TAG_JPEG_QUALITY = "QualityGuess";

	/**
	 * Value is int<br />
	 * Not an exif tag, this is extracted from the jpeg file. It gives information about the process used to create the JPEG file.
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
	public static final String TAG_JPEG_PROCESS = "Process";	

	/**
	 * Value is ascii string<br />
	 * The manufacturer of the recording equipment. This is the manufacturer of the DSC, scanner, video digitizer or other equipment
	 * that generated the image. When the field is left blank, it is treated as unknown.
	 */
	public static final String TAG_EXIF_MAKE = "Make";

	/**
	 * Value is ascii string<br />
	 * The model name or model number of the equipment. This is the model name of number of the DSC, scanner, video digitizer or
	 * other equipment that generated the image. When the field is left blank, it is treated as unknown.
	 */
	public static final String TAG_EXIF_MODEL = "Model";

	/**
	 * Value is ascii string (20)<br />
	 * Date/Time of image was last modified. Data format is "YYYY:MM:DD HH:MM:SS"+0x00, total 20bytes. In usual, it has the same
	 * value of DateTimeOriginal(0x9003)
	 */
	public static final String TAG_EXIF_DATETIME = "DateTime";

	/**
	 * Value is ascii string (20)<br />
	 * Date/Time of image digitized. Usually, it contains the same value of DateTimeOriginal(0x9003).
	 */
	public static final String TAG_EXIF_DATETIME_DIGITIZED = "DateTimeDigitized";

	/**
	 * Value is ascii string (20)<br />
	 * Date/Time of original image taken. This value should not be modified by user program.
	 */
	public static final String TAG_EXIF_DATETIME_ORIGINAL = "DateTimeOriginal";

	/**
	 * Values is ascii string<br />
	 * Shows copyright information
	 */
	public static final String TAG_EXIF_COPYRIGHT = "Copyright";

	/**
	 * Vallue is ascii String<br />
	 * This tag records the name of the camera owner, photographer or image creator. The detailed format is not specified, but it is
	 * recommended that the information be written as in the example below for ease of Interoperability. When the field is left
	 * blank, it is treated as unknown.
	 */
	public static final String TAG_EXIF_ARTIST = "Artist";

	/**
	 * Value is ascii string<br />
	 * Shows firmware(internal software of digicam) version number.
	 */
	public static final String TAG_EXIF_SOFTWARE = "Software";

	/**
	 * Value is int<br />
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
	public static final String TAG_EXIF_ORIENTATION = "Orientation";

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
	public static final String TAG_EXIF_FLASH = "Flash";

	/**
	 * Value is unsigned double<br />
	 * Focal length of lens used to take image. Unit is millimeter.
	 */
	public static final String TAG_EXIF_FOCAL_LENGHT = "FocalLength";

	/**
	 * Value is unsigned double<br />
	 * Exposure time (reciprocal of shutter speed). Unit is second
	 */
	public static final String TAG_EXIF_EXPOSURE_TIME = "ExposureTime";

	/**
	 * Value is unsigned double<br />
	 * The actual F-number(F-stop) of lens when the image was taken
	 */
	public static final String TAG_EXIF_FNUMBER = "FNumber";

	/**
	 * Value is unsigned double<br />
	 * The actual aperture value of lens when the image was taken.<br />
	 * To convert this value to ordinary F-number(F-stop), calculate this value's power of root 2 (=1.4142).<br />
	 * For example, if value is '5', F-number is 1.4142^5 = F5.6<br />
	 * 
	 * <pre>
	 * FNumber = Math.exp( ApertureValue * Math.log( 2 ) * 0.5 );
	 * </pre>
	 */
	public static final String TAG_EXIF_APERTURE = "ApertureValue";

	/**
	 * Value is signed double<br />
	 * Brightness of taken subject, unit is EV.
	 */
	public static final String TAG_EXIF_BRIGHTNESS = "BrightnessValue";

	/**
	 * Value is unsigned double.<br />
	 * Maximum aperture value of lens.<br />
	 * You can convert to F-number by calculating power of root 2 (same process of ApertureValue(0x9202).<br />
	 * 
	 * <pre>
	 * FNumber = Math.exp( MaxApertureValue * Math.log( 2 ) * 0.5 )
	 * </pre>
	 */
	public static final String TAG_EXIF_MAXAPERTURE = "MaxApertureValue";

	/**
	 * Value if signed double.<br />
	 * Distance to focus point, unit is meter. If value < 0 then focus point is infinite
	 */
	public static final String TAG_EXIF_SUBJECT_DISTANCE = "SubjectDistance";

	/**
	 * Value is signed double.<br />
	 * The exposure bias. The unit is the APEX value. Ordinarily it is given in the range of -99.99 to 99.99
	 */
	public static final String TAG_EXIF_EXPOSURE_BIAS = "ExposureBiasValue";

	/**
	 * Value is double.<br />
	 * This tag indicates the digital zoom ratio when the image was shot. If the numerator of the recorded value is 0, this indicates
	 * that digital zoom was not used
	 */
	public static final String TAG_EXIF_DIGITAL_ZOOM_RATIO = "DigitalZoomRatio";

	/**
	 * Value is unsigned int.<br />
	 * This tag indicates the equivalent focal length assuming a 35mm film camera, in mm.<br />
	 * Exif 2.2 tag, usually not present, it can be calculated by:
	 * 
	 * <pre>
	 * CCDWidth = ( PixelXDimension * FocalplaneUnits / FocalplaneXRes );
	 * FocalLengthIn35mmFilm = ( FocalLength / CCDWidth * 36 + 0.5 );
	 * </pre>
	 */
	public static final String TAG_EXIF_FOCAL_LENGTH_35_MM = "FocalLengthIn35mmFilm";

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
	public static final String TAG_EXIF_SENSING_METHOD = "SensingMethod";

	/**
	 * Value is int.<br />
	 * This tag indicates the white balance mode set when the image was shot:
	 * <ul>
	 * <li>0 = Auto white balance</li>
	 * <li>1 = Manual white balance</li>
	 * <li>Other = reserved</li>
	 * </ul>
	 */
	public static final String TAG_EXIF_WHITE_BALANCE = "Whitebalance";

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
	public static final String TAG_EXIF_METERING_MODE = "MeteringMode";

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
	public static final String TAG_EXIF_EXPOSURE_PROGRAM = "ExposureProgram";

	/**
	 * Value is int.<br />
	 * This tag indicates the exposure mode set when the image was shot. In auto-bracketing mode, the camera shoots a series of
	 * frames of the same scene at different exposure settings.
	 * <ul>
	 * <li>0 = Auto exposure</li>
	 * <li>1 = Manual exposure</li>
	 * <li>2 = Auto bracket</li>
	 * <li>Other = reserved</li>
	 * </ul>
	 */
	public static final String TAG_EXIF_EXPOSURE_MODE = "ExposureMode";

	/**
	 * Value is unsigned int.<br />
	 * CCD sensitivity equivalent to Ag-Hr film speedrate.<br />
	 * Indicates the ISO Speed and ISO Latitude of the camera or input device as specified in ISO 12232
	 */
	public static final String TAG_EXIF_ISO_SPEED_RATINGS = "ISOSpeedRatings";

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
	public static final String TAG_EXIF_LIGHT_SOURCE = "LightSource";

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
	public static final String TAG_EXIF_SUBJECT_DISTANCE_RANGE = "SubjectDistanceRange";

	/**
	 * Value is unsigned double.<br />
	 * Display/Print resolution of image. Large number of digicam uses 1/72inch, but it has no mean because personal computer doesn't
	 * use this value to display/print out.
	 */
	public static final String TAG_EXIF_X_RESOLUTION = "XResolution";

	/**
	 * @see #TAG_X_RESOLUTION
	 */
	public static final String TAG_EXIF_Y_RESOLUTION = "YResolution";

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
	public static final String TAG_EXIF_RESOLUTION_UNIT = "ResolutionUnit";

	/**
	 * Value is unsigned int.<br />
	 * Specific to compressed data; the valid width of the meaningful image. When a compressed file is recorded, the valid width of
	 * the meaningful image shall be recorded in this tag, whether or not there is padding data or a restart marker. This tag should
	 * not exist in an uncompressed file.
	 */
	public static final String TAG_EXIF_PIXEL_X_DIMENSION = "PixelXDimension";

	/**
	 * Value is unsigned int.<br />
	 * 
	 * @see #TAG_EXIF_PIXEL_X_DIMENSION
	 */
	public static final String TAG_EXIF_PIXEL_Y_DIMENSION = "PixelYDimension";

	/**
	 * Value is unsigned double.<br />
	 * Indicates the number of pixels in the image width (X) direction per FocalPlaneResolutionUnit on the camera focal plane. CCD's
	 * pixel density
	 * 
	 * @see #TAG_EXIF_FOCAL_PLANE_RESOLUTION_UNIT
	 */
	public static final String TAG_EXIF_FOCAL_PLANE_X_RESOLUTION = "FocalPlaneXResolution";

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
	 * 
	 * <pre>
	 * CCDWidth = ( PixelXDimension * FocalPlaneResolutionUnit / FocalPlaneXResolution )
	 * </pre>
	 */
	public static final String TAG_EXIF_FOCAL_PLANE_RESOLUTION_UNIT = "FocalPlaneResolutionUnit";

	/**
	 * Value is unsigned double.<br />
	 * Indicates the number of pixels in the image height (Y) direction per FocalPlaneResolutionUnit on the camera focal plane. CCD's
	 * pixel density.
	 */
	public static final String TAG_EXIF_FOCAL_PLANE_Y_RESOLUTION = "FocalPlaneYResolution";

	/**
	 * Value is int.<br />
	 * This tag indicates the type of scene that was shot. It can also be used to record the mode in which the image was shot. Note
	 * that this differs from the scene type (SceneType) tag.
	 * <ul>
	 * <li>0 = Standard</li>
	 * <li>1 = Landscape</li>
	 * <li>2 = Portrait</li>
	 * <li>3 = Night scene</li>
	 * <li>Other = reserved</li>
	 * </ul>
	 */
	public static final String TAG_EXIF_SCENE_CAPTURE_TYPE = "SceneCaptureType";

	/**
	 * Value is signed double.<br />
	 * Shutter speed. To convert this value to ordinary 'Shutter Speed'; calculate this value's power of 2, then reciprocal. For
	 * example, if value is '4', shutter speed is 1/(2^4)=1/16 second.
	 */
	public static final String TAG_EXIF_SHUTTER_SPEED_VALUE = "ShutterSpeedValue";

	/**
	 * ASCII string (4).<br />
	 * The version of this standard supported. Nonexistence of this field is taken to mean nonconformance to the standard (see
	 * section 4.2). Conformance to this standard is indicated by recording "0220" as 4-byte ASCII
	 */
	public static final String TAG_EXIF_VERSION = "ExifVersion";

	/**
	 * Value is int.<br />
	 * Normally sRGB (=1) is used to define the color space based on the PC monitor conditions and environment. If a color space
	 * other than sRGB is used, Uncalibrated (=FFFF.H) is set. Image data recorded as Uncalibrated can be treated as sRGB when it is
	 * converted to Flashpix. On sRGB see Annex E.
	 * <ul>
	 * <li>'1' = sRGB</li>
	 * <li>'FFFF' = Uncalibrated</li>
	 * <li>'other' = Reserved</li>
	 * </ul>
	 */
	public static final String TAG_EXIF_COLOR_SPACE = "ColorSpace";

	/**
	 * Value is unsigned int.<br />
	 * (Read only tag) The compression scheme used for the image data. When a primary image is JPEG compressed, this designation is
	 * not necessary and is omitted. When thumbnails use JPEG compression, this tag value is set to 6.
	 * <ul>
	 * <li>1 = uncompressed</li>
	 * <li>6 = JPEG compression (thumbnails only)</li>
	 * <li>Other = reserved</li>
	 */
	public static final String TAG_EXIF_COMPRESSION = "Compression";

	/**
	 * Value is string.<br />
	 * The latitude is expressed as degrees, minutes, and seconds, respectively.<br />
	 * Example: N 40d 43m 1.4712s
	 */
	public static final String TAG_EXIF_GPS_LATITUDE = "GpsLat";

	/**
	 * Value is string.<br />
	 * The longitude is expressed as degrees, minutes, and seconds, respectively.<br />
	 * Example: W 73d 57m 20.4235s
	 */
	public static final String TAG_EXIF_GPS_LONGITUDE = "GpsLong";

	/**
	 * Value is string.<br />
	 * The altitude (in meters), example: -6.50m
	 */
	public static final String TAG_EXIF_GPS_ALTITUDE = "GpsAlt";
	
	// Constants used for the Orientation Exif tag.
	public static final int ORIENTATION_UNDEFINED = 0;
	public static final int ORIENTATION_NORMAL = 1;
	public static final int ORIENTATION_FLIP_HORIZONTAL = 2; // left right reversed mirror
	public static final int ORIENTATION_ROTATE_180 = 3;
	public static final int ORIENTATION_FLIP_VERTICAL = 4; // upside down mirror
	public static final int ORIENTATION_TRANSPOSE = 5; // flipped about top-left <--> bottom-right axis
	public static final int ORIENTATION_ROTATE_90 = 6; // rotate 90 cw to right it
	public static final int ORIENTATION_TRANSVERSE = 7; // flipped about top-right <--> bottom-left axis
	public static final int ORIENTATION_ROTATE_270 = 8; // rotate 270 to right it	
	
	public static final int RESOLUTION_UNIT_INCHES = 2;
	public static final int RESOLUTION_UNIT_CENTIMETERS = 3;	
	public static final int RESOLUTION_UNIT_MILLIMETERS = 4;	
	public static final int RESOLUTION_UNIT_MICROMETERS = 5;	


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
	 * Returns a set containing all the exif attributes loaded
	 * @return
	 */
	public Set<String> keySet() {
		return new HashSet<String>( mAttributes.keySet() );
	}
	
	/**
	 * Returns true if the passed key has the corresponding value
	 * @param key
	 * @return
	 */
	public boolean hasAttribute( final String key ) {
		return mAttributes.containsKey( key );
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
	 * Returns the integer value of the specified tag. If there is no such tag in the JPEG file or the value cannot be parsed as
	 * integer, return <var>defaultValue</var>.
	 * 
	 * @param tag
	 *           the name of the tag.
	 * @param defaultValue
	 *           the value to return if the tag is not available.
	 */
	public int getAttributeInt( String tag, int defaultValue ) {
		String value = mAttributes.get( tag );
		if ( value == null ) return defaultValue;
		try {
			return Integer.valueOf( value );
		} catch ( NumberFormatException ex ) {
			return defaultValue;
		}
	}
	
	/**
	 * Returns the double value of the specified rational tag. If there is no such tag in the JPEG file or the value cannot be parsed
	 * as double, return <var>defaultValue</var>.
	 * 
	 * @param tag
	 *           the name of the tag.
	 * @param defaultValue
	 *           the value to return if the tag is not available.
	 */
	public double getAttributeDouble( String tag, double defaultValue ) {
		String value = mAttributes.get( tag );
		if ( value == null ) return defaultValue;
		try {
			return Double.parseDouble( value );
		} catch ( NumberFormatException ex ) {
			ex.printStackTrace();
			return defaultValue;
		}
	}
	
	/**
	 * Return the F-Number value.<br />
	 * 
	 * @return
	 */
	public double getApertureSize() {
		double value = getAttributeDouble( TAG_EXIF_FNUMBER, 0 );
		if( value > 0 ) {
			return value;
		}
		
		value = getAttributeDouble( TAG_EXIF_APERTURE, 0 );
		if( value > 0 ) {
			return Math.exp( value * Math.log( 2 ) * 0.5 );
		}
		
		value = getAttributeDouble( TAG_EXIF_MAXAPERTURE, 0 );
		if( value > 0 ) {
			return Math.exp( value * Math.log( 2 ) * 0.5 );
		}
		
		return 0;
	}
   
	/**
	 * Returns the orientation in degress.
	 * @return
	 */

	public int getOrientation() {
		final int orientation = getAttributeInt( TAG_EXIF_ORIENTATION, -1 );
		if ( orientation != -1 ) {
			switch ( orientation ) {
				case ORIENTATION_UNDEFINED:
				case ORIENTATION_NORMAL:
					return 0;
				case ORIENTATION_ROTATE_90:
					return 90;
				case ORIENTATION_ROTATE_180:
					return 180;
				case ORIENTATION_ROTATE_270:
					return 270;
				default:
					return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Given the value from {@link #TAG_EXIF_FOCAL_PLANE_RESOLUTION_UNIT} or {@link #TAG_EXIF_RESOLUTION_UNIT}
	 * this method will return the corresponding value in millimeters
	 * @param resolution
	 * @return resolution in millimeters
	 */
	public double getResolutionUnit( int resolution ) {
		switch ( resolution ) {
			case 1:
			case RESOLUTION_UNIT_INCHES:
				return 25.4;

			case RESOLUTION_UNIT_CENTIMETERS:
				return 10;

			case RESOLUTION_UNIT_MILLIMETERS:
				return 1;

			case RESOLUTION_UNIT_MICROMETERS:
				return .001;

			default:
				return 25.4;
		}
	}
	
	/**
	 * Returns the CCD (Charge-Coupled Device), if it can be computed.<br />
	 * To be computed the following tags must be present:
	 * <ul>
	 * <li>{@link #TAG_EXIF_FOCAL_PLANE_X_RESOLUTION}</li>
	 * <li>{@link #TAG_EXIF_PIXEL_X_DIMENSION}</li>
	 * <li>{@link #TAG_EXIF_FOCAL_PLANE_RESOLUTION_UNIT}</li>
	 * </ul>
	 * @return the CCD width value, in millimeters, or -1 if the value cannot be computed
	 */
	public double getCCDWidth() {
		double focalPlaneXResolution = getAttributeDouble( ExifInterfaceExtended.TAG_EXIF_FOCAL_PLANE_X_RESOLUTION, 0 );
		int pixel_x_dimen = getAttributeInt( ExifInterfaceExtended.TAG_EXIF_PIXEL_X_DIMENSION, 0 );
		int pixel_y_dimen = getAttributeInt( ExifInterfaceExtended.TAG_EXIF_PIXEL_Y_DIMENSION, 0 );

		if ( focalPlaneXResolution > 0 && ( pixel_x_dimen > 0 || pixel_y_dimen > 0 ) ) {
			int size = Math.max( pixel_x_dimen, pixel_y_dimen );
			int resolution_unit = getAttributeInt( ExifInterfaceExtended.TAG_EXIF_FOCAL_PLANE_RESOLUTION_UNIT, 0 );
			double resolution_mm = getResolutionUnit( resolution_unit );
			return ( size * resolution_mm / focalPlaneXResolution );
		}
		return -1;
	}
   
   /**
    * Set the value of the specified tag.
    *
    * @param tag the name of the tag.
    * @param value the value of the tag.
    */
   public void setAttribute(String tag, String value) {
       mAttributes.put(tag, value);
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

   /**
    * Returns the thumbnail inside the JPEG file, or {@code null} if there is no thumbnail.
    * The returned data is in JPEG format and can be decoded using
    * {@link android.graphics.BitmapFactory#decodeByteArray(byte[],int,int)}
    */	
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
	 * 
	 * @return
	 */
	public int getJpegQuality() {
		return getAttributeInt( TAG_JPEG_QUALITY, -1 );
	}

	/**
	 * Stores the latitude and longitude value in a float array. The first element is the latitude, and the second element is the
	 * longitude. Returns false if the Exif tags are not available.
	 */
	public boolean getLatLong( float output[] ) {
		String latValue = mAttributes.get( TAG_EXIF_GPS_LATITUDE );
		String lngValue = mAttributes.get( TAG_EXIF_GPS_LONGITUDE );

		if ( latValue != null && lngValue != null ) {
			try {
				output[0] = convertRationalLatLonToFloat( latValue );
				output[1] = convertRationalLatLonToFloat( lngValue );
				return true;
			} catch ( IllegalArgumentException e ) {
				// if values are not parseable
			}
		}
		return false;
	}
	
   /**
    * Return the altitude in meters. If the exif tag does not exist, return
    * <var>defaultValue</var>.
    *
    * @param defaultValue the value to return if the tag is not available.
    */
   public double getAltitude(double defaultValue) {
       String altitude = getAttribute(TAG_EXIF_GPS_ALTITUDE);
       if( null != altitude ) {
      	 try {
      		 return Double.parseDouble( altitude.trim().substring( 0, altitude.length() - 1 ) );
      	 } catch( NumberFormatException e ) {
      		 // return the default value
      	 }
       }
       return defaultValue;
   }
   
   /**
    * Returns number of milliseconds since Jan. 1, 1970, midnight.
    * Returns -1 if the date time information if not available.
    * @hide
    */
   public long getDateTime( String dateTimeString ) {
       if (dateTimeString == null) return -1;

       ParsePosition pos = new ParsePosition(0);
       try {
           Date datetime = sFormatter.parse(dateTimeString, pos);
           if (datetime == null) return -1;
           return datetime.getTime();
       } catch (IllegalArgumentException ex) {
           return -1;
       }
   }   

	private static float convertRationalLatLonToFloat( String rationalString ) {
		try {
			
			String[] parts = rationalString.split( "( )+" );
			if( parts.length < 4 ) {
				throw new IllegalArgumentException("Expecting 4 parts, " + parts.length + " found");
			}

			// first part is the reference ( N/S W/E )
			String ref = parts[0].trim();
			
			String value = parts[1].trim();
			value = value.substring( 0, value.length() - 1 );
			double degrees = Double.parseDouble( value );

			value = parts[2].trim();
			value = value.substring( 0, value.length() - 1 );
			double minutes = Double.parseDouble( value );

			value = parts[3].trim();
			value = value.substring( 0, value.length() - 1 );
			double seconds = Double.parseDouble( value );
			
			double result = degrees + ( minutes / 60.0 ) + ( seconds / 3600.0 );
			
         if ((ref.equals("S") || ref.equals("W"))) {
            return (float) -result;			
         }
         
			return (float) result;
		} catch ( NumberFormatException e ) {
			// Some of the nubmers are not valid
			throw new IllegalArgumentException();
		} catch ( ArrayIndexOutOfBoundsException e ) {
			// Some of the rational does not follow the correct format
			throw new IllegalArgumentException();
		} catch( IndexOutOfBoundsException e ) {
			// String not valid
			throw new IllegalArgumentException();
		}
	} 
	
	// -------------------
	// NATIVE METHODS
	// -------------------
	
	
	private native boolean appendThumbnailNative( String fileName, String thumbnailFileName );

	private native void saveAttributesNative( String fileName, String compressedAttributes );

	private native String getAttributesNative( String fileName );

	private native void commitChangesNative( String fileName );

	private native byte[] getThumbnailNative( String fileName );
}
