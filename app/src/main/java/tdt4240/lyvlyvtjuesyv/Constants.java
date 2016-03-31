package tdt4240.lyvlyvtjuesyv;

/**
 * Created by RayTM on 30.03.2016.
 */
public class Constants {
    private static final String PROJECT_LOCATION = "tdt4240.lyvlyvtjuesyv.";

    /** Keys **/
    public static final String IS_HOST_ADD = PROJECT_LOCATION + "IS_HOST";
    public static final String SERVER_ADDRESS_ADD = PROJECT_LOCATION + "SERVER_ADDRESS";
    public static final String SERVER_PORT_ADD = PROJECT_LOCATION + "SERVER_PORT";

    /** Server communication **/
    public static final char STATUS_ACTIVE = 'a';
    public static final char STATUS_NO_ANSWER = 'n';

    public static final char REQUEST_STATUS = 's';

    /** Variables **/
    public static final int DEFAULT_PORT = 56694;
    public static final String HOME_ADDRESS = "localhost";
    public static final long PING_INTERVAL = 5000;
    public static final long RESPONSE_TIMEOUT = 10000;
    public static final long RESPONSE_CHECK_INTERVAL = 100;

    /** Prevent instancing **/
    private Constants() {}
}
