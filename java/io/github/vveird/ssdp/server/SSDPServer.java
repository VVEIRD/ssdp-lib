package io.github.vveird.ssdp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.vveird.ssdp.RegexHelper;
import io.github.vveird.ssdp.SSDPClient;
import io.github.vveird.ssdp.SSDPListener;
import io.github.vveird.ssdp.SSDPMessage;
import io.github.vveird.ssdp.SSDPStatic;
import io.github.vveird.ssdp.SSDPStatic.SSDPType;

public class SSDPServer {
	
	Map<String, SSDPService> services = new HashMap<>();
	Map<String, NotifyTask> notifiyTasks = new HashMap<>();
	
	List<SSDPClient> clients = null;
	
	List<DiscoverThread> discoveryThreads = new LinkedList<>();
	
	Timer notifier = null;
	
	public SSDPServer() {
		this.notifier = new Timer();
		this.clients = new LinkedList<SSDPClient>();
		try {
			List<InetAddress> ipv4Adresses = new ArrayList<>();
				ipv4Adresses = Arrays
						.asList(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName())).stream()
						.filter(i -> RegexHelper.IP_V_4_PATTERN.matcher(i.toString()).matches()).collect(Collectors.toList());
			// Init SSDP Clients
			for (InetAddress ipv4Adress : ipv4Adresses) {
				try {
					SSDPClient ssdpClient = new SSDPClient(ipv4Adress);
					ssdpClient.setTimeout(SSDPStatic.TIMEOUT);
					clients.add(ssdpClient);
					DiscoverThread dt = new DiscoverThread(ssdpClient);
					discoveryThreads.add(dt);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void registerService(SSDPService s) {
		this.services.put(s.getUSN(), s);
		NotifyTask n = new NotifyTask(s);
		this.notifiyTasks.put(s.getUSN(), n);
		this.notifier.scheduleAtFixedRate(n, 0, s.getNotifyInterval() * 1_000);
	}

	private class NotifyTask extends TimerTask {
		
		SSDPService service = null;
		
		SSDPMessage sm = null;

		public NotifyTask(SSDPService service) {
			super();
			this.service = service;
			HashMap<String, String> header = new HashMap<>();
			header.put("CACHE-CONTROL", "max-age " + service.getNotifyInterval());
			this.sm = new SSDPMessage(SSDPType.NOTIFY, null, service.getLocation(), SSDPStatic.SERVER, service.getST(), service.getUSN(), SSDPStatic.NTS_ALIVE, header, null, 999, "");
		}

		@Override
		public void run() {
			System.out.println("NOTIFY for [" + this.sm.getServiceType() + "] to all interfaces");
			for (SSDPClient client : clients) {
				try {
					client.sendMulticast(this.sm.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//
	// Sub-Classes
	//
	private class DiscoverThread implements Runnable {
		
		private Thread t = null;
		
		private Logger logger = LogManager.getLogger(DiscoverThread.class);
		
		private List<SSDPListener> listeners = new LinkedList<>();
		
		private SSDPClient client = null;
		
		private boolean looping = true;
		
		public DiscoverThread(SSDPClient client) {
			this.client = client;
			this.t= new Thread(this);
			this.t.setDaemon(true);
			this.t.setName("DiscoverThread Daemon " + UUID.randomUUID().toString());
			this.t.start();
		}
		
		public void recieveMsearch(SSDPMessage msg) {
			try {
				// Wait for MSearches for the registered Services
				for (SSDPService ssdpService : SSDPServer.this.services.values()) {
					Matcher urlMatcher = RegexHelper.HTTP_PATTERN.matcher(msg.getLocation());
					HashMap<String, String> header = new HashMap<>();
					header.put("CACHE-CONTROL", "max-age " + ssdpService.getNotifyInterval());
					SSDPMessage sm = new SSDPMessage(
							SSDPType.SSDP_RESPONSE, 
							msg.getIpAddress(), 
							ssdpService.getLocation(),
							SSDPStatic.SERVER, 
							ssdpService.getST(), 
							ssdpService.getUSN(), 
							SSDPStatic.NTS_ALIVE,
							header, 
							null, 
							200, 
							""
					);
					client.sendResponse(sm);
				}
				client.setTimeout(0);
			} catch (IOException e) {
				if (!(e instanceof SocketTimeoutException))
					logger.error("Error encountered while searching for aurora lights", e);
			}
			
		}
		
		@Override
		public void run() {
			do {
				try {
					logger.debug("Waiting for Notify");
					SSDPMessage sm = SSDPMessage.parse(client.multicastReceive(), client);
					recieveMsearch(sm);
					fireSSDPEvent(sm);
					client.setTimeout(0);
				} catch (IOException e) {
					if (!(e instanceof SocketTimeoutException))
						logger.error("Error encountered while searching for aurora lights", e);
				}
			} while(looping);
		}
		
		public void addListener(SSDPListener listener) {
			this.listeners.add(listener);
		}
		
		private void fireSSDPEvent(SSDPMessage msg) {
			logger.debug("SSDP message recieved: " + msg.getSSDPType() + String.format("(ST: %s, USN: %s, Location: %s, NTS: %s)",
					msg.getServiceType(), msg.getUSN(), msg.getLocation(), msg.getNTS()));
			if(msg.isNotify()) {
				for (SSDPListener ssdpListener : listeners) {
					ssdpListener.notify(msg);
				}
			}
			else if(msg.isMSearch()) {
				for (SSDPListener ssdpListener : listeners) {
					ssdpListener.msearch(msg);
				}
			}
			else if(msg.isMSearchResponse()) {
				for (SSDPListener ssdpListener : listeners) {
					ssdpListener.msearchResponse(msg);
				}
			}
		}
	}
}
