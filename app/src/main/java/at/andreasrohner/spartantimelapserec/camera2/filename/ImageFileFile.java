package at.andreasrohner.spartantimelapserec.camera2.filename;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * Wrapper for Files
 */
public class ImageFileFile implements ImageFile {

	/**
	 * @return Get Root Dir
	 */
	public static ImageFileFile root() {
		File rootDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath());
		return new ImageFileFile(rootDir);
	}

	/**
	 * File
	 */
	private final File file;

	/**
	 * Constructor
	 *
	 * @param file File
	 */
	public ImageFileFile(File file) {
		this.file = file;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public InputStream openInputStream(Context context) throws FileNotFoundException {
		return new FileInputStream(file);
	}

	@Override
	public long lastModified() {
		return file.lastModified();
	}

	@Override
	public String getRelativeUrl() {
		File rootDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath());
		return rootDir.toURI().relativize(file.toURI()).getPath();
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public boolean isFile() {
		return file.isFile();
	}

	@Override
	public ImageFile child(String child) {
		return new ImageFileFile(new File(file, child));
	}

	@Override
	public List<ImageFile> listFiles() {
		List<ImageFile> list = new ArrayList<>();

		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				list.add(new ImageFileFile(f));
			}
		}

		return list;
	}
}