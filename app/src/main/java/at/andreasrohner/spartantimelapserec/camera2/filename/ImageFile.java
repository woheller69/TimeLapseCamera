package at.andreasrohner.spartantimelapserec.camera2.filename;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Image file interface
 */
public interface ImageFile {

	/**
	 * @return File name
	 */
	String getName();

	/**
	 * @return Size of the file
	 */
	long length();

	/**
	 * Open file as input stream
	 *
	 * @param context Context
	 * @return InputStream
	 */
	InputStream openInputStream(Context context) throws FileNotFoundException;

	/**
	 * @return Timestamp
	 */
	long lastModified();

	/**
	 * @return The relative URL from root folder
	 */
	String getRelativeUrl();

	/**
	 * @return true if a directory
	 */
	boolean isDirectory();

	/**
	 * @return true if a file
	 */
	boolean isFile();

	/**
	 * Get a subfolder / child folder
	 *
	 * @param child Child name
	 * @return Child Folder
	 */
	ImageFile child(String child);

	/**
	 * @return Files in folder
	 */
	List<ImageFile> listFiles();

	/**
	 * @param context Context
	 * @return Free space in bytes
	 */
	long getFreeSpace(Context context);
}