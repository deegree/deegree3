package org.deegree.commons.json;

public class JSONParsingException extends RuntimeException {
    private String message = "org.deegree.json.JSONParsingException";

    private String stackTrace = "<< is empty >>";

    /**
     * Creates a new instance of <code>XMLProcessingException</code> without detail message.
     */
    protected JSONParsingException() {
        // nothing to do
    }

    /**
     * Constructs an instance of <code>XMLProcessingException</code> with the specified detail message.
     * 
     * @param msg
     *            the detail message.
     */
    public JSONParsingException( String msg ) {
        super();
        message = msg;

    }

    /**
     * Constructs an instance of <code>XMLProcessingException</code> with the specified cause.
     * 
     * @param cause
     *            the Throwable that caused this XMLParsingException
     * 
     */
    public JSONParsingException( Throwable cause ) {
        super( cause );
    }

    /**
     * Constructs an instance of <code>XMLProcessingException</code> with the specified detail message.
     * 
     * @param msg
     *            the detail message.
     * @param e
     */
    public JSONParsingException( String msg, Throwable e ) {
        this( msg );
        if ( e != null ) {
            StackTraceElement[] se = e.getStackTrace();
            StringBuffer sb = new StringBuffer( 1000 );
            for ( int i = 0; i < se.length; i++ ) {
                sb.append( se[i].getClassName() + " " );
                sb.append( se[i].getFileName() + " " );
                sb.append( se[i].getMethodName() + "(" );
                sb.append( se[i].getLineNumber() + ")\n" );
            }
            stackTrace = e.getMessage() + sb.toString();
        }
    }

    @Override
    public String toString() {
        return this.getClass() + ": " + getMessage() + "\n" + stackTrace;
    }

    /**
     *
     */
    @Override
    public String getMessage() {
        return message;
    }
}
