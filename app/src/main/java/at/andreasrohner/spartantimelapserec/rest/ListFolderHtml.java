package at.andreasrohner.spartantimelapserec.rest;

import java.io.IOException;

import at.andreasrohner.spartantimelapserec.camera2.filename.ImageFile;

/**
 * List Folder user clickable
 */
public class ListFolderHtml extends ListFolderPlain {

	/**
	 * Current listing folder
	 */
	private String listFolder;

	/**
	 * Constructor
	 *
	 * @param out     Output
	 * @param rootDir Root directory
	 */
	public ListFolderHtml(HttpOutput out, ImageFile rootDir) {
		super(out, rootDir);
	}

	@Override
	public boolean output(String listFolder) throws IOException {
		this.listFolder = listFolder;
		boolean result = super.output(listFolder);
		if (result) {
			out.sendLine("</body>");
		}
		return result;
	}

	@Override
	protected void writeHeader() throws IOException {
		out.sendReplyHeader(ReplyCode.FOUND, "text/html");
		out.sendLine("<html><head><title>TimeLapsCam</title></head><body>");
		out.sendLine("<h1>List folder</h1>");
	}

	@Override
	protected void writeFile(ImageFile file) throws IOException {
		// No escaping... The File should not contiain special chars, so keep it simple
		if (file.isDirectory()) {
			out.sendLine("<a href=\"/1/img" + listFolder + "/" + file.getName() + "/listhtml\">" + file.getName() + "</a><br>");
		} else {
			out.sendLine("<a href=\"/1/img" + listFolder + "/" + file.getName() + "\">" + file.getName() + "</a><br>");
		}
	}
}
