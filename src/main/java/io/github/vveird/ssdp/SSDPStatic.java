package io.github.vveird.ssdp;

import java.util.Arrays;

public class SSDPStatic {
	
	public static final String SERVER = System.getProperty("os.name").substring(0, 3) + "/" + System.getProperty("os.version") + " UPnP/1.0 VSSDPServ 0.1";

	/**
	 * SSDP Multicast address
	 */
	public static final String MULTICAST_ADDRESS = "239.255.255.250";

	/**
	 * SSDP Multicast port
	 */
	public static final int MULTICAST_PORT = 1900;

	/**
	 * New line definition
	 */
	public static final String NEWLINE = "\r\n";

	/* Definitions of notification sub type */
	public static final String NTS_ALIVE = "ssdp:alive";
	public static final String NTS_BYEBYE = "ssdp:byebye";
	public static final String NTS_UPDATE = "ssdp:update";
	public static int TIMEOUT = 5_000;

	public static enum SSDPType {
		SSDP_RESPONSE("SSDP-RESPONSE", "HTTP/1.1"), 
		M_SEARCH("M-SEARCH", "M-SEARCH * HTTP/1.1"),
		NOTIFY("NOTIFY", "NOTIFY * HTTP/1.1");

		private String type = null;
		private String header = null;

		@Override
		public String toString() {
			return header;
		}

		public String getHeader() {
			return header;
		}

		public String getType() {
			return type;
		}

		private SSDPType(String type, String header) {
			this.type = type;
			this.header = header;
		}
		
		public static SSDPType valueOf(Object key) {
			return Arrays.stream(values()).filter(t -> t.getType().equals(key)).findFirst().orElse(null);
		}
	}

}
