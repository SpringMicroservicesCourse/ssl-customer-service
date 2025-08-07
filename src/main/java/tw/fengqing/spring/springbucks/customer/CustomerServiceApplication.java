package tw.fengqing.spring.springbucks.customer;

import tw.fengqing.spring.springbucks.customer.support.CustomConnectionKeepAliveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.time.Duration;

@SpringBootApplication
@Slf4j
public class CustomerServiceApplication {
	@Value("${security.key-store}")
	private Resource keyStore;
	@Value("${security.key-pass}")
	private String keyPass;

	public static void main(String[] args) {
		new SpringApplicationBuilder()
				.sources(CustomerServiceApplication.class)
				.bannerMode(Banner.Mode.OFF)
				.web(WebApplicationType.NONE)
				.run(args);
	}

	@Bean
	public HttpComponentsClientHttpRequestFactory requestFactory() {
		// 參考 ssldd 的 SSL 配置，載入 p12 憑證
		SSLContext sslContext = null;
		try {
			sslContext = org.apache.hc.core5.ssl.SSLContextBuilder.create()
					// 會校驗證書
					.loadTrustMaterial(keyStore.getURL(), keyPass.toCharArray())
					// 放過所有證書校驗
					// .loadTrustMaterial(null, (certificate, authType) -> true)
					.build();
			log.info("SSL Context created successfully with p12 certificate");
		} catch(Exception e) {
			log.error("Exception occurred while creating SSLContext.", e);
			// 如果載入失敗，使用預設的 SSL 上下文
			try {
				sslContext = SSLContext.getDefault();
			} catch (Exception ex) {
				log.error("Failed to get default SSL context", ex);
				sslContext = null;
			}
		}

		// 使用 HttpClient5 的正確 SSL 配置方法，並忽略主機名驗證
		PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
				.setSSLSocketFactory(
						SSLConnectionSocketFactoryBuilder.create()
								.setSslContext(sslContext)
								.setHostnameVerifier(NoopHostnameVerifier.INSTANCE)  // 忽略主機名驗證
								.build()
				)
				.build();

		CloseableHttpClient httpClient = HttpClients
				.custom()
				.evictIdleConnections(TimeValue.ofSeconds(30))
				.setConnectionReuseStrategy(new CustomConnectionKeepAliveStrategy())
				.setConnectionManager(connectionManager)
				.build();

		HttpComponentsClientHttpRequestFactory requestFactory =
				new HttpComponentsClientHttpRequestFactory(httpClient);

		return requestFactory;
	}

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate(requestFactory());
		return restTemplate;
	}
}
