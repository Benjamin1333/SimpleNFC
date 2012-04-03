package de.simplenfc.entity.exceptions;

/**
 * Exception thrown, if the capacity of the identified tag is too low.
 * 
 * @author Benjamin R&uuml;hl (simplenfc@benjamin-ruehl.de)
 * @author Dennis Becker (simplenfc@denbec.de)
 * @version 1.0
 *
 */
public class LowCapacityException extends Exception {
	private static final long serialVersionUID = 1L;
	private int capacity, messageSize;
	
	/**
	 * Construct a LowCapacityException with tag-capacity and actual message size.
	 * @param capacity Capacity of the read tag.
	 * @param messageSize Size of the message to write.
	 */
	public LowCapacityException(int capacity, int messageSize){
		this.capacity = capacity;
		this.messageSize = messageSize;
	}
	
	/**
	 * @return English description of exeption, including size information of tag and message.
	 */
	@Override
	public String getMessage() {
		return "Tag capacity is " + capacity + " bytes, message needs " + messageSize + " bytes.";
	}
}
