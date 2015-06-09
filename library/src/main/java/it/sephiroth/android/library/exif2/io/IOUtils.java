/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package it.sephiroth.android.library.exif2.io;

import java.io.*;

/**
 * extract from org.apache.commons.io only related functions as the library is too large.
 */
public class IOUtils {

	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static final int EOF = -1;

	public static int copy(final InputStream input, final OutputStream output) throws IOException {
		final long count = copyLarge(input, output);
		if (count > Integer.MAX_VALUE) {
			return -1;
		}
		return (int) count;
	}

	public static long copy(final InputStream input, final OutputStream output, final int bufferSize) throws IOException {
		return copyLarge(input, output, new byte[bufferSize]);
	}

	public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
			throws IOException {
		long count = 0;
		int n = 0;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public static long copyLarge(final InputStream input, final OutputStream output)
			throws IOException {
		return copy(input, output, DEFAULT_BUFFER_SIZE);
	}

	public static void closeQuietly(final InputStream input) {
		closeQuietly((Closeable)input);
	}

	public static void closeQuietly(final OutputStream output) {
		closeQuietly((Closeable)output);
	}

	public static void closeQuietly(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException ioe) {
			// ignore
		}
	}
}
