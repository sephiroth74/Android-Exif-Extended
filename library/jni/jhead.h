//--------------------------------------------------------------------------
// Include file for jhead program.
//
// This include file only defines stuff that goes across modules.  
// I like to keep the definitions for macros and structures as close to 
// where they get used as possible, so include files only get stuff that 
// gets used in more than one file.
//--------------------------------------------------------------------------
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <ctype.h>

//--------------------------------------------------------------------------

#ifdef _WIN32
    #include <sys/utime.h>

    // Make the Microsoft Visual c 10 deprecate warnings go away.
    // The _CRT_SECURE_NO_DEPRECATE doesn't do the trick like it should.
    #define unlink _unlink
    #define chmod _chmod
    #define access _access
    #define mktemp _mktemp
#else
    #include <utime.h>
    #include <sys/types.h>
    #include <unistd.h>
    #include <errno.h>
    #include <limits.h>
#endif


typedef unsigned char uchar;

#ifndef TRUE
    #define TRUE 1
    #define FALSE 0
#endif

#define MAX_COMMENT_SIZE 2000

#ifdef _WIN32
    #define PATH_MAX _MAX_PATH
    #define SLASH '\\'
#else
    #ifndef PATH_MAX
        #define PATH_MAX 1024
    #endif
    #define SLASH '/'
#endif


//--------------------------------------------------------------------------
// This structure is used to store jpeg file sections in memory.
typedef struct {
    uchar *  Data;
    int      Type;
    unsigned Size;
}Section_t;

extern int ExifSectionIndex;

extern int DumpExifMap;

#define MAX_DATE_COPIES 10


// Buffer size must large enough to hold maximum location string
// containing six signed integers plus delimeters and terminator,
// i.e.: 11 * 6 + 3(Ô/Õ) + 2(Õ,Õ) + 1(\0) = 72
#define MAX_GPS_BUF_SIZE    72

//--------------------------------------------------------------------------
// This structure stores Exif header image elements in a simple manner
// Used to store camera data as extracted from the various ways that it can be
// stored in an exif header
typedef struct {
    char  FileName     [PATH_MAX+1];
    time_t FileDateTime;

    struct {
        // Info in the jfif header.
        // This info is not used much - jhead used to just replace it with default
        // values, and over 10 years, only two people pointed this out.
        char  Present;
        char  ResolutionUnits;
        short XDensity;
        short YDensity;
    }JfifHeader;

    unsigned FileSize;

    /* ascii string
     * The manufacturer of the recording equipment. This is the manufacturer of the DSC, scanner, video digitizer or other
     * equipment that generated the image. When the field is left blank, it is treated as unknown.
     */
    char  Make[32];

    /* ascii string.
     * The model name or model number of the equipment. This is the model name of number of the DSC, scanner, video
     * digitizer or other equipment that generated the image. When the field is left blank, it is treated as unknown.
     */
    char  Model[40];

    /* ascii string (20)
     * Date/Time of image was last modified. Data format is "YYYY:MM:DD HH:MM:SS"+0x00, total 20bytes.
     * In usual, it has the same value of DateTimeOriginal(0x9003)
     */
    char  DateTime[20];

    /* ascii string (20)
     * Date/Time of image digitized. Usually, it contains the same value of DateTimeOriginal(0x9003).
     */
    char DateTimeDigitized[20];

    /* ascii string (20)
     * Date/Time of original image taken. This value should not be modified by user program.
     */
    char  DateTimeOriginal[20];

    /* ascii string
     * Shows copyright information
     */
    char  Copyright[255];

    /*
     * This tag records the name of the camera owner, photographer or image creator. The detailed format is not specified,
     * but it is recommended that the information be written as in the example below for ease of Interoperability. When the
     * field is left blank, it is treated as unknown.
     */
    char  Artist[255];

    /* ascii string
     * Shows firmware(internal software of digicam) version number.
     */
    char  Software[255];

    /* unsigned short
     * The number of columns and rows of image data, equal to the number of pixels per row. In JPEG compressed data a JPEG  marker is used instead of this tag.
     */
    int ImageWidth;
    int ImageLength;


    /* unsigned short
     * The orientation of the camera relative to the scene, when the image was captured.
     * The start point of stored data is,
     * '0' undefined
     * '1' normal,
     * '2' flip horizontal
     * '3' rotate 180,
     * '4' flip vertical
     * '5' transpose, flipped about top-left <--> bottom-right axis
     * '6' rotate 90 cw,
     * '7' transverse, flipped about top-right <--> bottom-left axis
     * '8' rotate 270,
     * '9' undefined
     */
    int   Orientation;

    /* readonly information */
    int   Process;

    /*
     * unsigned short
     * Bit 0 indicates the flash firing status,
     * bits 1 and 2 indicate the flash return status,
     * bits 3 and 4 indicate the flash mode,
     * bit 5 indicates whether the flash function is present,
     * and bit 6 indicates "red eye" mode
     * bit 7 unused
     *
     * Resulting Flash tag values.
     * 0000.H = Flash did not fire.
     * 0001.H = Flash fired.
     * 0005.H = Strobe return light not detected.
     * 0007.H = Strobe return light detected.
     * 0009.H = Flash fired, compulsory flash mode
     * 000D.H = Flash fired, compulsory flash mode, return light not detected
     * 000F.H = Flash fired, compulsory flash mode, return light detected
     * 0010.H = Flash did not fire, compulsory flash mode
     * 0018.H = Flash did not fire, auto mode
     * 0019.H = Flash fired, auto mode
     * 001D.H = Flash fired, auto mode, return light not detected
     * 001F.H = Flash fired, auto mode, return light detected
     * 0020.H = No flash function
     * 0041.H = Flash fired, red-eye reduction mode
     * 0045.H = Flash fired, red-eye reduction mode, return light not detected
     * 0047.H = Flash fired, red-eye reduction mode, return light detected
     * 0049.H = Flash fired, compulsory flash mode, red-eye reduction mode
     * 004D.H = Flash fired, compulsory flash mode, red-eye reduction mode, return light not detected
     * 004F.H = Flash fired, compulsory flash mode, red-eye reduction mode, return light detected
     * 0059.H = Flash fired, auto mode, red-eye reduction mode
     * 005D.H = Flash fired, auto mode, return light not detected, red-eye reduction mode
     * 005F.H = Flash fired, auto mode, return light detected, red-eye reduction mode
     * Other = reserved
     */
    int   Flash;

    /*
     * unsigned rational.
     * Focal length of lens used to take image. Unit is millimeter.
     */
    float FocalLength;

    /* unsigned rational. Exposure time (reciprocal of shutter speed). Unit is second. */
    float ExposureTime;

    /* unsigned rational. The actual F-number(F-stop) of lens when the image was taken. */
    float FNumber;

    /*
     * unsigned rational
     * The actual aperture value of lens when the image was taken.
     * To convert this value to ordinary F-number(F-stop), calculate this value's power of root 2 (=1.4142).
     * For example, if value is '5', F-number is 1.4142^5 = F5.6.
     * FNumber = math.exp(ApertureValue * math.log(2) * 0.5)
     */
    float ApertureValue;

    /*
     * signed rational
     * Brightness of taken subject, unit is EV.
     */
    float BrightnessValue;

    /*
     * unsigned rational
     * Maximum aperture value of lens.
     * You can convert to F-number by calculating power of root 2 (same process of ApertureValue(0x9202).
     * FNumber = math.exp(MaxApertureValue * math.log(2) * 0.5)
     */
    float MaxApertureValue;

    /*
     * signed rational
     * Distance to focus point, unit is meter.
     * If value < 0 then focus point is infinite
     */
    float SubjectDistance;

    /*
     * signed rational
     * Exposure bias value of taking picture. Unit is EV.
     */
    float ExposureBiasValue;

    /*
     * This tag indicates the digital zoom ratio when the image was shot. If the numerator of the recorded value is 0, this
     * indicates that digital zoom was not used
     */
    float DigitalZoomRatio;

    /*
     * unsigned short
     * if not present, it can be calculated by:
     * CCDWidth = (PixelXDimension * FocalplaneUnits / FocalplaneXRes);
     * FocalLengthIn35mmFilm = (FocalLength / CCDWidth * 36 + 0.5);
     */
    int FocalLengthIn35mmFilm; // Exif 2.2 tag - usually not present.

    /*
     * unsigned short.
     * Indicates the image sensor type on the camera or input device. The values are as follows
     * 1 = Not defined
     * 2 = One-chip color area sensor
     * 3 = Two-chip color area sensor
     * 4 = Three-chip color area sensor
     * 5 = Color sequential area sensor
     * 7 = Trilinear sensor
     * 8 = Color sequential linear sensor
     * Other = reserved
     */
    int SensingMethod;

    /* short
     * This tag indicates the white balance mode set when the image was shot.
     * 0 = Auto white balance
     * 1 = Manual white balance
     * Other = reserved
     */
    int   Whitebalance;

    /*
     * unsigned short
     * Exposure metering method.
     * 0 = unknown
     * 1 = Average
     * 2 = CenterWeightedAverage
     * 3 = Spot
     * 4 = MultiSpot
     * 5 = Pattern
     * 6 = Partial
     * Other = reserved
     * 255 = other
     */
    int   MeteringMode;

    /* unsigned rational.
     * The average compression ratio of JPEG.
     * Information specific to compressed data. The compression mode used for a compressed image is indicated in unit bits per pixel.
     */
    // float CompressedBitsPerPixel;

    /*
     * unsigned short.
     * Exposure program that the camera used when image was taken.
     * '1' means manual control,
     * '2' program normal,
     * '3' aperture priority,
     * '4' shutter priority,
     * '5' program creative (slow program),
     * '6' program action(high-speed program),
     * '7' portrait mode,
     * '8' landscape mode.
     */
    int   ExposureProgram;

    /* short
     * This tag indicates the exposure mode set when the image was shot. In auto-bracketing mode, the camera shoots a
     * series of frames of the same scene at different exposure settings.
     * 0 = Auto exposure
     * 1 = Manual exposure
     * 2 = Auto bracket
     * Other = reserved
     */
    int   ExposureMode;

    /* unsigned short.
     * CCD sensitivity equivalent to Ag-Hr film speedrate.
     */
    int   ISOSpeedRatings;

    /*
     * unsigned short
     * Light source, actually this means white balance setting.
     * '0' means auto
     * 1 = Daylight
     * 2 = Fluorescent
     * 3 = Tungsten (incandescent light)
     * 4 = Flash
     * 9 = Fine weather
     * 10 = Cloudy weather
     * 11 = Shade
     * 12 = Daylight fluorescent (D 5700 Ð 7100K)
     * 13 = Day white fluorescent (N 4600 Ð 5400K)
     * 14 = Cool white fluorescent (W 3900 Ð 4500K)
     * 15 = White fluorescent (WW 3200 Ð 3700K)
     * 17 = Standard light A
     * 18 = Standard light B
     * 19 = Standard light C
     * 20 = D55
     * 21 = D65
     * 22 = D75
     * 23 = D50
     * 24 = ISO studio tungsten
     * 255 = other light source
     * Other = reserved
     */
    int   LightSource;

    /* short
     * This tag indicates the distance to the subject.
     * 0 = unknown
     * 1 = Macro
     * 2 = Close view
     * 3 = Distant view
     * Other = reserved
     */
    int   SubjectDistanceRange;

    /* unsigned rational
     * Display/Print resolution of image. Large number of digicam uses 1/72inch,
     * but it has no mean because personal computer doesn't use this value to display/print out.
     */
    float XResolution;
    float YResolution;

    /* unsigned short
     * Unit of XResolution(0x011a)/YResolution(0x011b)
     *  '1' means no-unit ( use inch )
     *  '2' inch
     *  '3' centimeter
     *  '4' millimeter
     *  '5' micrometer
     */
    int   ResolutionUnit;

    unsigned ThumbnailOffset;          // Exif offset to thumbnail
    unsigned ThumbnailSize;            // Size of thumbnail.
    unsigned LargestExifOffset;        // Last exif data referenced (to check if thumbnail is at end)

    char  ThumbnailAtEnd;              // Exif header ends with the thumbnail
                                       // (we can only modify the thumbnail if its at the end)
    int   ThumbnailSizeOffset;

    /*
     * unsigned short/long
     */
    int PixelXDimension;

    /*
     * unsigned short/long
     */
    int PixelYDimension;

    /*
     * unsigned rational
     * Indicates the number of pixels in the image width (X) direction per FocalPlaneResolutionUnit on the camera focal plane.
     * CCD's pixel density (see FocalPlaneResolutionUnit)
     */
    float FocalPlaneXResolution;

    /*
     * unsigned short
     * Unit of FocalPlaneXResoluton/FocalPlaneYResolution.
     * '1' means no-unit,
     * '2' inch,
     * '3' centimeter.
     * '4' millimeter
     * '5' micrometer
     *
     * CCD Width = (PixelXDimension * FocalPlaneResolutionUnit / FocalPlaneXResolution)
     */
    int FocalPlaneResolutionUnit;

    /*
     * unsigned rational
     * Indicates the number of pixels in the image height (Y) direction per FocalPlaneResolutionUnit on the camera focal plane.
     * CCD's pixel density.
     */
    float FocalPlaneYResolution;

    int  DateTimeOffsets[MAX_DATE_COPIES];
    int  numDateTimeTags;

    /* information only, tells if the GPS tags are present */
    int GpsInfoPresent;

    // char GpsLatitude[31];
    // char GpsLongitude[31];
    // char GpsAltitude[20];

    // reference ( N/S - W/E )
    char GpsLatitudeRef[2];
    char GpsLongitudeRef[2];
    char GpsSpeedRef[2];
    char GpsAltitudeRef; // - or +

    // contains the raw values
    char GpsLatitude[MAX_GPS_BUF_SIZE];
    char GpsLongitude[MAX_GPS_BUF_SIZE];
    char GpsAltitude[MAX_GPS_BUF_SIZE];
    char GpsSpeed[MAX_GPS_BUF_SIZE];


    /* informational only, not an actual exif tag */
    int  QualityGuess;

    /* short
     * This tag indicates the type of scene that was shot. It can also be used to record the mode in which the image was
     * shot. Note that this differs from the scene type (SceneType) tag.
     * 0 = Standard
     * 1 = Landscape
     * 2 = Portrait
     * 3 = Night scene
     * Other = reserved
     */
    int SceneCaptureType;

    /*
     * signed rational
     * Shutter speed. To convert this value to ordinary 'Shutter Speed'; calculate this value's power of 2, then reciprocal.
     * For example, if value is '4', shutter speed is 1/(2^4)=1/16 second.
     */
    float ShutterSpeedValue;

    /*
     * ascii string (4)
     * The version of this standard supported. Nonexistence of this field is taken to mean nonconformance to the standard
     * (see section 4.2). Conformance to this standard is indicated by recording "0220" as 4-byte ASCII. Since the type is
     * UNDEFINED, there is no NULL for termination.
     */
    char ExifVersion[4];

    /*
     * short
     * Normally sRGB (=1) is used to define the color space based on the PC monitor conditions and environment. If a
     * color space other than sRGB is used, Uncalibrated (=FFFF.H) is set. Image data recorded as Uncalibrated can be
     * treated as sRGB when it is converted to Flashpix. On sRGB see Annex E.
     * '1' = sRGB
     * 'FFFF' = Uncalibrated
     * 'other' = Reserved
     */
    int ColorSpace;

    /* unsigned short (read-only)
     * The compression scheme used for the image data. When a primary image is JPEG compressed, this designation is
     * not necessary and is omitted. When thumbnails use JPEG compression, this tag value is set to 6.
     *
     * 1 = uncompressed
     * 6 = JPEG compression (thumbnails only)
     * Other = reserved
     */
    int Compression;

    /*
     * unsigned short
     * This tag indicates the direction of sharpness processing applied by the camera when the image was shot
     * 0 = Normal
     * 1 = Soft
     * 2 = Hard
     * Other = reserved
     */
    int Sharpness;

    /* unsigned short
     * This tag indicates the direction of contrast processing applied by the camera when the image was shot.
     * 0 = Normal
     * 1 = Soft
     * 2 = Hard
     * Other = reserved
     */
    int Contrast;

    /* unsigned short
     * This tag indicates the direction of saturation processing applied by the camera when the image was shot
     * 0 = Normal
     * 1 = Low saturation
     * 2 = High saturation
     * Other = reserved
     */
    int Saturation;

    /* unsigned short
     * This tag indicates the degree of overall image gain adjustment.
     * 0 = None
     * 1 = Low gain up
     * 2 = High gain up
     * 3 = Low gain down
     * 4 = High gain down
     * Other = reserved
     *
     */
    int GainControl;

}ImageInfo_t;



#define EXIT_FAILURE  1
#define EXIT_SUCCESS  0

// jpgfile.c functions
typedef enum {
    READ_METADATA = 1,
    READ_IMAGE = 2,
    READ_ALL = 3,
    READ_ANY = 5        // Don't abort on non-jpeg files.
}ReadMode_t;

typedef struct {
    int Tag;     // tag value, i.e. TAG_MODEL
    int Format;             // format of data
    char* Value;            // value of data in string format
    int DataLength;         // length of string when format says Value is a string
    int GpsTag;             // bool - the tag is related to GPS info
} ExifElement_t;

typedef struct {
    int Tag;
    char * Desc;
    int Format;
    int DataLength;         // Number of elements in Format. -1 means any length.
} TagTable_t;


// prototypes for jhead.c functions
void ErrFatal(const char * msg);
void ErrNonfatal(const char * msg, int a1, int a2);
void FileTimeAsString(char * TimeStr);

// Prototypes for exif.c functions.
int Exif2tm(struct tm * timeptr, char * ExifTime);
void process_EXIF (unsigned char * CharBuf, unsigned int length);
void ShowImageInfo(int ShowFileInfo);
void ShowConciseImageInfo(void);
const char * ClearOrientation(void);
void PrintFormatNumber(void * ValuePtr, int Format, int ByteCount);
double ConvertAnyFormat(void * ValuePtr, int Format);
int Get16u(void * Short);
unsigned Get32u(void * Long);
int Get32s(void * Long);
void Put32u(void * Value, unsigned PutValue);
void create_EXIF(void);

// Added
void create_EXIF_Elements(ExifElement_t* elements, int exifTagCount, int gpsTagCount, int elementTableSize, int hasDateTimeTag);
int IsGpsTag(const char* tag);
int GpsTagNameToValue(const char* tagName);
TagTable_t* GpsTagToTagTableEntry(unsigned short tag);
int TagNameToValue(const char* tagName);
const char* TagValueToName( int tag );
int IsDateTimeTag(unsigned short tag);

static const char ExifAsciiPrefix[] = { 0x41, 0x53, 0x43, 0x49, 0x49, 0x0, 0x0, 0x0 };

//--------------------------------------------------------------------------
// Exif format descriptor stuff
extern const int BytesPerFormat[];
#define NUM_FORMATS 12

#define FMT_BYTE       1 
#define FMT_STRING     2
#define FMT_USHORT     3
#define FMT_ULONG      4
#define FMT_URATIONAL  5
#define FMT_SBYTE      6
#define FMT_UNDEFINED  7
#define FMT_SSHORT     8
#define FMT_SLONG      9
#define FMT_SRATIONAL 10
#define FMT_SINGLE    11
#define FMT_DOUBLE    12


// makernote.c prototypes
extern void ProcessMakerNote(unsigned char * DirStart, int ByteCount,
                 unsigned char * OffsetBase, unsigned ExifLength);

// gpsinfo.c prototypes
void ProcessGpsInfo(unsigned char * ValuePtr,  
                unsigned char * OffsetBase, unsigned ExifLength);

// iptc.c prototpyes
void show_IPTC (unsigned char * CharBuf, unsigned int length);
void ShowXmp(Section_t XmpSection);

// Prototypes for myglob.c module
#ifdef _WIN32
void MyGlob(const char * Pattern , void (*FileFuncParm)(const char * FileName));
void SlashToNative(char * Path);
#endif

// Prototypes for paths.c module
int EnsurePathExists(const char * FileName);
void CatPath(char * BasePath, const char * FilePath);

// Prototypes from jpgfile.c
int ReadJpegSections (FILE * infile, ReadMode_t ReadMode);
void DiscardData(void);
void DiscardAllButExif(void);
int ReadJpegFile(const char * FileName, ReadMode_t ReadMode);
int ReplaceThumbnail(const char * ThumbFileName);
int SaveThumbnail(char * ThumbFileName);
int RemoveSectionType(int SectionType);
int RemoveUnknownSections(void);
int WriteJpegFile(const char * FileName);
Section_t * FindSection(int SectionType);
Section_t * CreateSection(int SectionType, unsigned char * Data, int size);
void ResetJpgfile(void);

// Prototypes from jpgqguess.c
void process_DQT (const uchar * Data, int length);
void process_DHT (const uchar * Data, int length);

// Variables from jhead.c used by exif.c
extern ImageInfo_t ImageInfo;
extern int ShowTags;

//--------------------------------------------------------------------------
// JPEG markers consist of one or more 0xFF bytes, followed by a marker
// code byte (which is not an FF).  Here are the marker codes of interest
// in this program.  (See jdmarker.c for a more complete list.)
//--------------------------------------------------------------------------

#define M_SOF0  0xC0          // Start Of Frame N
#define M_SOF1  0xC1          // N indicates which compression process
#define M_SOF2  0xC2          // Only SOF0-SOF2 are now in common use
#define M_SOF3  0xC3
#define M_SOF5  0xC5          // NB: codes C4 and CC are NOT SOF markers
#define M_SOF6  0xC6
#define M_SOF7  0xC7
#define M_SOF9  0xC9
#define M_SOF10 0xCA
#define M_SOF11 0xCB
#define M_SOF13 0xCD
#define M_SOF14 0xCE
#define M_SOF15 0xCF
#define M_SOI   0xD8          // Start Of Image (beginning of datastream)
#define M_EOI   0xD9          // End Of Image (end of datastream)
#define M_SOS   0xDA          // Start Of Scan (begins compressed data)
#define M_JFIF  0xE0          // Jfif marker
#define M_EXIF  0xE1          // Exif marker.  Also used for XMP data!
#define M_XMP   0x10E1        // Not a real tag (same value in file as Exif!)
#define M_COM   0xFE          // COMment 
#define M_DQT   0xDB          // Define Quantization Table
#define M_DHT   0xC4          // Define Huffmann Table
#define M_DRI   0xDD
#define M_IPTC  0xED          // IPTC marker
