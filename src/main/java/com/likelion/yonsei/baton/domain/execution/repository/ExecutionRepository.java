package com.likelion.yonsei.baton.domain.execution.repository;

import com.likelion.yonsei.baton.domain.execution.entity.Execution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutionRepository extends JpaRepository<Execution, Long> {

	List<Execution> findByBatonIdOrderByCreatedAtDesc(Long batonId);
}
