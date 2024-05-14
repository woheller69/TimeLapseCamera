package at.andreasrohner.spartantimelapserec.camera2.filename;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateFormat;

import java.io.IOException;
import java.io.OutputStream;

import androidx.documentfile.provider.DocumentFile;
import at.andreasrohner.spartantimelapserec.ImageRecorderState;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Store the files on the external SD card
 */
public class FileNameControllerExternal extends AbstractFileNameController {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * Output path
	 */
	private final DocumentFile outputDir;

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public FileNameControllerExternal(Context context) {
		super(context);

		String externalStoragePath = prefs.getString("external_storage_path", null);
		Uri currentUri = Uri.parse(externalStoragePath);
		DocumentFile rootDir = DocumentFile.fromTreeUri(context, currentUri);
		DocumentFile dir1 = rootDir.findFile(projectName);
		if (dir1 == null || !dir1.exists()) {
			dir1 = rootDir.createDirectory(projectName);
		}

		if (dir1 == null) {
			this.outputDir = null;
			logger.error("Error, could not write external path! Select path again!");
			return;
		}

		String dateString = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString();
		DocumentFile dir2 = dir1.findFile(dateString);
		if (dir2 == null || !dir2.exists()) {
			dir2 = dir1.createDirectory(dateString);
		}

		this.outputDir = dir2;

		logger.info("Project Folder: «{}»", this.outputDir.getUri());
	}

	/**
	 * Get Next output filename
	 *
	 * @param ext Extension
	 * @return File
	 * @throws IOException
	 */
	public OutputStream getOutputFile(String ext) throws IOException {
		if (this.outputDir == null) {
			throw new IOException("Cannot write to external drive");
		}

		DocumentFile outFile = null;
		do {
			String name = projectName + fileIndex + "." + ext;

			// File not found / does not exists
			if (this.outputDir.findFile(name) == null) {
				outFile = this.outputDir.createFile("image/jpeg", name);
				if (outFile == null) {
					throw new IOException("Could not create file «" + name + "»");
				}
			}

			fileIndex++;
		} while (outFile == null);

		ImageRecorderState.setCurrentImage(new ImageFileDocumentFile(outFile));

		return context.getContentResolver().openOutputStream(outFile.getUri());
	}
}
