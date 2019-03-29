package io.github.vveird.ssdp;


public interface SSDPListener {

	public void notify(SSDPMessage msg);

	public void msearchResponse(SSDPMessage msg);

	public void msearch(SSDPMessage msg);

}
