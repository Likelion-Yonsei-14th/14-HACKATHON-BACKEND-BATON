package com.likelion.yonsei.baton.domain.baton.repository;

import com.likelion.yonsei.baton.domain.baton.entity.Baton;
import com.likelion.yonsei.baton.domain.baton.entity.BatonStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BatonRepository extends JpaRepository<Baton, Long> {

	Optional<Baton> findByIdAndUserId(Long id, Long userId);

	@Query("""
			select b from Baton b
			where b.userId = :userId
			and (:status is null or b.status = :status)
			and (:conversationId is null or b.conversationId = :conversationId)
			and (:cursor is null or b.id < :cursor)
			order by b.id desc
			""")
	List<Baton> search(
			@Param("userId") Long userId,
			@Param("status") BatonStatus status,
			@Param("conversationId") Long conversationId,
			@Param("cursor") Long cursor,
			Pageable pageable
	);

	Optional<Baton> findByConversationIdAndStatusIn(Long conversationId, List<BatonStatus> statuses);

	boolean existsByTriggerMessageId(Long triggerMessageId);

	long countByUserIdAndStatus(Long userId, BatonStatus status);

	long countByUserIdAndCompletedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

	long countByUserIdAndStatusInAndCompletedAtBetween(Long userId, List<BatonStatus> statuses, LocalDateTime from, LocalDateTime to);

	List<Baton> findByUserIdAndStatusAndCompletedAtBetween(Long userId, BatonStatus status, LocalDateTime from, LocalDateTime to);
}
