package com.likelion.yonsei.baton.domain.classification.repository;

import com.likelion.yonsei.baton.domain.classification.entity.Classification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassificationRepository extends JpaRepository<Classification, Long> {

	List<Classification> findByBatonIdOrderByCreatedAtDesc(Long batonId);
}
