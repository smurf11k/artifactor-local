package com.renata.application.impl;

import com.renata.application.contract.AuthService;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.MarketInfoService;
import com.renata.application.contract.TransactionService;
import com.renata.application.contract.UserService;
import com.renata.application.dto.TransactionStoreDto;
import com.renata.application.dto.TransactionUpdateDto;
import com.renata.application.exception.AuthException;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.entities.Transaction;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
import com.renata.domain.enums.MarketEventType;
import com.renata.domain.enums.TransactionType;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.TransactionRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import jakarta.validation.Validator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/** Реалізація сервісу для роботи з транзакціями антикваріату. */
@Service
final class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final MarketInfoService marketInfoService;
    private final UserService userService;
    private final AuthService authService;
    private final ItemService itemService;
    private final PersistenceContext persistenceContext;
    private final Validator validator;
    private final InfrastructureConfig infrastructureConfig;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            MarketInfoService marketInfoService,
            UserService userService,
            AuthService authService,
            ItemService itemService,
            PersistenceContext persistenceContext,
            Validator validator,
            InfrastructureConfig infrastructureConfig) {
        this.transactionRepository = transactionRepository;
        this.marketInfoService = marketInfoService;
        this.userService = userService;
        this.authService = authService;
        this.itemService = itemService;
        this.persistenceContext = persistenceContext;
        this.validator = validator;
        this.infrastructureConfig = infrastructureConfig;
    }

    @Override
    public Transaction create(TransactionStoreDto transactionStoreDto) {
        Transaction transaction =
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .userId(transactionStoreDto.userId())
                        .itemId(transactionStoreDto.itemId())
                        .type(transactionStoreDto.type())
                        .timestamp(
                                transactionStoreDto.timestamp() != null
                                        ? transactionStoreDto.timestamp()
                                        : LocalDateTime.now())
                        .build();

        UUID itemId = transactionStoreDto.itemId();
        Optional<MarketInfo> latestMarketInfoOpt = marketInfoService.findLatestMarketInfo(itemId);

        MarketInfo marketInfo;
        if (latestMarketInfoOpt.isPresent()) {
            MarketInfo latestMarketInfo = latestMarketInfoOpt.get();
            MarketEventType marketEventType =
                    transactionStoreDto.type() == TransactionType.PURCHASE
                            ? MarketEventType.PURCHASED
                            : MarketEventType.RELISTED;
            marketInfo =
                    MarketInfo.builder()
                            .id(UUID.randomUUID())
                            .itemId(itemId)
                            .price(latestMarketInfo.getPrice())
                            .timestamp(LocalDateTime.now())
                            .type(marketEventType)
                            .build();
        } else {
            throw new IllegalStateException(
                    "Не знайдено ринкової інформації для предмету: " + itemId);
        }

        persistenceContext.registerNew(transaction);
        persistenceContext.registerNew(marketInfo);
        persistenceContext.commit();

        return transaction;
    }

    @Override
    public Transaction update(TransactionUpdateDto transactionUpdateDto) {
        Set<jakarta.validation.ConstraintViolation<TransactionUpdateDto>> violations =
                validator.validate(transactionUpdateDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("transaction update", violations);
        }

        UUID dtoId = transactionUpdateDto.id();
        User user = authService.getCurrentUser();

        if (!authService.hasPermission(Role.EntityName.TRANSACTION, "update")
                || user.getRole() != Role.ADMIN) {
            throw new AuthException("У вас немає права на редагування цієї транзакції.");
        }

        Optional<Transaction> transactionOpt = transactionRepository.findById(dtoId);
        if (transactionOpt.isEmpty()) {
            throw new DatabaseAccessException("Транзакцію не знайдено з таким id: " + dtoId);
        }
        Transaction transaction = transactionOpt.get();

        transaction.setType(transactionUpdateDto.type());
        transaction.setItemId(transactionUpdateDto.itemId());
        transaction.setTimestamp(transactionUpdateDto.timestamp());

        persistenceContext.registerUpdated(dtoId, transaction);
        persistenceContext.commit();
        return transaction;
    }

    @Override
    public void delete(UUID id) {
        User user = authService.getCurrentUser();

        if (!authService.hasPermission(Role.EntityName.TRANSACTION, "delete")
                || user.getRole() != Role.ADMIN) {
            throw new AuthException("У вас немає права на видалення цієї транзакції.");
        }

        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            persistenceContext.registerDeleted(transaction);
            persistenceContext.commit();
        }
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id);
    }

    @Override
    public List<Transaction> findAll(int offset, int limit) {
        return transactionRepository.findAll(offset, limit);
    }

    @Override
    public List<Transaction> findByUserId(UUID userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public List<Transaction> findByItemId(UUID itemId) {
        return transactionRepository.findByItemId(itemId);
    }

    @Override
    public List<Transaction> findByType(TransactionType type) {
        return transactionRepository.findByType(type);
    }

    @Override
    public List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByDateRange(from, to);
    }

    @Override
    public void generateReport(Predicate<Transaction> filter) {
        String REPORTS_DIRECTORY = infrastructureConfig.getReportsDirectory();
        File reportsDir = new File(REPORTS_DIRECTORY);
        if (!reportsDir.exists() && !reportsDir.mkdirs()) {
            throw new RuntimeException("Не вдалося створити директорію: " + REPORTS_DIRECTORY);
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "ID", "UserID", "Username", "ItemID", "ItemName", "TransactionType", "Price"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        List<Transaction> transactions =
                transactionRepository.findAll(0, Integer.MAX_VALUE).stream()
                        .filter(filter)
                        .toList();

        String currentUsername;
        try {
            currentUsername = authService.getCurrentUser().getUsername();
        } catch (AuthException e) {
            currentUsername = "Unknown";
        }

        for (Transaction transaction : transactions) {
            Row row = sheet.createRow(rowNum++);
            UUID userId = transaction.getUserId();
            UUID itemId = transaction.getItemId();

            User user = userService.findById(userId);
            String username = user != null ? user.getUsername() : "Unknown";
            String itemName = itemService.findById(itemId).map(Item::getName).orElse("Unknown");
            Optional<MarketInfo> marketInfo = marketInfoService.findLatestMarketInfo(itemId);
            Double price = marketInfo.map(MarketInfo::getPrice).orElse(0.0);
            String transactionType =
                    transaction.getType() != null ? transaction.getType().toString() : "Unknown";

            row.createCell(0).setCellValue(transaction.getId().toString());
            row.createCell(1).setCellValue(userId.toString());
            row.createCell(2).setCellValue(username);
            row.createCell(3).setCellValue(itemId.toString());
            row.createCell(4).setCellValue(itemName);
            row.createCell(5).setCellValue(transactionType);
            row.createCell(6).setCellValue(price);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        String timestamp =
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("transaction-%s-%s.xlsx", currentUsername, timestamp);
        Path outputPath = Path.of(REPORTS_DIRECTORY, fileName);

        try (FileOutputStream outputStream = new FileOutputStream(outputPath.toFile())) {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(
                    "Помилка при збереженні звіту транзакцій: " + e.getMessage());
        }
    }
}
