package com.likelion.yonsei.baton.domain.execution.service;

import com.likelion.yonsei.baton.common.exception.BusinessException;
import com.likelion.yonsei.baton.domain.baton.branch.entity.Branch;
import com.likelion.yonsei.baton.domain.baton.branch.service.BranchService;
import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import com.likelion.yonsei.baton.domain.baton.service.BatonService;
import com.likelion.yonsei.baton.domain.execution.dto.BatonResolveRequest;
import com.likelion.yonsei.baton.domain.execution.entity.Execution;
import com.likelion.yonsei.baton.domain.execution.entity.ExecutionStatus;
import com.likelion.yonsei.baton.domain.execution.exception.ExecutionErrorCode;
import com.likelion.yonsei.baton.domain.execution.repository.ExecutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ExecutionService {

	private final ExecutionRepository executionRepository;
	private final BatonService batonService;
	private final BranchService branchService;
	private final ActionExecutor actionExecutor;
	private final Clock clock;

	public ExecutionService(
			ExecutionRepository executionRepository,
			BatonService batonService,
			BranchService branchService,
			ActionExecutor actionExecutor,
			Clock clock
	) {
		this.executionRepository = executionRepository;
		this.batonService = batonService;
		this.branchService = branchService;
		this.actionExecutor = actionExecutor;
		this.clock = clock;
	}

	public List<Execution> list(Long batonId, Long userId) {
		batonService.getById(batonId, userId);
		return executionRepository.findByBatonIdOrderByCreatedAtDesc(batonId);
	}

	public Execution getById(Long id, Long userId) {
		Execution execution = executionRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ExecutionErrorCode.EXECUTION_NOT_FOUND));
		batonService.getById(execution.getBatonId(), userId);
		return execution;
	}

	@Transactional
	public ResolveResult resolve(Long batonId, Long userId, BatonResolveRequest request) {
		Baton baton = batonService.getById(batonId, userId);
		if (baton.getStatus() != BatonStatus.PENDING_REVIEW) {
			throw new BusinessException(ExecutionErrorCode.RESOLVE_NOT_ALLOWED);
		}

		Execution execution = switch (request.resolutionType()) {
			case SELECT_BRANCH -> resolveBySelectingBranch(batonId, userId, baton, request.branchId());
			case MANUAL_REPLY -> resolveByManualReply(baton, request.manualResponse());
			case CANCEL -> {
				baton.cancel();
				yield null;
			}
		};

		if (execution != null) {
			LocalDateTime now = LocalDateTime.now(clock);
			baton.markExecuted(now);
			baton.complete(now);
			if (execution.getExecutionStatus() == ExecutionStatus.FAILED) {
				baton.markError();
			}
		}

		return new ResolveResult(baton.getStatus(), execution == null ? null : execution.getId(),
				execution == null ? null : execution.getResultMessageId());
	}

	private Execution resolveBySelectingBranch(Long batonId, Long userId, Baton baton, Long branchId) {
		if (branchId == null) {
			throw new BusinessException(ExecutionErrorCode.INVALID_RESOLUTION, "branch_id가 필요합니다.");
		}
		Branch branch = branchService.getByIdForBaton(batonId, branchId, userId);
		return actionExecutor.execute(baton, branch.getId(), null, branch);
	}

	private Execution resolveByManualReply(Baton baton, String manualResponse) {
		if (manualResponse == null || manualResponse.isBlank()) {
			throw new BusinessException(ExecutionErrorCode.INVALID_RESOLUTION, "manual_response가 필요합니다.");
		}
		return actionExecutor.executeManualReply(baton, manualResponse);
	}

	public record ResolveResult(BatonStatus status, Long executionId, Long resultMessageId) {
	}
}
