package tw.fengqing.spring.springbucks.customer.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.util.Arrays;

public class CustomConnectionKeepAliveStrategy implements ConnectionReuseStrategy {
    private final long DEFAULT_SECONDS = 30;

    @Override
    public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
        return Arrays.asList(response.getHeaders("Keep-Alive"))
                .stream()
                .filter(h -> StringUtils.equalsIgnoreCase(h.getName(), "timeout")
                        && StringUtils.isNumeric(h.getValue()))
                .findFirst()
                .map(h -> NumberUtils.toLong(h.getValue(), DEFAULT_SECONDS))
                .orElse(DEFAULT_SECONDS) > 0;
    }
}
