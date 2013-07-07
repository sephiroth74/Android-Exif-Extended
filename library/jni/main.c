/*
 * main.c
 *
 *  Created on: Jul 5, 2013
 *      Author: alessandro
 */

#include <jni.h>
#include <assert.h>
#include <ctype.h>
#include <dlfcn.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>

#include "config.h"
#include "jhead.h"
#include "utils/log.h"

#ifndef NELEM
#define NELEM(x) ((int)(sizeof(x) / sizeof((x)[0])))
#endif

	static int attributeCount; // keep track of how many attributes we've added

	static int addKeyValueString(char** buf, int bufLen, const char* key, const char* value)
	{
		LOGI("addKeyValueString: %s = %s", key,value);

		// Appends to buf like this: "ImageLength=4 1024"
		char valueLen[15];
		snprintf(valueLen, 15, "=%d ", (int) strlen(value));

		// check to see if buf has enough room to append
		int len = strlen(key) + strlen(valueLen) + strlen(value);
		int newLen = strlen(*buf) + len;
		if (newLen >= bufLen)
		{
			bufLen = newLen + 500;
			*buf = realloc(*buf, bufLen);
			if (*buf == NULL)
			{
				return 0;
			}
		}
		// append the new attribute and value
		snprintf(*buf + strlen(*buf), bufLen, "%s%s%s", key, valueLen, value);
		++attributeCount;
		return bufLen;
	}

	// returns new buffer length
	static int addKeyValueInt(char** buf, int bufLen, const char* key, int value)
	{
		char valueStr[20];
		snprintf(valueStr, 20, "%d", value);
		return addKeyValueString(buf, bufLen, key, valueStr);
	}

	// returns new buffer length
	static int addKeyValueDouble(char** buf, int bufLen, const char* key, double value, const char* format)
	{
		char valueStr[30];
		snprintf(valueStr, 30, format, value);
		return addKeyValueString(buf, bufLen, key, valueStr);
	}

	static int loadExifInfo(const char* FileName, int readJPG)
	{
		LOGI("loadExifInfo, filename: %s, readJPG: %i", FileName, readJPG);

		int Modified = FALSE;
		ReadMode_t ReadMode = READ_METADATA;
		if (readJPG)
		{
			// Must add READ_IMAGE else we can't write the JPG back out.
			ReadMode |= READ_IMAGE;
		}

		LOGD("ResetJpgfile");
		ResetJpgfile();

		// Start with an empty image information structure.
		memset(&ImageInfo, 0, sizeof(ImageInfo));
		ImageInfo.Flash = -1;
		ImageInfo.MeteringMode = -1;
		ImageInfo.Whitebalance = -1;

		// Store file date/time.
		{
			struct stat st;
			if (stat(FileName, &st) >= 0)
			{
				ImageInfo.FileDateTime = st.st_mtime;
				ImageInfo.FileSize = st.st_size;
			}
		}

		strncpy(ImageInfo.FileName, FileName, PATH_MAX);
		LOGD("ReadJpegFile");
		return ReadJpegFile(FileName, ReadMode);
	}

// JNI Methods

	static jboolean appendThumbnail(JNIEnv *env, jobject jobj, jstring jfilename, jstring jthumbnailfilename)
	{
		LOGI("appendThumbnail");
		return JNI_FALSE;
	}

	static void saveAttributes(JNIEnv *env, jobject jobj, jstring jfilename, jstring jattributes)
	{
		LOGI("saveAttributes");
	}

	static jstring getAttributes(JNIEnv *env, jobject jobj, jstring jfilename)
	{
		LOGI("getAttributes");

		const char* filename = (*env)->GetStringUTFChars(env, jfilename, NULL);
		LOGD("filename: %s", filename);

		loadExifInfo(filename, FALSE);

		// release the string
		(*env)->ReleaseStringUTFChars(env, jfilename, filename);

		attributeCount = 0;
		int bufLen = 1000;

		char* buf = malloc(bufLen);
		if (buf == NULL)
		{
			return NULL;
		}
		*buf = 0; // start the string out at zero length

		// ShowImageInfo(TRUE);

		// parse all attributes

		// thumbnail
		bufLen = addKeyValueString(&buf, bufLen, "hasThumbnail", ImageInfo.ThumbnailOffset == 0 || ImageInfo.ThumbnailAtEnd == FALSE || ImageInfo.ThumbnailSize == 0 ? "false" : "true");
		if (bufLen == 0) return NULL;

		if( ImageInfo.Make[0] )
		{
			bufLen = addKeyValueString( &buf, bufLen, "Make", ImageInfo.Make );
			if( bufLen == 0 ) return NULL;
		}

		if( ImageInfo.Model[0] )
		{
			bufLen = addKeyValueString( &buf, bufLen, "Model", ImageInfo.Model );
			if( bufLen == 0 ) return NULL;
		}

		if( ImageInfo.DateTime[0] )
		{
			bufLen = addKeyValueString( &buf, bufLen, "DateTime", ImageInfo.DateTime );
			if( bufLen == 0 ) return NULL;
		}

		if( ImageInfo.DateTimeDigitized[0] )
		{
			bufLen = addKeyValueString( &buf, bufLen, "DateTimeDigitized", ImageInfo.DateTimeDigitized );
			if( bufLen == 0 ) return NULL;
		}

		if( ImageInfo.DateTimeOriginal[0] )
		{
			bufLen = addKeyValueString( &buf, bufLen, "DateTimeOriginal", ImageInfo.DateTimeOriginal );
			if( bufLen == 0 ) return NULL;
		}

		if( ImageInfo.Copyright[0] )
		{
			bufLen = addKeyValueString( &buf, bufLen, "Copyright", ImageInfo.Copyright );
			if( bufLen == 0 ) return NULL;
		}

		if( ImageInfo.Artist[0] )
		{
			bufLen = addKeyValueString( &buf, bufLen, "Artist", ImageInfo.Artist );
			if( bufLen == 0 ) return NULL;
		}

		if( ImageInfo.Software[0] )
		{
			bufLen = addKeyValueString( &buf, bufLen, "Software", ImageInfo.Software );
			if( bufLen == 0 ) return NULL;
		}

		if( ImageInfo.ImageWidth > 0 && ImageInfo.ImageHeight > 0 )
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ImageWidth", ImageInfo.ImageWidth);
			if (bufLen == 0) return NULL;

			bufLen = addKeyValueInt(&buf, bufLen, "ImageHeight", ImageInfo.ImageHeight);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.Orientation >= 0 )
		{
			bufLen = addKeyValueInt(&buf, bufLen, "Orientation", ImageInfo.Orientation);
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.Flash >= 0)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "Flash", ImageInfo.Flash);
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.FocalLength)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "FocalLength", ImageInfo.FocalLength, "%4.2f");
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.ExposureTime)
		{
			const char* format;
			if (ImageInfo.ExposureTime < 0.010)
			{
				format = "%6.4f";
			} else
			{
				format = "%5.3f";
			}

			bufLen = addKeyValueDouble(&buf, bufLen, "ExposureTime", (double)ImageInfo.ExposureTime, format);
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.FNumber > 0)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "FNumber", (double)ImageInfo.FNumber, "%3.1f");
			if (bufLen == 0) return NULL;
		}

		if( ImageInfo.ApertureValue )
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "ApertureValue", (double)ImageInfo.ApertureValue, "%4.2f");
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.BrightnessValue )
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "BrightnessValue", (double)ImageInfo.BrightnessValue, "%4.2f");
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.MaxApertureValue)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "MaxApertureValue", (double)ImageInfo.MaxApertureValue, "%4.2f");
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.SubjectDistance)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "SubjectDistance", (double)ImageInfo.SubjectDistance, "%4.2f");
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.ExposureBiasValue)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "ExposureBiasValue", (double)ImageInfo.ExposureBiasValue, "%4.2f");
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.DigitalZoomRatio > 1.0)
		{
			// Digital zoom used.  Shame on you! (LOL)
			bufLen = addKeyValueDouble(&buf, bufLen, "DigitalZoomRatio", ImageInfo.DigitalZoomRatio, "%2.3f");
			if (bufLen == 0) return NULL;
		}

		if( ImageInfo.FocalLengthIn35mmFilm )
		{
			bufLen = addKeyValueInt(&buf, bufLen, "FocalLengthIn35mmFilm", ImageInfo.FocalLengthIn35mmFilm);
			if (bufLen == 0) return NULL;
		}

		if( ImageInfo.SensingMethod > 0 )
		{
			bufLen = addKeyValueInt(&buf, bufLen, "SensingMethod", ImageInfo.SensingMethod);
			if (bufLen == 0) return NULL;
		}

		if( ImageInfo.Whitebalance >= 0 && ImageInfo.Whitebalance <= 1 )
		{
			bufLen = addKeyValueInt(&buf, bufLen, "Whitebalance", ImageInfo.Whitebalance);
			if (bufLen == 0) return NULL;
		}

		if( ImageInfo.MeteringMode > 0 && ImageInfo.MeteringMode <= 255)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "MeteringMode", ImageInfo.MeteringMode);
			if (bufLen == 0) return NULL;
		}

//		if(ImageInfo.CompressedBitsPerPixel)
//		{
//			bufLen = addKeyValueDouble(&buf, bufLen, "CompressedBitsPerPixel", (double)ImageInfo.CompressedBitsPerPixel, "%4.2f");
//			if (bufLen == 0) return NULL;
//		}

		if (ImageInfo.ExposureProgram > 0)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ExposureProgram", ImageInfo.ExposureProgram);
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.ExposureMode)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ExposureMode", ImageInfo.ExposureMode);
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.ISOSpeedRatings)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ISOSpeedRatings", ImageInfo.ISOSpeedRatings);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.LightSource)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "LightSource", ImageInfo.LightSource);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.SubjectDistanceRange > 0)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "SubjectDistanceRange", ImageInfo.SubjectDistanceRange);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.XResolution)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "XResolution", ImageInfo.XResolution, "%.2f");
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.YResolution)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "YResolution", ImageInfo.YResolution, "%.2f");
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.ResolutionUnit)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ResolutionUnit", ImageInfo.ResolutionUnit);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.FocalPlaneXResolution)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "FocalPlaneXResolution", ImageInfo.FocalPlaneXResolution, "%.4f");
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.FocalPlaneYResolution)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "FocalPlaneYResolution", ImageInfo.FocalPlaneYResolution, "%.4f");
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.FocalPlaneResolutionUnit)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "FocalPlaneResolutionUnit", ImageInfo.FocalPlaneResolutionUnit);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.PixelXDimension)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "PixelXDimension", ImageInfo.PixelXDimension);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.PixelYDimension)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "PixelYDimension", ImageInfo.PixelYDimension);
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.QualityGuess)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "QualityGuess", ImageInfo.QualityGuess);
			if (bufLen == 0) return NULL;
		}

		if( ImageInfo.SceneCaptureType)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "SceneCaptureType", ImageInfo.SceneCaptureType);
			if (bufLen == 0) return NULL;
		}

		if( ImageInfo.ShutterSpeedValue)
		{
			bufLen = addKeyValueDouble(&buf, bufLen, "ShutterSpeedValue", ImageInfo.ShutterSpeedValue, "%.4f");
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.ExifVersion[0])
		{
			bufLen = addKeyValueString(&buf, bufLen, "ExifVersion", ImageInfo.ExifVersion);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.ColorSpace > 0)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ColorSpace", ImageInfo.ColorSpace);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.Compression > 0)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "Compression", ImageInfo.Compression);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.Process)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "Process", ImageInfo.Process);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.FileDateTime > 0)
		{
			char filedatetime[20];
			FileTimeAsString( filedatetime );
			bufLen = addKeyValueString( &buf, bufLen, "FileDateTime", filedatetime );
			if( bufLen == 0 ) return NULL;
		}

		if(ImageInfo.FileSize)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "FileSize", ImageInfo.FileSize);
			if (bufLen == 0) return NULL;
		}

		// GPS
		if (ImageInfo.GpsInfoPresent)
		{
			if (ImageInfo.GpsLat[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsLat", ImageInfo.GpsLat);
				if (bufLen == 0) return NULL;
			}

			if (ImageInfo.GpsLong[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsLong", ImageInfo.GpsLong);
				if (bufLen == 0) return NULL;
			}

			if (ImageInfo.GpsAlt[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsAlt", ImageInfo.GpsAlt);
				if (bufLen == 0) return NULL;
			}
		}

		// end parsing
		LOGD("final buffer: %s", buf);

		// put the attribute count at the beginnnig of the string
		int finalBufLen = strlen(buf) + 20;
		char* finalResult = malloc(finalBufLen);
		if (finalResult == NULL)
		{
			free(buf);
			return NULL;
		}
		snprintf(finalResult, finalBufLen, "%d %s", attributeCount, buf);
		int k;
		for (k = 0; k < finalBufLen; k++)
		if (!isascii(finalResult[k]))
		finalResult[k] = '?';
		free(buf);

		LOGD("*********Returning result \"%s\"", finalResult);
		jstring result = ((*env)->NewStringUTF(env, finalResult));
		free(finalResult);
		DiscardData();
		return result;
	}

	static void commitChanges(JNIEnv *env, jobject jobj, jstring jfilename)
	{
		LOGI("commitChanges");
	}

static jbyteArray getThumbnail(JNIEnv *env, jobject jobj, jstring jfilename)
{
	LOGI("getThumbnail");

	const char* filename = (*env)->GetStringUTFChars(env, jfilename, NULL);

	if (filename)
	{
		loadExifInfo(filename, FALSE);
		Section_t* ExifSection = FindSection(M_EXIF);
		if (ExifSection == NULL || ImageInfo.ThumbnailSize == 0)
		{
			LOGE("no exif section or size == 0, so no thumbnail\n");
			goto noThumbnail;
		}
		uchar* thumbnailPointer = ExifSection->Data + ImageInfo.ThumbnailOffset + 8;
		jbyteArray byteArray = (*env)->NewByteArray(env, ImageInfo.ThumbnailSize);
		if (byteArray == NULL)
		{
			LOGE("couldn't allocate thumbnail memory, so no thumbnail\n");
			goto noThumbnail;
		}
		(*env)->SetByteArrayRegion(env, byteArray, 0, ImageInfo.ThumbnailSize, thumbnailPointer);
		LOGD("thumbnail size %d\n", ImageInfo.ThumbnailSize);
		(*env)->ReleaseStringUTFChars(env, jfilename, filename);
		DiscardData();
		return byteArray;
	}
	noThumbnail: if (filename)
	{
		(*env)->ReleaseStringUTFChars(env, jfilename, filename);
	}
	DiscardData();
	return NULL;
}

// JNI load

	static JNINativeMethod methods[] =
	{
		{	"saveAttributesNative", "(Ljava/lang/String;Ljava/lang/String;)V", (void*) saveAttributes},
		{	"getAttributesNative", "(Ljava/lang/String;)Ljava/lang/String;", (void*) getAttributes},
		{	"appendThumbnailNative",
			"(Ljava/lang/String;Ljava/lang/String;)Z", (void*) appendThumbnail},
		{	"commitChangesNative", "(Ljava/lang/String;)V",
			(void*) commitChanges},
		{	"getThumbnailNative", "(Ljava/lang/String;)[B", (void*) getThumbnail},};

	/*
	 * Register several native methods for one class.
	 */
	static int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int numMethods)
	{
		jclass clazz;

		clazz = (*env)->FindClass(env, className);
		if (clazz == NULL)
		{
			LOGE("Native registration unable to find class '%s'", className);
			return JNI_FALSE;
		}
		if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0)
		{
			LOGE("RegisterNatives failed for '%s'", className);
			return JNI_FALSE;
		}
		return JNI_TRUE;
	}

	/*
	 * Register native methods for all classes we know about.
	 */
	static int registerNatives(JNIEnv* env)
	{
		return registerNativeMethods(env, fullQualifiedClassName, methods, NELEM(methods));
	}

	/*
	 * Set some test stuff up.
	 *
	 * Returns the JNI version on success, -1 on failure.
	 */
	__attribute__ ((visibility("default"))) jint JNI_OnLoad(JavaVM* vm, void* reserved)
	{
		JNIEnv* env = NULL;
		jint result = -1;

		if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		{
			LOGE("ERROR: GetEnv failed");
			goto bail;
		}
		assert(env != NULL);

		LOGD("In main JNI_OnLoad");

		if (registerNatives(env) < 0)
		{
			LOGE("ERROR: Exif native registration failed");
			goto bail;
		}

		/* success -- return valid version number */
		result = JNI_VERSION_1_4;

		bail: return result;
	}

