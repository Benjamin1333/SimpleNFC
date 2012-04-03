package de.simplenfc.entity.exceptions;

/**
 * Exception thrown, if the identified tag is read-only.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class ReadOnlyException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * @return English description of exception
	 */
	@Override
	public String getMessage() {
		return "Tag is read-only.";
	}
}
