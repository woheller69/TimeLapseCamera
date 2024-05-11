package at.andreasrohner.spartantimelapserec.rest;

import android.util.Log;

import java.io.IOException;
import java.util.List;

import at.andreasrohner.spartantimelapserec.camera2.filename.ImageFile;

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
	protected final ImageFile rootDir;

	/**
	 * Constructor
	 *
	 * @param out     Output
	 * @param rootDir Root directory
	 */
	public ListFolderPlain(HttpOutput out, ImageFile rootDir) {
		this.out = out;
		this.rootDir = rootDir;
	}

	/**
	 * List the folder
	 *
	 * @param listFolder Folder
	 * @return true on success
	 * @throws IOException
	 */
	public boolean output(String listFolder) throws IOException {
		ImageFile listDir = rootDir.child(listFolder);
		Log.d(TAG, "List dir: " + listDir);
		if (!listDir.isDirectory()) {
			Log.w(TAG, "Directory does not exists");
			return false;
		}

		List<ImageFile> files = listDir.listFiles();
		if (files.isEmpty()) {
			Log.w(TAG, "Directory could not be listed");
			return false;
		}

		writeHeader();

		for (ImageFile f : files) {
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
		out.sendReplyHeader(ReplyCode.FOUND, "text/plain");
	}

	/**
	 * Write out a file
	 *
	 * @param file File
	 */
	protected void writeFile(ImageFile file) throws IOException {
		out.sendLine(file.getName());
	}
}
