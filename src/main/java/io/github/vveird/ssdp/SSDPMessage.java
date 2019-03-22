package io.github.vveird.ssdp;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.vveird.ssdp.SSDPStatic.SSDPType;


/**
 * Represent a SSDPMessage discovered by SSDP.
 * 
 * Source: https://github.com/vmichalak/ssdp-client
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Valentin Michalak
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class SSDPMessage {
    /* New line definition */ 

	

	
	private static long receiveTime;
	
	private final SSDPClient source;
	
    private final SSDPType ssdpType;
    private final int ssdpResponseCode;
    private final String ssdpResponseDescriptor;
    private final String ip;
    private final String descriptionUrl;
    private final String server;
    private final String serviceType;
    private final String usn;
    private final String nts;
    private final Map<String, String> headers;

	public SSDPMessage(SSDPType ssdpType, String ip, String descriptionUrl, String server, String serviceType,
			String usn, String nts, Map<String, String> headers, SSDPClient source, int ssdpResponseCode,
			String ssdpResponseDescriptor) {
    	this.ssdpType = ssdpType;
    	this.ssdpResponseCode = ssdpResponseCode;
    	this.ssdpResponseDescriptor = ssdpResponseDescriptor;
        this.ip = ip;
        this.descriptionUrl = descriptionUrl;
        this.server = server;
        this.serviceType = serviceType;
        this.usn = usn;
        this.nts = nts;
        this.headers = headers;
        this.source = source;
        this.receiveTime = System.currentTimeMillis();
    }

    /**
     * Instantiate a new SSDPMessage Object from a SSDP discovery response packet.
     * @param ssdpResult SSDP Discovery Response packet.
     * @return SSDPMessage
     */
    public static SSDPMessage parse(DatagramPacket ssdpResult, SSDPClient source) {
        HashMap<String, String> headers = new HashMap<String, String>();
    	SSDPType ssdpType = null;
        Pattern pattern = Pattern.compile("(.*): (.*)");
        Pattern httpResponsePattern = Pattern.compile("^(HTTP|http)/(?<httpversion>(1|2)\\.\\d) (?<httpresponsecode>\\d{3}) (?<httpresponsedescriptor>.*)");
        Pattern ssdpPattern = Pattern.compile("(?<ssdptype>.*) \\* HTTP/(?<httpversion>.*)");

        String[] lines = new String(ssdpResult.getData()).replace("\r", "").split("\n");
        for (String line : lines) {
        	Matcher ssdpHeader = ssdpPattern.matcher(line);
        	Matcher httpResponseHeader = httpResponsePattern.matcher(line);
        	if(ssdpHeader.matches()) {
        		ssdpType = SSDPType.valueOf(ssdpHeader.group("ssdptype"));
        	}
        	else if(httpResponseHeader.matches()) {
        		ssdpType = SSDPType.SSDP_RESPONSE;
        		headers.put("HTTP-RESPONSE-STATUS-CODE", httpResponseHeader.group("httpresponsecode"));
        		headers.put("HTTP-RESPONSE-STATUS-DESC", httpResponseHeader.group("httpresponsedescriptor"));
        	}
            Matcher matcher = pattern.matcher(line);
            if(matcher.matches()) {
                headers.put(matcher.group(1).toUpperCase(), matcher.group(2));
            }
        }
        String location = headers.get("LOCATION");
        location = location != null ? location : "";
        String server = headers.get("SERVER");
        String st = headers.get("ST") != null ? headers.get("ST") : headers.get("NT");
        String usn = headers.get("USN");
        String nts = headers.get("NTS");
        int ssdpResponseCode = 999;
        try {
			ssdpResponseCode = Integer.valueOf(headers.get("HTTP-RESPONSE-STATUS-CODE"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		String ssdpResponseDescriptor = headers.get("HTTP-RESPONSE-STATUS-DESC");
		ssdpResponseDescriptor = ssdpResponseDescriptor == null ? "" : ssdpResponseDescriptor;
        headers.remove("LOCATION");
        headers.remove("SERVER");
        headers.remove("ST");
        headers.remove("NT");
        headers.remove("USN");
        headers.remove("NTS");
        return new SSDPMessage(
        		ssdpType,
                ssdpResult.getAddress().getHostAddress(),
                location,
                server,
                st,
                usn,
                nts,
                headers,
                source,
                ssdpResponseCode,
                ssdpResponseDescriptor);
    }
    
    public boolean isMSearch() {
    	return ssdpType == SSDPType.M_SEARCH;
    }
    
    public boolean isMSearchResponse() {
    	return ssdpType == SSDPType.SSDP_RESPONSE;
    }
    
    public boolean isNotify() {
    	return ssdpType == SSDPType.NOTIFY;
    }
    
    public boolean isAlive() {
    	return isNotify() && SSDPStatic.NTS_ALIVE.equalsIgnoreCase(this.getNTS());
    }
    
    public boolean isByeBye() {
    	return isNotify() && SSDPStatic.NTS_BYEBYE.equalsIgnoreCase(this.getNTS());
    }
    
    public SSDPType getSSDPType() {
		return ssdpType;
	}

    public String getIPAddress() {
        return ip;
    }

    public String getLocation() {
        return descriptionUrl;
    }

    public String getServer() {
        return server;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getUSN() {
        return usn;
    }
    
    public String getNTS() {
        return nts;
    }
    
    public String getHeader(String header) {
    	return this.headers.get(header);
    }
    
    public List<String> getHeaders()  {
    	return new LinkedList<>(this.headers.keySet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SSDPMessage device = (SSDPMessage) o;

        if (ip != null ? !ip.equals(device.ip) : device.ip != null) return false;
        if (descriptionUrl != null ? !descriptionUrl.equals(device.descriptionUrl) : device.descriptionUrl != null)
            return false;
        if (server != null ? !server.equals(device.server) : device.server != null) return false;
        if (serviceType != null ? !serviceType.equals(device.serviceType) : device.serviceType != null) return false;
        return usn != null ? usn.equals(device.usn) : device.usn == null;
    }
    
    public SSDPClient getSource() {
		return source;
	}
    
    public boolean isExpired() {
    	Matcher mCacheConmtrol = RegexHelper.CACHE_CONTROL.matcher(getHeader("CACHE-CONTROL") != null ? getHeader("CACHE-CONTROL").trim() : "");
  		if(mCacheConmtrol.matches()) {
  			String option = mCacheConmtrol.group("option");
  			String value = mCacheConmtrol.group("value");
  			if(option != null && option.trim().equalsIgnoreCase("max-age") && value != null) {
  				return System.currentTimeMillis() - (Integer.valueOf(value)*1200) > receiveTime;
  			}
    	}
    	return false;
    }
    
    public SSDPMessage createByeBye() {
    	return new SSDPMessage(SSDPType.NOTIFY, this.ip, this.descriptionUrl, this.server, this.serviceType, this.usn, SSDPStatic.NTS_BYEBYE, this.headers, this.source, 999, "");
    }
    
    public SSDPMessage createAlive() {
    	return new SSDPMessage(SSDPType.NOTIFY, this.ip, this.descriptionUrl, this.server, this.serviceType, this.usn, SSDPStatic.NTS_ALIVE, this.headers, this.source, 999, "");
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + (descriptionUrl != null ? descriptionUrl.hashCode() : 0);
        result = 31 * result + (server != null ? server.hashCode() : 0);
        result = 31 * result + (serviceType != null ? serviceType.hashCode() : 0);
        result = 31 * result + (usn != null ? usn.hashCode() : 0);
        return result;
    }

    public String toJson() {
        return "SSDPMessage{" +
                "ip='" + ip + '\'' +
                ", descriptionUrl='" + descriptionUrl + '\'' +
                ", server='" + server + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", usn='" + usn + '\'' +
                '}';
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(this.getSSDPType().toString()).append((this.getSSDPType() == SSDPType.SSDP_RESPONSE ? ssdpResponseCode + " " + ssdpResponseDescriptor : "")).append(SSDPStatic.NEWLINE)
    	.append("HOST: ").append(SSDPStatic.MULTICAST_ADDRESS).append(":").append(SSDPStatic.MULTICAST_PORT).append(SSDPStatic.NEWLINE)
    	.append(descriptionUrl != null && !descriptionUrl.trim().isEmpty() ?  "LOCATION: " + descriptionUrl + SSDPStatic.NEWLINE: "")
    	.append("NTS: ").append(nts).append(SSDPStatic.NEWLINE)
    	.append(server != null && !server.trim().isEmpty() ?  "SERVER: " + server + SSDPStatic.NEWLINE: "")
    	.append("USN: ").append(usn).append(SSDPStatic.NEWLINE)
    	.append("NT: ").append(serviceType).append(SSDPStatic.NEWLINE);
    	for (String header : headers.keySet()) {
			sb.append(header).append(": ").append(headers.get(header));
		}
    	return sb.toString();
    }
}