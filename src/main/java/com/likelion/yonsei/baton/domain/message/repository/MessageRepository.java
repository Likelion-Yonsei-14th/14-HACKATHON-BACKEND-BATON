package com.likelion.yonsei.baton.domain.message.repository;

import com.likelion.yonsei.baton.domain.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

	Optional<Message> findByConversationIdAndExternalMessageId(Long conversationId, String externalMessageId);

	// Scopes by the message's conversation -> platform_connection -> owning user, since Message has
	// no JPA relation mappings (plain FK id columns) to walk with a derived query.
	@Query("""
			select m from Message m
			join Conversation c on c.id = m.conversationId
			join PlatformConnection pc on pc.id = c.platformConnectionId
			where m.id = :id and pc.userId = :userId
			""")
	Optional<Message> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	boolean existsByExternalEventId(String externalEventId);

	List<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);

	List<Message> findByConversationIdAndSentAtLessThanOrderBySentAtDesc(Long conversationId, LocalDateTime before, Pageable pageable);

	List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

	Optional<Message> findTopByConversationIdOrderBySentAtDesc(Long conversationId);
}
