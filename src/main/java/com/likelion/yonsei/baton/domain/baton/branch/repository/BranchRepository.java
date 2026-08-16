package com.likelion.yonsei.baton.domain.baton.branch.repository;

import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {

	List<Branch> findByBatonIdOrderBySortOrderAsc(Long batonId);

	Optional<Branch> findByIdAndBatonId(Long id, Long batonId);

	void deleteByBatonId(Long batonId);
}
