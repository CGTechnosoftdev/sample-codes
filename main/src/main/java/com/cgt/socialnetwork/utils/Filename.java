package com.cgt.socialnetwork.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

// TODO: Auto-generated Javadoc

/**
 * Created by CGTechnosoft
 */
public class Filename {

	/** The full path. */
	private String fullPath;

	/** The extension separator. */
	private char pathSeparator, extensionSeparator;

	/**
	 * Instantiates a new filename.
	 * 
	 * @param str
	 *            the str
	 * @param sep
	 *            the sep
	 * @param ext
	 *            the ext
	 */
	public Filename(String str, char sep, char ext) {
		fullPath = str;
		pathSeparator = sep;
		extensionSeparator = ext;
	}

	/**
	 * Extension.
	 * 
	 * @return the string
	 */
	public String extension() {
		int dot = fullPath.lastIndexOf(extensionSeparator);
		return fullPath.substring(dot + 1);
	}

	public int getFileSizeInKB(String sourceFilePath) {
		File sourcePath = new File(sourceFilePath);
		double bytes = sourcePath.length();
		double kilobytes = (bytes / 1024);		
		int m =(int)kilobytes;
		return m; 
	}

	/**
	 * Filename.
	 * 
	 * @return the string
	 */
	public String filename() { // gets filename without extension
		int dot = fullPath.lastIndexOf(extensionSeparator);
		int sep = fullPath.lastIndexOf(pathSeparator);
		return fullPath.substring(sep + 1, dot);
	}

	/**
	 * Path.
	 * 
	 * @return the string
	 */
	public String path() {
		int sep = fullPath.lastIndexOf(pathSeparator);
		return fullPath.substring(0, sep);
	}

	public static File copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		return dst;
	}
	


	public String getRandomName() {
		String filename;
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
		filename = sdf.format(date);
		;
		return filename;
	}
}
