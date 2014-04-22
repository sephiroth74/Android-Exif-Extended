Android-Exif-Extended
=====================

## Description
Exif library for Android.
It's based on the android [ExifInterface] [1]
and on the [Jhead] [2] c library


## Import

Just add this line to your dependency group:

	compile 'it.sephiroth.android.exif:library:+'

## Usage

    import it.sephiroth.android.library.exif2.ExifInterface;
    import it.sephiroth.android.library.exif2.ExifTag;

    ExifInterface exif = new ExifInterface();
    exif.readExif( filename, ExifInterface.Options.OPTION_ALL );

    // list of all tags found
    List<ExifTag> all_tags = exif.getAllTags();

    // jpeg quality
    int jpeg_quality =  exif.getQualityGuess()

    // image size
    int[] imagesize = exif.getImageSize();

    // process used to create the jpeg file
    short process = exif.getJpegProcess();

    // gps lat-lon
    double[] latlon = exif.getLatLongAsDoubles();


    ....
    // save
    exif.writeExif( src_file, dst_file );

## License

This software is licensed under the Apache 2 license, quoted below.

Licensed under the Apache License, Version 2.0 (the "License"); you may not<br>
use this file except in compliance with the License. You may obtain a copy of<br>
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software<br>
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT<br>
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the<br>
License for the specific language governing permissions and limitations under<br>
the License.

## See Also
For more informations about Exif Format see [exifStandard2.pdf] [3]



[1]: http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.4.2_r1/com/android/gallery3d/exif/ExifInterface.java#ExifInterface
[2]: http://www.sentex.net/~mwandel/jhead/
[3]: http://www.kodak.com/global/plugins/acrobat/en/service/digCam/exifStandard2.pdf