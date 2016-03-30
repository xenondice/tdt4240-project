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
    public static final String SERVER_STATUS_ADD = PROJECT_LOCATION + "SERVER_STATUS";

    /** Stations **/
//    public static final String SERVER_BROADCAST_ADD = PROJECT_LOCATION + "SERVER_BROADCAST";
//    public static final String HOST_BROADCAST_ADD = PROJECT_LOCATION + "HOST_BROADCAST";

    /** Statuses **/
//    public static final int SERVER_STATUS_ACTIVE = 1;
//    public static final int HOST_STATUS_STOP = 10;

    /** Variables **/
    public static final int DEFAULT_PORT = 56694;
    public static final String HOME_ADDRESS = "localhost";
    public static final long PING_INTERVAL = 500;

    /** Prevent instancing **/
    private Constants() {}
}
