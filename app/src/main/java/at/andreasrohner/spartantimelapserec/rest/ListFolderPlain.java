package at.andreasrohner.spartantimelapserec.rest;

import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * List Folder for REST API
 */
public class ListFolderPlain {

	/**
	 * Log Tag
	 */
	protected static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Output
	 */
	protected final HttpOutput out;

	/**
	 * Root directory
	 */
	protected final File rootDir;

	/**
	 * Constructor
	 *
	 * @param out     Output
	 * @param rootDir Root directory
	 */
	public ListFolderPlain(HttpOutput out, File rootDir) {
		this.out = out;
		this.rootDir = rootDir;
	}

	public boolean output(String listFolder) throws IOException {
		File listDir = new File(rootDir, listFolder);
		Log.d(TAG, "List dir: " + listDir);
		if (!listDir.isDirectory()) {
			Log.w(TAG, "Directory does not exists");
			return false;
		}

		File[] files = listDir.listFiles();
		if (files == null) {
			Log.w(TAG, "Directory could not be listed");
			return false;
		}

		writeHeader();

		for (File f : files) {
			if (f.getName().charAt(0) == '.') {
				continue;
			}

			writeFile(f);
		}

		return true;
	}

	/**
	 * Write out HTTP Header
	 */
	protected void writeHeader() throws IOException {
		out.sendReplyHeader(HttpThread.ReplyCode.FOUND, "text/plain");
	}

	/**
	 * Write out a file
	 *
	 * @param file File
	 */
	protected void writeFile(File file) throws IOException {
		out.sendLine(file.getName());
	}
}
