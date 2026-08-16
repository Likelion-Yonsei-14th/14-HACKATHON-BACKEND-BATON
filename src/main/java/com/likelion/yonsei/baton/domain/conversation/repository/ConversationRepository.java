package com.likelion.yonsei.baton.domain.conversation.repository;

import com.likelion.yonsei.baton.domain.conversation.entity.Conversation;
import com.likelion.yonsei.baton.domain.conversation.entity.ConversationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	Optional<Conversation> findByPlatformConnectionIdAndExternalConversationIdAndExternalThreadId(
			Long platformConnectionId, String externalConversationId, String externalThreadId);

	Optional<Conversation> findFirstByExternalConversationId(String externalConversationId);

	@Query("""
			select c from Conversation c
			join PlatformConnection pc on pc.id = c.platformConnectionId
			where pc.userId = :userId
			and (:platformConnectionId is null or c.platformConnectionId = :platformConnectionId)
			and (:conversationType is null or c.conversationType = :conversationType)
			and (:cursor is null or c.id < :cursor)
			order by c.id desc
			""")
	List<Conversation> search(
			@Param("userId") Long userId,
			@Param("platformConnectionId") Long platformConnectionId,
			@Param("conversationType") ConversationType conversationType,
			@Param("cursor") Long cursor,
			Pageable pageable
	);

	@Query("""
			select c from Conversation c
			join PlatformConnection pc on pc.id = c.platformConnectionId
			where c.id = :id and pc.userId = :userId
			""")
	Optional<Conversation> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
