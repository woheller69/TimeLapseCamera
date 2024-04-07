package at.andreasrohner.spartantimelapserec.rest;

/**
 * HTTP Reply codes
 */
public enum ReplyCode {

	/**
	 * Found
	 */
	FOUND(200, "Found"),

	/**
	 * Forbidden
	 */
	FORBIDDEN(403, "Forbidden"),

	/**
	 * File was not found
	 */
	NOT_FOUND(404, "Not found");

	/**
	 * HTTP Code
	 */
	public final int code;

	/**
	 * Result Text
	 */
	public final String text;

	/**
	 * Constructor
	 *
	 * @param code HTTP Code
	 * @param text Result Text
	 */
	private ReplyCode(int code, String text) {
		this.code = code;
		this.text = text;
	}
}
