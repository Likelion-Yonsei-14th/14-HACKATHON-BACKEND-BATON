package com.likelion.yonsei.baton.domain.baton.branch.service;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.branch.exception.BranchErrorCode;
import com.likelion.yonsei.baton.domain.baton.branch.repository.BranchRepository;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import com.likelion.yonsei.baton.domain.baton.service.BatonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BranchService {

	private final BranchRepository branchRepository;
	private final BatonService batonService;

	public BranchService(BranchRepository branchRepository, BatonService batonService) {
		this.branchRepository = branchRepository;
		this.batonService = batonService;
	}

	@Transactional
	public Branch create(Long batonId, Long userId, Branch draft) {
		Baton baton = batonService.getById(batonId, userId);
		requireDraft(baton);
		return branchRepository.save(draft);
	}

	public List<Branch> list(Long batonId, Long userId) {
		batonService.getById(batonId, userId);
		return branchRepository.findByBatonIdOrderBySortOrderAsc(batonId);
	}

	public Branch getById(Long id) {
		return branchRepository.findById(id)
				.orElseThrow(() -> new BusinessException(BranchErrorCode.BRANCH_NOT_FOUND));
	}

	public Branch getByIdForUser(Long id, Long userId) {
		Branch branch = getById(id);
		batonService.getById(branch.getBatonId(), userId);
		return branch;
	}

	public Branch getByIdForBaton(Long batonId, Long id, Long userId) {
		batonService.getById(batonId, userId);
		return branchRepository.findByIdAndBatonId(id, batonId)
				.orElseThrow(() -> new BusinessException(BranchErrorCode.BRANCH_NOT_FOUND));
	}

	@Transactional
	public Branch update(
			Long batonId,
			Long id,
			Long userId,
			String name,
			String description,
			String conditionText,
			String conditionRuleJson,
			String decisionText,
			String responseText,
			com.likelion.yonsei.baton.domain.baton.branch.entity.ActionType actionType,
			String actionConfigJson,
			com.likelion.yonsei.baton.domain.baton.branch.entity.ExecutionMode executionMode,
			Integer sortOrder
	) {
		Branch branch = getByIdForBaton(batonId, id, userId);
		branch.update(name, description, conditionText, conditionRuleJson, decisionText, responseText,
				actionType, actionConfigJson, executionMode, sortOrder);
		return branch;
	}

	@Transactional
	public void delete(Long batonId, Long id, Long userId) {
		Baton baton = batonService.getById(batonId, userId);
		requireDraft(baton);
		Branch branch = getByIdForBaton(batonId, id, userId);
		branchRepository.delete(branch);
	}

	private void requireDraft(Baton baton) {
		if (baton.getStatus() != BatonStatus.DRAFT) {
			throw new BusinessException(BranchErrorCode.BRANCH_MUTATION_NOT_ALLOWED);
		}
	}
}
