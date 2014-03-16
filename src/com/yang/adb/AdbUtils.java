package com.yang.adb;

import java.io.Closeable;
import java.io.IOException;

public class AdbUtils {
	public static boolean safeClose(Closeable paramCloseable) {
		if (paramCloseable == null)
			return false;
		try {
			paramCloseable.close();
			return true;
		} catch (IOException localIOException) {
		}
		return false;
	}

}
