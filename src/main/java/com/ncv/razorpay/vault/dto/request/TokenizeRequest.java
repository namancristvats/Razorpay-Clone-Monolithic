package com.ncv.razorpay.vault.dto.request;

import com.ncv.razorpay.vault.validation.ExpiryYear;
import jakarta.validation.constraints.*;
import lombok.NonNull;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

public record TokenizeRequest(
        @NotBlank(message="PAN is required")
        @LuhnCheck(message="Invalid card number")
        @Pattern(regexp="^[0-9]{13,19}$",message = "PAN Length is Invalid")
        String pan,

        @NotBlank(message="CVV is required")
        @Pattern(regexp="^[0-9]{3,4}$",message = "CVV Length is Invalid")
        String ccv,

        @NotNull(message="Expiry Month is required")
        @Min(value=1,message="Expiry must be between 1 to 12")
        @Max(value=12,message="Expiry must be between 1 to 12")
        Integer expiryMonth,

        @NotNull(message="Expiry Year is required")
        @ExpiryYear
        Integer expiryYear,

        UUID customerId,

        @Size(min = 3, message = "Card Holder Name should have at least 3 characters")
        String cardHolderName
) {
}
