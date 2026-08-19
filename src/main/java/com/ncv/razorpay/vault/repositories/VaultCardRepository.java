package com.ncv.razorpay.vault.repositories;

import com.ncv.razorpay.vault.entity.VaultCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VaultCardRepository extends JpaRepository<VaultCard, UUID> {
}
