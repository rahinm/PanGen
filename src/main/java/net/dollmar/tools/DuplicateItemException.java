package net.dollmar.tools;

/**
 * Exception class for duplicate item errors
 * in search tree insertions.
 * @author Mark Allen Weiss
 */
public class DuplicateItemException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7530248135165633392L;
    
    /**
     * Construct this exception object.
     */
    public DuplicateItemException( ) {
        super( );
    }
    /**
     * Construct this exception object.
     * @param message the error message.
     */
    public DuplicateItemException( String message ) {
        super( message );
    }
}