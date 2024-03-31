package at.andreasrohner.spartantimelapserec.rest;

import java.io.IOException;

/**
 * HTTP Output writer
 */
public interface HttpOutput {

	/**
	 * Send HTTP Header
	 *
	 * @param code        Code
	 * @param contentType Content Type
	 * @throws IOException
	 */
	public void sendReplyHeader(ReplyCode code, String contentType) throws IOException;

	/**
	 * Send Line
	 *
	 * @param line Line
	 */
	public void sendLine(String line) throws IOException;
}
