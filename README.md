# SSL 客戶端服務 ⚡

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 專案介紹

這是一個展示 SSL/TLS 安全通訊在 Spring Boot 微服務架構中應用的客戶端服務專案。主要功能是模擬咖啡廳的客戶端，透過 SSL/TLS 協議與服務端進行安全通訊，實現菜單查詢、訂單建立和訂單查詢等核心業務功能。

### 🎯 專案特色

- **SSL/TLS 安全通訊** - 使用自簽憑證確保 HTTPS 安全連線
- **HttpClient5 連線池** - 高效能的 HTTP 客戶端連線管理
- **非同步處理** - 使用 ApplicationRunner 實現非同步業務邏輯
- **自定義序列化** - 支援 Joda Money 的自定義序列化處理
- **連線重複使用** - 實作自定義的連線保持策略
- **RESTful API** - 標準化的 REST API 設計

> 💡 **為什麼選擇此專案？**
> - 展示 SSL/TLS 在 Spring Boot 客戶端中的完整實作
> - 提供完整的微服務客戶端開發範例
> - 適合學習安全通訊和客戶端開發
> - 程式碼結構清晰，便於理解和擴展

## 技術棧

### 核心框架
- **Spring Boot 3.2.5** - 現代化的 Java 應用程式框架
- **Java 21** - 最新的 Java 版本，支援現代化語法特性
- **HttpClient5 5.3.1** - 高效能的 HTTP 客戶端庫，支援 SSL/TLS

### 開發工具與輔助
- **Lombok** - 減少樣板程式碼，提升開發效率
- **Joda Money 2.0.2** - 專業的貨幣處理庫
- **Apache Commons Lang3 3.17.0** - 實用的工具類庫

## 專案結構

```
ssl-customer-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── tw/fengqing/spring/springbucks/customer/
│   │   │       ├── CustomerServiceApplication.java     # 主應用程式類別
│   │   │       ├── CustomerRunner.java                # 業務邏輯執行器
│   │   │       ├── model/                            # 資料模型
│   │   │       │   ├── Coffee.java                   # 咖啡商品模型
│   │   │       │   ├── CoffeeOrder.java              # 訂單模型
│   │   │       │   ├── NewOrderRequest.java          # 新訂單請求模型
│   │   │       │   └── OrderState.java               # 訂單狀態列舉
│   │   │       └── support/                          # 支援類別
│   │   │           ├── CustomConnectionKeepAliveStrategy.java  # 連線保持策略
│   │   │           ├── MoneySerializer.java          # 貨幣序列化器
│   │   │           └── MoneyDeserializer.java        # 貨幣反序列化器
│   │   └── resources/
│   │       ├── application.properties                # 應用程式設定檔
│   │       └── springbucks.p12                      # SSL 憑證檔案
│   └── test/
├── pom.xml
└── README.md
```

## 快速開始

### 前置需求
- **Java 21** - 確保已安裝 JDK 21 或更新版本
- **Maven 3.6+** - 用於專案建置和依賴管理
- **SSL 憑證** - 確保 `springbucks.p12` 憑證檔案存在於 resources 目錄
- **服務端服務** - 確保 SSL 服務端服務正在運行

### 安裝與執行

1. **克隆此倉庫：**
```bash
git clone https://github.com/SpringMicroservicesCourse/ssl-customer-service.git
```

2. **進入專案目錄：**
```bash
cd ssl-customer-service
```

3. **編譯專案：**
```bash
mvn compile
```

4. **執行應用程式：**
```bash
mvn spring-boot:run
```

## 進階說明

### 環境變數
```properties
# 服務端 URL 設定
waiter.service.url=https://localhost:8443

# SSL 憑證設定
security.key-store=classpath:springbucks.p12
security.key-pass=spring
```

### 設定檔說明
```properties
# application.properties 主要設定
waiter.service.url=https://localhost:8443    # 服務端 HTTPS 端點
security.key-store=classpath:springbucks.p12 # SSL 憑證檔案路徑
security.key-pass=spring                     # 憑證密碼
```

### 核心程式碼說明

#### SSL 客戶端配置
```java
@Bean
public HttpComponentsClientHttpRequestFactory requestFactory() {
    // 載入 SSL 憑證檔案，建立 SSL 上下文
    SSLContext sslContext = null;
    try {
        sslContext = org.apache.hc.core5.ssl.SSLContextBuilder.create()
                .loadTrustMaterial(keyStore.getURL(), keyPass.toCharArray())
                .build();
        log.info("SSL Context created successfully with p12 certificate");
    } catch(Exception e) {
        log.error("Exception occurred while creating SSLContext.", e);
        // 如果載入失敗，使用預設的 SSL 上下文
        sslContext = SSLContext.getDefault();
    }

    // 配置 HttpClient5 的連線池管理器，支援 SSL/TLS
    PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(
                    SSLConnectionSocketFactoryBuilder.create()
                            .setSslContext(sslContext)
                            .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)  // 忽略主機名驗證
                            .build()
            )
            .build();

    // 建立 HttpClient5 實例，配置連線池和重複使用策略
    CloseableHttpClient httpClient = HttpClients
            .custom()
            .evictIdleConnections(TimeValue.ofSeconds(30))  // 閒置連線清理
            .setConnectionReuseStrategy(new CustomConnectionKeepAliveStrategy())  // 自定義連線保持策略
            .setConnectionManager(connectionManager)
            .build();

    return new HttpComponentsClientHttpRequestFactory(httpClient);
}
```

#### 業務邏輯執行器
```java
@Component
@Slf4j
public class CustomerRunner implements ApplicationRunner {
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        readMenu();        // 讀取咖啡菜單
        Long id = orderCoffee();  // 建立咖啡訂單
        queryOrder(id);    // 查詢訂單狀態
    }

    private void readMenu() {
        // 使用 RestTemplate 呼叫服務端 API 取得咖啡菜單
        ParameterizedTypeReference<List<Coffee>> ptr =
                new ParameterizedTypeReference<List<Coffee>>() {};
        ResponseEntity<List<Coffee>> list = restTemplate
                .exchange(url + "/coffee/", HttpMethod.GET, null, ptr);
        list.getBody().forEach(c -> log.info("Coffee: {}", c));
    }

    private Long orderCoffee() {
        // 建立新訂單請求，包含客戶資訊和商品清單
        NewOrderRequest orderRequest = NewOrderRequest.builder()
                .customer("Li Lei")
                .items(Arrays.asList("capuccino"))
                .build();
        
        // 發送 POST 請求到服務端建立訂單
        RequestEntity<NewOrderRequest> request = RequestEntity
                .post(UriComponentsBuilder.fromUriString(url + "/order/").build().toUri())
                .body(orderRequest);
        ResponseEntity<CoffeeOrder> response = restTemplate.exchange(request, CoffeeOrder.class);
        
        log.info("Order Request Status Code: {}", response.getStatusCode());
        Long id = response.getBody().getId();
        log.info("Order ID: {}", id);
        return id;
    }
}
```

#### 自定義連線保持策略
```java
public class CustomConnectionKeepAliveStrategy implements ConnectionReuseStrategy {
    private final long DEFAULT_SECONDS = 30;

    @Override
    public boolean keepAlive(HttpRequest request, HttpResponse response, HttpContext context) {
        // 解析 Keep-Alive 標頭，決定是否重複使用連線
        return Arrays.asList(response.getHeaders("Keep-Alive"))
                .stream()
                .filter(h -> StringUtils.equalsIgnoreCase(h.getName(), "timeout")
                        && StringUtils.isNumeric(h.getValue()))
                .findFirst()
                .map(h -> NumberUtils.toLong(h.getValue(), DEFAULT_SECONDS))
                .orElse(DEFAULT_SECONDS) > 0;
    }
}
```

## API 端點說明

### 客戶端功能
- **菜單查詢** - 從服務端取得咖啡商品清單
- **訂單建立** - 向服務端發送新訂單請求
- **訂單查詢** - 根據訂單 ID 查詢訂單狀態

### 服務端 API 呼叫
- `GET /coffee/` - 取得所有咖啡商品
- `POST /order/` - 建立新訂單
- `GET /order/{id}` - 根據 ID 取得訂單

## 參考資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [HttpClient5 官方文件](https://hc.apache.org/httpcomponents-client-5.1.x/)
- [SSL/TLS 安全通訊指南](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.ssl)
- [Joda Money 文件](https://www.joda.org/joda-money/)

## 注意事項與最佳實踐

### ⚠️ 重要提醒

| 項目 | 說明 | 建議做法 |
|------|------|----------|
| 安全性 | SSL 憑證管理 | 使用環境變數管理憑證密碼 |
| 效能 | HTTP 連線池 | 適當配置連線池大小和超時時間 |
| 錯誤處理 | 網路異常處理 | 實作重試機制和錯誤回退 |

### 🔒 最佳實踐指南

- **憑證管理** - 將敏感資訊如憑證密碼存放在環境變數中
- **連線池配置** - 根據網路環境調整適當的連線池大小
- **錯誤處理** - 實作完整的異常處理機制
- **日誌記錄** - 使用結構化日誌記錄關鍵操作
- **單元測試** - 為核心業務邏輯撰寫完整的測試案例

### 🔧 開發建議

- **程式碼註解** - 在重要的程式碼區塊添加清楚註解，方便團隊成員理解與維護
- **命名規範** - 使用有意義的變數和方法名稱
- **模組化設計** - 將不同功能分離到不同的類別中
- **配置外部化** - 將設定值放在配置檔案中，便於環境切換
- **效能優化** - 使用連線池和連線重複使用策略提升效能

## 授權說明

本專案採用 MIT 授權條款，詳見 LICENSE 檔案。

## 關於我們

我們主要專注在敏捷專案管理、物聯網（IoT）應用開發和領域驅動設計（DDD）。喜歡把先進技術和實務經驗結合，打造好用又靈活的軟體解決方案。

## 聯繫我們

- **FB 粉絲頁**：[風清雲談 | Facebook](https://www.facebook.com/profile.php?id=61576838896062)
- **LinkedIn**：[linkedin.com/in/chu-kuo-lung](https://www.linkedin.com/in/chu-kuo-lung)
- **YouTube 頻道**：[雲談風清 - YouTube](https://www.youtube.com/channel/UCXDqLTdCMiCJ1j8xGRfwEig)
- **風清雲談 部落格**：[風清雲談](https://blog.fengqing.tw/)
- **電子郵件**：[fengqing.tw@gmail.com](mailto:fengqing.tw@gmail.com)

---

**📅 最後更新：2025年8月7日**  
**👨‍💻 維護者：風清雲談團隊** 