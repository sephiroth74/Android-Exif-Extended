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

#ifdef __cplusplus
extern "C"
{
#endif

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


	static void saveJPGFile(const char* filename)
	{
		LOGI("saveJPGFile: %s", filename);

		char backupName[400];
		struct stat buf;

		LOGV("Modified: %s\n", filename);

		strncpy(backupName, filename, 395);
		strcat(backupName, ".t");

		// Remove any .old file name that may pre-exist
		LOGV("removing backup %s", backupName);

		unlink(backupName);

		// Rename the old file.
		LOGV("rename %s to %s", filename, backupName);

		rename(filename, backupName);

		// Write the new file.
		LOGD("WriteJpegFile %s", filename);

		if (WriteJpegFile(filename))
		{
			// Copy the access rights from original file
			LOGV("stating old file %s", backupName);
			if (stat(backupName, &buf) == 0)
			{
				// set Unix access rights and time to new file
				struct utimbuf mtime;
				chmod(filename, buf.st_mode);

				mtime.actime = buf.st_mtime;
				mtime.modtime = buf.st_mtime;

				utime(filename, &mtime);
			}

			// Now that we are done, remove original file.
			LOGV("unlinking old file %s", backupName);
			unlink(backupName);

			LOGV("returning from saveJPGFile");
		} else
		{
			LOGE("WriteJpegFile failed, restoring from backup file");
			// move back the backup file
			rename(backupName, filename);
		}
	}

// JNI Methods

	void copyThumbnailData(uchar* thumbnailData, int thumbnailLen)
	{
		LOGI("******************************** copyThumbnailData");

		Section_t* ExifSection = FindSection(M_EXIF);

		if (ExifSection == NULL) {
			return;
		}

		int NewExifSize = ImageInfo.ThumbnailOffset+8+thumbnailLen;
		ExifSection->Data = (uchar *)realloc(ExifSection->Data, NewExifSize);

		if (ExifSection->Data == NULL) {
			LOGW("ExifSection->Data = NULL");
			return;
		}

		uchar* ThumbnailPointer = ExifSection->Data+ImageInfo.ThumbnailOffset+8;
		memcpy(ThumbnailPointer, thumbnailData, thumbnailLen);

		ImageInfo.ThumbnailSize = thumbnailLen;

		Put32u(ExifSection->Data+ImageInfo.ThumbnailSizeOffset+8, thumbnailLen);

		ExifSection->Data[0] = (uchar)(NewExifSize >> 8);
		ExifSection->Data[1] = (uchar)NewExifSize;
		ExifSection->Size = NewExifSize;
	}

	static void saveAttributes(JNIEnv *env, jobject jobj, jstring jfilename, jstring jattributes)
	{
		LOGI("******************************** saveAttributes");

		// format of attributes string passed from java:
		// "attrCnt attr1=valueLen value1attr2=value2Len value2..."
		// example input: "4 ImageLength=4 1024Model=6 FooImageWidth=4 1280Make=3 FOO"

		ExifElement_t* exifElementTable = NULL;
		const char* filename = NULL;
		uchar* thumbnailData = NULL;
		int attrCnt = 0;
		const char* attributes = (*env)->GetStringUTFChars(env, jattributes, NULL);
		if (attributes == NULL)
		{
			goto exit;
		}

		// Get the number of attributes - it's the first number in the string.
		attrCnt = atoi(attributes);
		char* attrPtr = strchr(attributes, ' ') + 1;

		LOGD("attribute count %d attrPtr %s\n", attrCnt, attrPtr);

		// Load all the hash exif elements into a more c-like structure
		exifElementTable = malloc(sizeof(ExifElement_t) * attrCnt);
		if (exifElementTable == NULL)
		{
			goto exit;
		}


		int i;
		char tag[100];
		int hasDateTimeTag = FALSE;
		int gpsTagCount = 0;
		int exifTagCount = 0;
		int tagValue;
		int tagFound;
		ExifElement_t* item;

		for (i = 0; i < attrCnt; i++)
		{
			// get an element from the attribute string and add it to the c structure
			// first, extract the attribute name
			tagFound = 0;
			char* tagEnd = strchr(attrPtr, '=');
			if (tagEnd == 0)
			{
				LOGE("saveAttributes: couldn't find end of tag");
				goto exit;
			}
			if (tagEnd - attrPtr > 99)
			{
				LOGE("saveAttributes: attribute tag way too long");
				goto exit;
			}

			memcpy(tag, attrPtr, tagEnd - attrPtr);
			tag[tagEnd - attrPtr] = 0;

			exifElementTable[i].Format = 0;
			exifElementTable[i].Tag = 0;
			exifElementTable[i].GpsTag = FALSE;

			if (IsGpsTag(tag))
			{
				tagValue = GpsTagNameToValue(tag);
				if( tagValue > -1 )
				{
					LOGV("Tag '%s' with value: X%x", tag, tagValue);
					exifElementTable[i].GpsTag = TRUE;
					exifElementTable[i].Tag = GpsTagNameToValue(tag);
					++gpsTagCount;
					tagFound = 1;
				} else {
					LOGE("(GPS) Skipping gps tag: %s = %i", tag, tagValue);
				}
			} else
			{
				tagValue = TagNameToValue(tag);
				if( tagValue > -1 )
				{
					LOGV("Tag '%s' with value: X%x", tag, tagValue);
					exifElementTable[i].GpsTag = FALSE;
					exifElementTable[i].Tag = tagValue;
					++exifTagCount;
					tagFound = 1;
				} else {
					LOGE("(EXIF) Skipping tag %s = %i", tag, tagValue);
				}
			}

			LOGV("tagFound: %i", tagFound);

			attrPtr = tagEnd + 1;
			// next get the length of the attribute value
			int valueLen = atoi(attrPtr);

			if (IsDateTimeTag(exifElementTable[i].Tag))
			{
				hasDateTimeTag = TRUE;
			}


			attrPtr = strchr(attrPtr, ' ') + 1;
			if (attrPtr == 0)
			{
				LOGE("saveAttributes: couldn't find end of value len");
				goto exit;
			}


			exifElementTable[i].Value = malloc(valueLen + 1);
			if (exifElementTable[i].Value == NULL)
			{
				goto exit;
			}

			memcpy(exifElementTable[i].Value, attrPtr, valueLen);
			exifElementTable[i].Value[valueLen] = 0;
			exifElementTable[i].DataLength = valueLen;

			attrPtr += valueLen;

			LOGD("tag %s id %d value %s data length=%d isGps=%d", tag, exifElementTable[i].Tag,
					exifElementTable[i].Value, exifElementTable[i].DataLength, exifElementTable[i].GpsTag);
		}

		LOGD("Total tags: %i - %i ( total was: %i )", exifTagCount, gpsTagCount, attrCnt);

		filename = (*env)->GetStringUTFChars(env, jfilename, NULL);
		LOGD("Call loadAttributes() with filename is %s. Loading exif info\n", filename);
		loadExifInfo(filename, TRUE);

		// DEBUG ONLY ----------
		// ShowTags = TRUE;
		// ShowImageInfo(TRUE);
		// LOGD("create exif 2");
		// ---------------------

		// If the jpg file has a thumbnail, preserve it.
		int thumbnailLength = ImageInfo.ThumbnailSize;
		if (ImageInfo.ThumbnailOffset)
		{
			Section_t* ExifSection = FindSection(M_EXIF);
			if (ExifSection)
			{
				uchar* thumbnailPointer = ExifSection->Data + ImageInfo.ThumbnailOffset + 8;
				thumbnailData = (uchar*)malloc(ImageInfo.ThumbnailSize);
				// if the malloc fails, we just won't copy the thumbnail
				if (thumbnailData)
				{
					memcpy(thumbnailData, thumbnailPointer, thumbnailLength);
				}
			}
		}

		create_EXIF_Elements(exifElementTable, exifTagCount, gpsTagCount, attrCnt, hasDateTimeTag);

		if (thumbnailData)
		{
			copyThumbnailData(thumbnailData, thumbnailLength);
		}

		exit:
		LOGE("cleaning up now in saveAttributes");
		// try to clean up resources
		if (attributes)
		{
			(*env)->ReleaseStringUTFChars(env, jattributes, attributes);
		}
		if (filename)
		{
			(*env)->ReleaseStringUTFChars(env, jfilename, filename);
		}
		if (exifElementTable)
		{
			// free the table
			for (i = 0; i < attrCnt; i++)
			{
				free(exifElementTable[i].Value);
			}
			free(exifElementTable);
		}
		if (thumbnailData)
		{
			free(thumbnailData);
		}

		LOGD("returning from saveAttributes");
	}

	static jstring getAttributes(JNIEnv *env, jobject jobj, jstring jfilename)
	{
		LOGI("******************************** getAttributes");

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

		if( ImageInfo.ImageWidth > 0 && ImageInfo.ImageLength > 0 )
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ImageWidth", ImageInfo.ImageWidth);
			if (bufLen == 0) return NULL;

			bufLen = addKeyValueInt(&buf, bufLen, "ImageLength", ImageInfo.ImageLength);
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

		if (ImageInfo.ExposureMode >= 0)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ExposureMode", ImageInfo.ExposureMode);
			if (bufLen == 0) return NULL;
		}

		if (ImageInfo.ISOSpeedRatings)
		{
			bufLen = addKeyValueInt(&buf, bufLen, "ISOSpeedRatings", ImageInfo.ISOSpeedRatings);
			if (bufLen == 0) return NULL;
		}

		if(ImageInfo.LightSource >= 0)
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

		if( ImageInfo.SceneCaptureType >= 0)
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
			if (ImageInfo.GpsLatitudeRef[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsLatitudeRef", ImageInfo.GpsLatitudeRef);
				if (bufLen == 0) return NULL;
			}

			if (ImageInfo.GpsLongitudeRef[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsLongitudeRef", ImageInfo.GpsLongitudeRef);
				if (bufLen == 0) return NULL;
			}

			if (ImageInfo.GpsAltitudeRef == 0 || ImageInfo.GpsAltitudeRef == 1)
			{
				bufLen = addKeyValueInt(&buf, bufLen, "GpsAltitudeRef", ImageInfo.GpsAltitudeRef);
				if (bufLen == 0) return NULL;
			}

			if (ImageInfo.GpsLatitude[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsLatitude", ImageInfo.GpsLatitude);
				if (bufLen == 0) return NULL;
			}

			if (ImageInfo.GpsLongitude[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsLongitude", ImageInfo.GpsLongitude);
				if (bufLen == 0) return NULL;
			}

			if (ImageInfo.GpsAltitude[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsAltitude", ImageInfo.GpsAltitude);
				if (bufLen == 0) return NULL;
			}

			if( ImageInfo.GpsSpeedRef[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsSpeedRef", ImageInfo.GpsSpeedRef);
				if (bufLen == 0) return NULL;
			}

			if (ImageInfo.GpsSpeed[0])
			{
				bufLen = addKeyValueString(&buf, bufLen, "GpsSpeed", ImageInfo.GpsSpeed);
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
		LOGI("******************************** commitChanges\n");

		const char* filename = (*env)->GetStringUTFChars(env, jfilename, NULL);

		if (filename)
		{
			saveJPGFile(filename);
			DiscardData();
			(*env)->ReleaseStringUTFChars(env, jfilename, filename);
		}
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
		{ "saveAttributesNative", "(Ljava/lang/String;Ljava/lang/String;)V", (void*) saveAttributes},
		{ "getAttributesNative", "(Ljava/lang/String;)Ljava/lang/String;", (void*) getAttributes},
		{ "commitChangesNative", "(Ljava/lang/String;)V", (void*) commitChanges},
		{ "getThumbnailNative", "(Ljava/lang/String;)[B", (void*) getThumbnail},
	};

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

#ifdef __cplusplus
}
#endif
