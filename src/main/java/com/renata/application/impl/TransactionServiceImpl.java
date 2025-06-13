package com.renata.application.impl;

import com.renata.application.contract.TransactionService;
import com.renata.application.dto.TransactionStoreDto;
import com.renata.application.dto.TransactionUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.TransactionRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
final class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final PersistenceContext persistenceContext;
    private final Validator validator;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            PersistenceContext persistenceContext,
            Validator validator) {
        this.transactionRepository = transactionRepository;
        this.persistenceContext = persistenceContext;
        this.validator = validator;
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

        persistenceContext.registerNew(transaction);
        persistenceContext.commit();
        return transaction;
    }

    @Override
    public Transaction update(UUID id, TransactionUpdateDto transactionUpdateDto) {
        Set<jakarta.validation.ConstraintViolation<TransactionUpdateDto>> violations =
                validator.validate(transactionUpdateDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("transaction update", violations);
        }

        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isEmpty()) {
            throw new DatabaseAccessException("Transaction not found with id: " + id);
        }
        Transaction transaction = transactionOpt.get();

        transaction.setType(transactionUpdateDto.type());
        transaction.setUserId(transactionUpdateDto.userId());
        transaction.setItemId(transactionUpdateDto.itemId());
        transaction.setTimestamp(transactionUpdateDto.timestamp());

        persistenceContext.registerUpdated(id, transaction);
        persistenceContext.commit();
        return transaction;
    }

    @Override
    public void delete(UUID id) {
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
    public List<Transaction> findByUserAndType(UUID userId, TransactionType type) {
        return transactionRepository.findByUserAndType(userId, type);
    }
}
