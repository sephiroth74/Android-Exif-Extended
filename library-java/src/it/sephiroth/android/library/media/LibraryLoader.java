package it.sephiroth.android.library.media;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.io.File;

/**
 * Created by alessandro on 08/04/14.
 * <p/>
 * Used to replace the regular android System.loadLibrary
 * as a workaround for the PackageManager bug which sometimes
 * doesn't install the shared libraries in the correct location
 */
class LibraryLoader {
	static final String LOG_TAG = "LibraryLoader";


	// Guards all access to the libraries
	private static Object sLock = new Object();

	// One-way switch becomes true when the libraries are initialized
	private static boolean sInitialized = false;

	// Prefix string of app's native library directory,
	// e.g., /data/app-lib/com.android.chrome-
	private static String sNativeLibraryDirPrefix = null;
	private static boolean sNativeLibraryHack;

	public static void ensureInitialized( Context context ) {
		synchronized( sLock ) {
			if( sInitialized ) {
				return;
			}
			Log.i( LOG_TAG, "ensureInitialized" );
			initializeNativeLibraryDirPrefix( context );
		}
	}


	/**
	 * Checks if library is fully loaded and initialized.
	 */
	public static boolean isInitialized() {
		synchronized( sLock ) {
			return sInitialized;
		}
	}

	private static void initializeNativeLibraryDirPrefix( Context context ) {
		if( sNativeLibraryDirPrefix != null ) {
			return;
		}

		Log.i( LOG_TAG, "initializeNativeLibraryDirPrefix" );

		// From JB and up, an updated version has native library directory like
		// /data/app-lib/com.android.chrome-N, where com.android.chrome is the
		// package name, and N is either 1 or 2.
		ApplicationInfo appInfo = context.getApplicationInfo();
		String nativeLibDir = appInfo.nativeLibraryDir;

		// Sanity checks.
		if( nativeLibDir == null ) {
			return;
		}

		String packageName = appInfo.packageName;
		int idx = nativeLibDir.lastIndexOf( packageName );
		if( idx == - 1 ) {
			return;
		}

		sNativeLibraryDirPrefix = nativeLibDir.substring( 0, idx + packageName.length() ) + "-";
	}


	// Load a library using system library loader (vs Linker).
	//
	// The implementation first tries System.loadLibrary, and if it fails
	// try a workaround of http://b/13216167.
	//
	// More details about http://b/13216167:
	//   PackageManager may fail to update shared library.
	//
	// Native library directory in an updated package is a symbolic link
	// to a directory in /data/app-lib/<package name>, for example:
	// /data/data/com.android.chrome/lib -> /data/app-lib/com.android.chrome[-1].
	// When updating the application, the PackageManager create a new directory,
	// e.g., /data/app-lib/com.android.chrome-2, and remove the old symlink and
	// recreate one to the new directory. However, on some devices (e.g. Sony Xperia),
	// the old directory was deleted, but deleting the old symlink failed,
	// and that makes system loading library from the /system/lib directory.
	//
	//  We make the following changes to alleviate the issue:
	//  1) name the native library with build id, e.g., libchrome.1750.135.so,
	//     1750 is the unique release branch name, 135 is the unique of each branch release.
	//  2) first try to load the library using System.loadLibrary,
	//     if that failed due to the file was not found,
	//     search the named library in other directories.
	//     Because of change 1, each version has a different native
	//     library name, so avoids mistakenly using the old native library.
	public static void loadLibrary( String library ) throws UnsatisfiedLinkError {

		Log.i( LOG_TAG, "loadLibrary: " + library );

		try {
			System.loadLibrary( library );
		} catch( UnsatisfiedLinkError e ) {
			Log.w( LOG_TAG, "------------------------------------" );
			Log.w( LOG_TAG, "failed to load " + library + " using System.loadLibrary" );
			Log.w( LOG_TAG, "switching to a secondary method" );
			Log.w( LOG_TAG, "------------------------------------" );
			Log.w( LOG_TAG, "catching: " + e.getMessage() );

			assert sInitialized;

			// Android PackageManager usually flips 1 and 2.
			if( sNativeLibraryDirPrefix != null ) {
				int i;
				String libPath;
				final String libName = System.mapLibraryName( library );

				// Trying using the android 4+ system path
				// /data/app-lib/com.aviary.android.feather-1/...
				for( i = 1; i <= 2; i++ ) {
					libPath = sNativeLibraryDirPrefix + i + "/" + libName;
					Log.d( LOG_TAG, "trying with libPath: " + libPath );

					File f = new File( libPath );
					if( f.exists() ) {
						Log.d( LOG_TAG, "loading library " + libPath );
						System.load( libPath );
						sNativeLibraryHack = true;
						return;
					}
				}

				// on android 2.3 the native library it's something like
				// /mnt/asec/com.aviary.android.feather/lib/...
				for( i = 1; i <= 2; i++ ) {
					libPath = sNativeLibraryDirPrefix + i + "/lib/" + libName;
					Log.d( LOG_TAG, "trying with libPath: " + libPath );

					File f = new File( libPath );
					if( f.exists() ) {
						Log.d( LOG_TAG, "loading library " + libPath );
						System.load( libPath );
						sNativeLibraryHack = true;
						return;
					}
				}
			}
			else {
				Log.w( LOG_TAG, "Couldn't load shared library because sNativeLibraryDirPrefix is null" );
			}

			Log.e( LOG_TAG, "failed to load the shared library " + library );
			throw e;
		}
	}
}
