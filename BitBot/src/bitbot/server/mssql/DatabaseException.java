package bitbot.server.mssql;

/**
 *
 * @author z
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = -420103154764822555L;

    public DatabaseException(String msg) {
	super(msg);
    }

    public DatabaseException(Throwable cause) {
	super(cause);
    }

    public DatabaseException(String message, Throwable cause) {
	super(message, cause);
    }
}
