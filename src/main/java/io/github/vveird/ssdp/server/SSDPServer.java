package io.github.vveird.ssdp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import io.github.vveird.ssdp.RegexHelper;
import io.github.vveird.ssdp.SSDPClient;
import io.github.vveird.ssdp.SSDPMessage;
import io.github.vveird.ssdp.SSDPStatic;
import io.github.vveird.ssdp.SSDPStatic.SSDPType;

public class SSDPServer {
	

	Map<String, Service> services = new HashMap<>();
	Map<String, NotifyTask> notifiyTasks = new HashMap<>();
	
	List<SSDPClient> clients = null;
	
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
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void registerService(Service s) {
		this.services.put(s.getUSN(), s);
		NotifyTask n = new NotifyTask(s);
		this.notifiyTasks.put(s.getUSN(), n);
		this.notifier.scheduleAtFixedRate(n, 0, s.getNotifyInterval() * 1_000);
	}

	private class NotifyTask extends TimerTask {
		
		Service service = null;
		
		SSDPMessage sm = null;

		public NotifyTask(Service service) {
			super();
			this.service = service;
			HashMap<String, String> header = new HashMap<>();
			header.put("CACHE-CONTROL", "max-age " + service.getNotifyInterval());
			this.sm = new SSDPMessage(SSDPType.NOTIFY, null, service.getLocation(), SSDPStatic.SERVER, service.getST(), service.getUSN(), SSDPStatic.NTS_ALIVE, header, null, 999, "");
		}

		@Override
		public void run() {
			for (SSDPClient client : clients) {
				try {
					client.sendMulticast(this.sm.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
