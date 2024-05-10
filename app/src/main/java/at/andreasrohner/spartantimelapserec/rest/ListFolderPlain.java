package at.andreasrohner.spartantimelapserec.rest;

import java.io.IOException;
import java.util.List;

import at.andreasrohner.spartantimelapserec.camera2.filename.ImageFile;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * List Folder for REST API
 */
public class ListFolderPlain {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

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
		logger.debug("List dir: {}", listDir);
		if (!listDir.isDirectory()) {
			logger.warn("Directory «{}» does not exists", listFolder);
			return false;
		}

		List<ImageFile> files = listDir.listFiles();
		if (files.isEmpty()) {
			logger.warn("Directory «{}» could not be listed", listFolder);
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
