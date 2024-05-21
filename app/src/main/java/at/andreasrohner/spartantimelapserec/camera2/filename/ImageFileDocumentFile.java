package at.andreasrohner.spartantimelapserec.camera2.filename;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.system.Os;
import android.system.StructStatVfs;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.documentfile.provider.DocumentFile;
import at.andreasrohner.spartantimelapserec.state.Logger;

/**
 * Wrapper for DocumentFile
 */
public class ImageFileDocumentFile implements ImageFile {

	/**
	 * Logger
	 */
	private Logger logger = new Logger(getClass());

	/**
	 * File
	 */
	private final DocumentFile file;

	/**
	 * Constructor
	 *
	 * @param file File
	 */
	public ImageFileDocumentFile(DocumentFile file) {
		this.file = file;
	}

	@Override
	public String getName() {
		if (file == null) {
			return null;
		}

		return file.getName();
	}

	@Override
	public long length() {
		if (file == null) {
			return 0;
		}

		return file.length();
	}

	@Override
	public InputStream openInputStream(Context context) throws FileNotFoundException {
		if (file == null) {
			return null;
		}

		return context.getContentResolver().openInputStream(file.getUri());
	}

	@Override
	public long lastModified() {
		if (file == null) {
			return 0;
		}

		return file.lastModified();
	}

	@Override
	public String getRelativeUrl() {
		// This implementation only works for images, but currently is only use for this.
		// Not the best solution, this may should be improved later...

		DocumentFile p1 = file.getParentFile();
		DocumentFile p2 = p1.getParentFile();

		return p2.getName() + "/" + p1.getName() + "/" + file.getName();
	}

	@Override
	public boolean isDirectory() {
		if (file == null) {
			return false;
		}

		return file.isDirectory();
	}

	@Override
	public boolean isFile() {
		if (file == null) {
			return false;
		}

		return file.isFile();
	}

	@Override
	public ImageFile child(String child) {
		if (file == null) {
			return new ImageFileDocumentFile(null);
		}

		if (child.isEmpty()) {
			return this;
		}

		if (child.contains("/")) {
			ImageFile f = this;

			String[] parts = child.split("/");
			for (String p : parts) {
				if (p.isEmpty()) {
					continue;
				}

				f = f.child(p);
			}

			return f;
		}

		return new ImageFileDocumentFile(file.findFile(child));
	}

	@Override
	public List<ImageFile> listFiles() {
		List<ImageFile> list = new ArrayList<>();

		DocumentFile[] files = file.listFiles();
		if (files != null) {
			for (DocumentFile f : files) {
				list.add(new ImageFileDocumentFile(f));
			}
		}

		return list;
	}

	@Override
	public synchronized long getFreeSpace(Context context) {
		try {
			DocumentFile createdFile = file.createFile("plain/text", "TimeLapseCamera.txt");
			ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(createdFile.getUri(), "w");
			assert pfd != null;
			StructStatVfs stats = Os.fstatvfs(pfd.getFileDescriptor());
			createdFile.delete();
			return stats.f_bavail * stats.f_bsize;
		} catch (Exception e) {
			logger.error("Could not get free space for «{}»", file.getUri(), e);
			return -1;
		}
	}
}