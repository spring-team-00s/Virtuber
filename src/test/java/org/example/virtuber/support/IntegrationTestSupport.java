package org.example.virtuber.support;

import org.example.virtuber.account.entity.Account;
import org.example.virtuber.account.repository.AccountRepository;
import org.example.virtuber.auth.repository.RefreshTokenRepository;
import org.example.virtuber.stock.entity.Stock;
import org.example.virtuber.stock.repository.StockPriceHistoryRepository;
import org.example.virtuber.stock.repository.StockRepository;
import org.example.virtuber.trade.dto.TradeRequest;
import org.example.virtuber.trade.repository.HoldingRepository;
import org.example.virtuber.trade.repository.TradeRepository;
import org.example.virtuber.user.entity.User;
import org.example.virtuber.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MySQLContainer;

import java.lang.reflect.Constructor;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    protected static final String RAW_PASSWORD = "password1234!";

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("virtuber_test")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    }

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected StockRepository stockRepository;

    @Autowired
    protected StockPriceHistoryRepository stockPriceHistoryRepository;

    @Autowired
    protected HoldingRepository holdingRepository;

    @Autowired
    protected TradeRepository tradeRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        tradeRepository.deleteAll();
        holdingRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        accountRepository.deleteAll();
        stockPriceHistoryRepository.deleteAll();
        stockRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User saveUser(String userId) {
        return userRepository.save(new User(userId, passwordEncoder.encode(RAW_PASSWORD)));
    }

    protected Account saveAccount(User user, Long seedMoney) {
        return accountRepository.save(new Account(user, seedMoney));
    }

    protected Stock saveStock(String stockCode, String stockName, Long currentPrice) {
        Stock stock = newStock();
        ReflectionTestUtils.setField(stock, "stockCode", stockCode);
        ReflectionTestUtils.setField(stock, "stockName", stockName);
        ReflectionTestUtils.setField(stock, "currentPrice", currentPrice);
        ReflectionTestUtils.setField(stock, "upPrice", currentPrice * 13 / 10);
        ReflectionTestUtils.setField(stock, "lowPrice", currentPrice * 7 / 10);
        ReflectionTestUtils.setField(stock, "companyInfo", stockName + " 회사 정보");
        ReflectionTestUtils.setField(stock, "financialInfo", stockName + " 재무 정보");
        return stockRepository.save(stock);
    }

    private Stock newStock() {
        try {
            Constructor<Stock> constructor = Stock.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Stock 테스트 픽스처를 생성할 수 없습니다.", e);
        }
    }

    protected TradeRequest tradeRequest(Long stockId, Long quantity) {
        TradeRequest request = new TradeRequest();
        ReflectionTestUtils.setField(request, "stockId", stockId);
        ReflectionTestUtils.setField(request, "quantity", quantity);
        return request;
    }
}
