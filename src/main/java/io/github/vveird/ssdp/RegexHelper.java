package io.github.vveird.ssdp;

import java.util.regex.Pattern;

public class RegexHelper {
	public static final Pattern IP_V_4_PATTERN = Pattern.compile("(?<hostname>.*)/(?<ip>\\d+\\.\\d+\\.\\d+\\.\\d+)(?<add>.*)");

	public static final Pattern HTTP_PATTERN = Pattern.compile("(?<http>https?://)(?<domain>[^:^/]*)(:(?<port>\\d*))?(?<uri>.*)?");
	
	public static final Pattern CACHE_CONTROL = Pattern.compile(" *(?<option>.*) *?\\= *?(?<value>\\d+).*");

}
