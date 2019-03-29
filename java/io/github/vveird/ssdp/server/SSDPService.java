package io.github.vveird.ssdp.server;

import io.github.vveird.ssdp.SSDPListener;

public class SSDPService {
	
	private String ST  = null;
	private String USN = null;
	private String LOCATION = null;
	
	private SSDPListener serviceListener = null;
	
	/**
	 * Intervall in seconds in which a notify ssdp message should be sent out. Is
	 * also the max-age for caching purposes.
	 */
	private int notifyInterval = 60;

	public SSDPService(String sT, String uSN, String lOCATION, SSDPListener serviceListener, int notifyInterval) {
		super();
		this.ST = sT;
		this.USN = uSN;
		this.LOCATION = lOCATION;
		this.serviceListener = serviceListener;
		this.notifyInterval = notifyInterval;
	}

	public String getST() {
		return ST;
	}

	public void setST(String sT) {
		ST = sT;
	}

	public String getUSN() {
		return USN;
	}

	public void setUSN(String uSN) {
		USN = uSN;
	}

	public String getLocation() {
		return LOCATION;
	}

	public void setLOCATION(String lOCATION) {
		LOCATION = lOCATION;
	}

	public SSDPListener getServiceListener() {
		return serviceListener;
	}

	public void setServiceListener(SSDPListener serviceListener) {
		this.serviceListener = serviceListener;
	}
	
	public static ServiceBuilder builder() {
		return new ServiceBuilder();
	}
	
	public int getNotifyInterval() {
		return notifyInterval;
	}
	
	public void setNotifyInterval(int notifyInterval) {
		this.notifyInterval = notifyInterval;
	}
	
	public static class ServiceBuilder {

		private String ST  = null;
		private String USN = null;
		private String LOCATION = null;
		
		private SSDPListener serviceListener = null;
		
		private int notifyInterval = 60;
		
		public ServiceBuilder st(String st) {
			this.ST = st;
			return this;
		}
		
		public ServiceBuilder usn(String usn) {
			this.USN = usn;
			return this;
		}
		
		public ServiceBuilder location(String location) {
			this.LOCATION = location;
			return this;
		}
		
		public ServiceBuilder serviceListener(SSDPListener serviceListener) {
			this.serviceListener = serviceListener;
			return this;
		}
		
		public ServiceBuilder notifyInterval(int notifyInterval) {
			this.notifyInterval = notifyInterval;
			return this;
		}
		
		public SSDPService build() {
			return new SSDPService(ST, USN, LOCATION, serviceListener, notifyInterval);
		}
	}

}
