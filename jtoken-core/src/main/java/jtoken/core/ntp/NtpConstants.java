package jtoken.core.ntp;

public abstract class NtpConstants {

	/**
	 * Standard NTP UDP port
	 */
	public static final int NTP_PORT = 123;

	public static final int LI_NO_WARNING = 0;
	public static final int LI_LAST_MINUTE_HAS_61_SECONDS = 1;
	public static final int LI_LAST_MINUTE_HAS_59_SECONDS = 2;
	public static final int LI_ALARM_CONDITION = 3;

	/* mode options */
	public static final int MODE_RESERVED = 0;
	public static final int MODE_SYMMETRIC_ACTIVE = 1;
	public static final int MODE_SYMMETRIC_PASSIVE = 2;
	public static final int MODE_CLIENT = 3;
	public static final int MODE_SERVER = 4;
	public static final int MODE_BROADCAST = 5;
	public static final int MODE_CONTROL_MESSAGE = 6;
	public static final int MODE_PRIVATE = 7;

	public static final int NTP_MINPOLL = 4; // 16 seconds
	public static final int NTP_MAXPOLL = 14; // 16284 seconds

	public static final int NTP_MINCLOCK = 1;
	public static final int NTP_MAXCLOCK = 10;

	public static final int VERSION_3 = 3;
	public static final int VERSION_4 = 4;
}
