//--------------------------------------------------------------------------
// Parsing of GPS info from exif header.
//
// Matthias Wandel,  Dec 1999 - Dec 2002 
//--------------------------------------------------------------------------
#include "jhead.h"
#include "utils/log.h"

#define MAX_GPS_TAG 0x1e

#define TAG_GPS_LAT_REF    1
#define TAG_GPS_LAT        2
#define TAG_GPS_LONG_REF   3
#define TAG_GPS_LONG       4
#define TAG_GPS_ALT_REF    5
#define TAG_GPS_ALT        6
#define TAG_GPS_SATELLITES 8
#define TAG_GPS_STATUS     9
#define TAG_GPS_PROCESSING_METHOD	27

static TagTable_t GpsTags[MAX_GPS_TAG + 1] = {
   { 0x00, "GpsVersionID", FMT_BYTE, 4 },
   { 0x01, "GpsLatitudeRef", FMT_STRING, 2 },
   { 0x02, "GpsLatitude", FMT_URATIONAL, 3 },
   { 0x03, "GpsLongitudeRef", FMT_STRING, 2 },
   { 0x04, "GpsLongitude", FMT_URATIONAL, 3 },
   { 0x05, "GpsAltitudeRef", FMT_BYTE, 1 },
   { 0x06, "GpsAltitude", FMT_URATIONAL, 1 },
   { 0x07, "GpsTimeStamp", FMT_SRATIONAL, 3 },
   { 0x08, "GpsSatellites", FMT_STRING, -1 },
   { 0x09, "GpsStatus", FMT_STRING, 2 },
   { 0x0A, "GpsMeasureMode", FMT_STRING, 2 },
   { 0x0B, "GpsDOP", FMT_SRATIONAL, 1 },
   { 0x0C, "GpsSpeedRef", FMT_STRING, 2 },
   { 0x0D, "GpsSpeed", FMT_SRATIONAL, 1 },
   { 0x0E, "GpsTrackRef", FMT_STRING, 2 },
   { 0x0F, "GpsTrack", FMT_SRATIONAL, 1 },
   { 0x10, "GpsImgDirectionRef", FMT_STRING, -1 },
   { 0x11, "GpsImgDirection", FMT_SRATIONAL, 1 },
   { 0x12, "GpsMapDatum", FMT_STRING, -1 },
   { 0x13, "GpsDestLatitudeRef", FMT_STRING, 2 },
   { 0x14, "GpsDestLatitude", FMT_SRATIONAL, 3 },
   { 0x15, "GpsDestLongitudeRef", FMT_STRING, 2 },
   { 0x16, "GpsDestLongitude", FMT_SRATIONAL, 3 },
   { 0x17, "GpsDestBearingRef", FMT_STRING, 1 },
   { 0x18, "GpsDestBearing", FMT_SRATIONAL, 1 },
   { 0x19, "GpsDestDistanceRef", FMT_STRING, 2 },
   { 0x1A, "GpsDestDistance", FMT_SRATIONAL, 1 },
   { 0x1B, "GpsProcessingMethod", FMT_UNDEFINED, -1 },
   { 0x1C, "GpsAreaInformation", FMT_STRING, -1 },
   { 0x1D, "GpsDateStamp", FMT_STRING, 11 },
   { 0x1E, "GpsDifferential", FMT_SSHORT, 1 },
};


int IsGpsTag(const char* tag)
{
	return strstr(tag, "Gps") == tag;
}

int GpsTagNameToValue(const char* tagName)
{
	unsigned int i;
	for (i = 0; i < MAX_GPS_TAG; i++)
	{
		if (strcmp(GpsTags[i].Desc, tagName) == 0)
		{
			return GpsTags[i].Tag;
		}
	}
	return -1;
}

TagTable_t* GpsTagToTagTableEntry(unsigned short tag)
{
	unsigned int i;
	for (i = 0; i < MAX_GPS_TAG; i++)
	{
		if (GpsTags[i].Tag == tag)
		{
			int format = GpsTags[i].Format;
			if (format == 0)
			{
				LOGW("tag %s format not defined", GpsTags[i].Desc);
				return NULL;
			}
			return &GpsTags[i];
		}
	}
	return NULL;
}

//--------------------------------------------------------------------------
// Process GPS info directory
//--------------------------------------------------------------------------
void ProcessGpsInfo(unsigned char * DirStart, unsigned char * OffsetBase, unsigned ExifLength)
{
	int de;
	unsigned a;
	int NumDirEntries;

	NumDirEntries = Get16u(DirStart);
#define DIR_ENTRY_ADDR(Start, Entry) (Start+2+12*(Entry))

	if (ShowTags)
	{
		printf("(dir has %d entries)\n", NumDirEntries);
	}

	ImageInfo.GpsInfoPresent = TRUE;
	strcpy(ImageInfo.GpsLatitude, "? ?");
	strcpy(ImageInfo.GpsLongitude, "? ?");
	ImageInfo.GpsAltitude[0] = 0;

	LOGI("NumDirEntries: %i", NumDirEntries);

	for (de = 0; de < NumDirEntries; de++)
	{
		unsigned Tag, Format, Components;
		unsigned char * ValuePtr;
		int ComponentSize;
		unsigned ByteCount;
		unsigned char * DirEntry;
		DirEntry = DIR_ENTRY_ADDR(DirStart, de);

		if (DirEntry + 12 > OffsetBase + ExifLength)
		{
			ErrNonfatal("GPS info directory goes past end of exif", 0, 0);
			return;
		}

		Tag = Get16u(DirEntry);
		Format = Get16u(DirEntry + 2);
		Components = Get32u(DirEntry + 4);

		if ((Format - 1) >= NUM_FORMATS)
		{
			// (-1) catches illegal zero case as unsigned underflows to positive large.
			ErrNonfatal("Illegal number format %d for Exif gps tag %04x", Format, Tag);
			continue;
		}

		ComponentSize = BytesPerFormat[Format];
		ByteCount = Components * ComponentSize;

		if (ByteCount > 4)
		{
			unsigned OffsetVal;
			OffsetVal = Get32u(DirEntry + 8);
			// If its bigger than 4 bytes, the dir entry contains an offset.
			if (OffsetVal + ByteCount > ExifLength)
			{
				// Bogus pointer offset and / or bytecount value
				ErrNonfatal("Illegal value pointer for Exif gps tag %04x", Tag, 0);
				continue;
			}
			ValuePtr = OffsetBase + OffsetVal;
		} else
		{
			// 4 bytes or less and value is in the dir entry itself
			ValuePtr = DirEntry + 8;
		}

		// LOGI("Tag: %i", Tag);

		switch (Tag)
		{
			char FmtString[21];
			char TempString[50];
			double Values[3];

		case TAG_GPS_LAT_REF:
			ImageInfo.GpsLatitude[0] = ValuePtr[0];
			break;

		case TAG_GPS_LONG_REF:
			ImageInfo.GpsLongitude[0] = ValuePtr[0];
			break;

		case TAG_GPS_LAT:
			case TAG_GPS_LONG:
			if (Format != FMT_URATIONAL)
			{
				ErrNonfatal("Inappropriate format (%d) for Exif GPS coordinates!", Format, 0);
			}
			strcpy(FmtString, "%0.0fd %0.0fm %0.0fs");
			for (a = 0; a < 3; a++)
			{
				int den, digits;

				den = Get32s(ValuePtr + 4 + a * ComponentSize);
				digits = 0;
				while (den > 1 && digits <= 6)
				{
					den = den / 10;
					digits += 1;
				}
				if (digits > 6)
					digits = 6;
				FmtString[1 + a * 7] = (char) ('2' + digits + (digits ? 1 : 0));
				FmtString[3 + a * 7] = (char) ('0' + digits);

				Values[a] = ConvertAnyFormat(ValuePtr + a * ComponentSize, Format);
			}

			sprintf(TempString, FmtString, Values[0], Values[1], Values[2]);

			if (Tag == TAG_GPS_LAT)
			{
				strncpy(ImageInfo.GpsLatitude + 2, TempString, 29);
			} else
			{
				strncpy(ImageInfo.GpsLongitude + 2, TempString, 29);
			}

//                sprintf(TempString, "%d/%d,%d/%d,%d/%d",
//                    Get32s(ValuePtr), Get32s(4+(char*)ValuePtr),
//                    Get32s(8+(char*)ValuePtr), Get32s(12+(char*)ValuePtr),
//                    Get32s(16+(char*)ValuePtr), Get32s(20+(char*)ValuePtr));
//
//                LOGD("Raw value: %s", TempString);
//
//                if( Tag == TAG_GPS_LAT )
//                {
//                    strncpy(ImageInfo.GpsLatRaw, TempString, MAX_GPS_BUF_SIZE);
//                } else {
//                    strncpy(ImageInfo.GpsLongRaw, TempString, MAX_GPS_BUF_SIZE);
//                }

			break;

		case TAG_GPS_ALT_REF:
			ImageInfo.GpsAltitude[0] = (char) (ValuePtr[0] ? '-' : ' ');
			break;

		case TAG_GPS_ALT:
			sprintf(ImageInfo.GpsAltitude + 1, "%.2fm", ConvertAnyFormat(ValuePtr, Format));
			break;

		case TAG_GPS_SATELLITES:
			break;

		case TAG_GPS_STATUS:
			break;

		case TAG_GPS_PROCESSING_METHOD:
			break;
		}

		if (ShowTags)
		{
			// Show tag value.
			if (Tag < MAX_GPS_TAG)
			{
				printf("        %s =", GpsTags[Tag].Desc);
			} else
			{
				// Show unknown tag
				printf("        Illegal GPS tag %04x=", Tag);
			}

			switch (Format)
			{
				case FMT_UNDEFINED:
					// Undefined is typically an ascii string.

				case FMT_STRING:
					// String arrays printed without function call (different from int arrays)
				{
					printf("\"");
					for (a = 0; a < ByteCount; a++)
					{
						int ZeroSkipped = 0;
						if (ValuePtr[a] >= 32)
						{
							if (ZeroSkipped)
							{
								printf("?");
								ZeroSkipped = 0;
							}
							putchar(ValuePtr[a]);
						} else
						{
							if (ValuePtr[a] == 0)
							{
								ZeroSkipped = 1;
							}
						}
					}
					printf("\"\n");
				}
					break;

				default:
					// Handle arrays of numbers later (will there ever be?)
					for (a = 0;;)
					{
						PrintFormatNumber(ValuePtr + a * ComponentSize, Format, ByteCount);
						if (++a >= Components)
							break;
						printf(", ");
					}
					printf("\n");
			}
		}
	}
}

