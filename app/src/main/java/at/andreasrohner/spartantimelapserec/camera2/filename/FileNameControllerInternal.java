package at.andreasrohner.spartantimelapserec.camera2.filename;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import at.andreasrohner.spartantimelapserec.ImageRecorderState;
import at.andreasrohner.spartantimelapserec.state.Logger;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Store the files in the internal storage
 */
public class FileNameControllerInternal extends AbstractFileNameController {

	/**
	 * Logger
	 */
	protected Logger logger = new Logger(getClass());

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

		logger.info("Project Folder: «{}»", this.outputDir);
	}

	@Override
	public ImageOutput getOutputFile(String ext) throws IOException {
		if (!outputDir.exists()) {
			if (!outputDir.mkdirs()) {
				throw new IOException("Could not create folder «" + outputDir + "»");
			}
		}

		File outFile;
		String fileName;
		do {
			fileName = projectName + fileIndex + "." + ext;
			outFile = new File(outputDir, fileName);
			fileIndex++;
		} while (outFile.isFile());

		if (!outputDir.isDirectory()) {
			throw new IOException("Could not open directory «" + outputDir + "»");
		}

		ImageRecorderState.setCurrentImage(new ImageFileFile(outFile));

		return new ImageOutput(fileName, new FileOutputStream(outFile));
	}
}
