package com.likelion.yonsei.baton.domain.message.repository;

import com.likelion.yonsei.baton.domain.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

	Optional<Message> findByConversationIdAndExternalMessageId(Long conversationId, String externalMessageId);

	boolean existsByExternalEventId(String externalEventId);

	List<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);

	List<Message> findByConversationIdAndSentAtLessThanOrderBySentAtDesc(Long conversationId, LocalDateTime before, Pageable pageable);

	List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

	Optional<Message> findTopByConversationIdOrderBySentAtDesc(Long conversationId);
}
