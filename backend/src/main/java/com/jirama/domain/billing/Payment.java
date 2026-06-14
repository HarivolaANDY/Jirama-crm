package com.jirama.domain.billing;

import com.jirama.domain.billing.enums.PaymentMethod;
import com.jirama.domain.billing.enums.PaymentStatus;
import com.jirama.domain.shared.BaseEntity;
import com.jirama.domain.shared.Money;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a payment transaction against an invoice.
 */
public class Payment extends BaseEntity {

    private final String paymentNumber;
    private final UUID invoiceId;
    private final UUID subscriberId;
    private final Money amount;
    private final PaymentMethod paymentMethod;
    private String mobileMoneyProvider;
    private String bankName;
    private String transactionReference;
    private PaymentStatus status;
    private String receiptNumber;
    private UUID processedBy;
    private String notes;

    public Payment(UUID id, Instant createdAt, Instant updatedAt, long version,
                   String paymentNumber, UUID invoiceId, UUID subscriberId,
                   Money amount, PaymentMethod paymentMethod,
                   String mobileMoneyProvider, String bankName,
                   String transactionReference, PaymentStatus status,
                   String receiptNumber, UUID processedBy, String notes) {
        super(id, createdAt, updatedAt, version);
        this.paymentNumber = paymentNumber;
        this.invoiceId = invoiceId;
        this.subscriberId = subscriberId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.mobileMoneyProvider = mobileMoneyProvider;
        this.bankName = bankName;
        this.transactionReference = transactionReference;
        this.status = status;
        this.receiptNumber = receiptNumber;
        this.processedBy = processedBy;
        this.notes = notes;
    }

    public static Payment create(String paymentNumber, UUID invoiceId, UUID subscriberId,
                                  Money amount, PaymentMethod paymentMethod) {
        return new Payment(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                paymentNumber, invoiceId, subscriberId, amount, paymentMethod,
                null, null, null, PaymentStatus.PENDING,
                null, null, null
        );
    }

    public void markCompleted(String transactionReference, String receiptNumber) {
        this.transactionReference = transactionReference;
        this.receiptNumber = receiptNumber;
        this.status = PaymentStatus.COMPLETED;
        markUpdated();
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.notes = reason;
        markUpdated();
    }

    public void refund(String reason) {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
        this.notes = reason;
        markUpdated();
    }

    // Getters
    public String getPaymentNumber() { return paymentNumber; }
    public UUID getInvoiceId() { return invoiceId; }
    public UUID getSubscriberId() { return subscriberId; }
    public Money getAmount() { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getMobileMoneyProvider() { return mobileMoneyProvider; }
    public String getBankName() { return bankName; }
    public String getTransactionReference() { return transactionReference; }
    public PaymentStatus getStatus() { return status; }
    public String getReceiptNumber() { return receiptNumber; }
    public UUID getProcessedBy() { return processedBy; }
    public String getNotes() { return notes; }
}
