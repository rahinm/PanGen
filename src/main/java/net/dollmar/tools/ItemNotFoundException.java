package net.dollmar.tools;

/**
 * Exception class for failed finds/removes in search
 * trees, hash tables, and list and tree iterators.
 * @author Mark Allen Weiss
 */
public class ItemNotFoundException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -7530248135165633392L;

    /**
     * Construct this exception object.
     */
    public ItemNotFoundException( ) {
        super( );
    }
    
    /**
     * Construct this exception object.
     * @param message the error message.
     */
    public ItemNotFoundException( String message ) {
        super( message );
    }
}