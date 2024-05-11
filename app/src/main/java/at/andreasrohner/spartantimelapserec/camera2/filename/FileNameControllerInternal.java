package at.andreasrohner.spartantimelapserec.camera2.filename;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import at.andreasrohner.spartantimelapserec.ImageRecorderState;
import at.andreasrohner.spartantimelapserec.state.StateLog;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Store the files in the internal storage
 */
public class FileNameControllerInternal extends AbstractFileNameController {

	/**
	 * Log Tag
	 */
	private static final String TAG = FileNameControllerInternal.class.getSimpleName();

	/**
	 * Output path
	 */
	private File outputDir;

	/**
	 * Constructor
	 *
	 * @param context Context
	 */
	public FileNameControllerInternal(Context context) {
		super(context);

		String projectPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath();
		this.outputDir = new File(projectPath, projectName + "/" + DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()) + "/");

		Log.i(TAG, "Project Folder: «" + this.outputDir + "»");
		StateLog.addEntry("Project Path", this.outputDir.toString());
	}

	@Override
	public OutputStream getOutputFile(String ext) throws IOException {
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

		ImageRecorderState.setCurrentImage(new ImageFileFile(outFile));

		return new FileOutputStream(outFile);
	}
}
