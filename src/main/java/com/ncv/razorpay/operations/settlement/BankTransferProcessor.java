package com.ncv.razorpay.operations.settlement;

import com.ncv.razorpay.common.entity.Money;
import com.ncv.razorpay.operations.settlement.dto.BankTransferResult;

import java.util.UUID;

public interface BankTransferProcessor {
    BankTransferResult initiate(UUID settlementId,UUID merchantId, Money amount,
                                String bankAccount, String ifsc);
}
