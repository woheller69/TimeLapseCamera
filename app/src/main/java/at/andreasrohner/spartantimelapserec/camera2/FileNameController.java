package at.andreasrohner.spartantimelapserec.camera2;

import android.content.SharedPreferences;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import at.andreasrohner.spartantimelapserec.ImageRecorderState;
import at.andreasrohner.spartantimelapserec.rest.HttpThread;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Controller for output filenames
 */
public class FileNameController {

	/**
	 * Log Tag
	 */
	private static final String TAG = HttpThread.class.getSimpleName();

	/**
	 * Project name
	 */
	private final String projectName;

	/**
	 * Output path
	 */
	private File outputDir;

	/**
	 * File Numbering
	 */
	private int fileIndex = 0;

	/**
	 * Constructor
	 *
	 * @param prefs SharedPreferences
	 */
	public FileNameController(SharedPreferences prefs) {
		String projectPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath();
		this.projectName = prefs.getString("pref_project_title", "NO_NAME");
		this.outputDir = new File(projectPath, projectName + "/" + DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()) + "/");
		Log.i(TAG, "Project Folder: «" + this.outputDir + "»");
	}

	/**
	 * Get Next output filename
	 *
	 * @param ext Extension
	 * @return File
	 * @throws IOException
	 */
	protected File getOutputFile(String ext) throws IOException {
		if (!outputDir.exists()) {
			if (!outputDir.mkdirs()) {
				throw new IOException("Could not create folder «" + outputDir + "»");
			}
		}

		File outFile;
		do {
			outFile = new File(outputDir, projectName + fileIndex + "." + ext);
			fileIndex++;
		} while (outFile.isFile());

		if (!outputDir.isDirectory()) {
			throw new IOException("Could not open directory «" + outputDir + "»");
		}

		ImageRecorderState.setCurrentImage(outFile);

		return outFile;
	}
}
