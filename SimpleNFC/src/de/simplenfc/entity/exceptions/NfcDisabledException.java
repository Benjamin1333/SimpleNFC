package de.simplenfc.entity.exceptions;

/**
 * Exception thrown, if the capacity of the identified tag is too low.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class NfcDisabledException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Construct a NfcDisabledException.
	 */
	public NfcDisabledException(){}

	
	/**
	 * @return English description of exeption.
	 */
	@Override
	public String getMessage() {
		return "Nfc is disabled.";
	}
}
