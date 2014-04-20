package it.sephiroth.android.example.exifsample.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Images.ImageColumns;

public class IOUtils {

	public static String getRealFilePath( final Context context, final Uri uri ) {

		if ( null == uri ) return null;

		final String scheme = uri.getScheme();
		String data = null;

		if ( scheme == null )
			data = uri.getPath();
		else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
			data = uri.getPath();
		} else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
			Cursor cursor;
			try {
				cursor = context.getContentResolver().query( uri, new String[]{ ImageColumns.DATA }, null, null, null );
			} catch( IllegalArgumentException e ) {
				e.printStackTrace();
				return null;
			}
			if ( null != cursor ) {
				if ( cursor.moveToFirst() ) {
					int index = cursor.getColumnIndex( ImageColumns.DATA );
					if ( index > -1 ) {
						data = cursor.getString( index );
					}
				}
				cursor.close();
			}
		}
		return data;
	}
}
