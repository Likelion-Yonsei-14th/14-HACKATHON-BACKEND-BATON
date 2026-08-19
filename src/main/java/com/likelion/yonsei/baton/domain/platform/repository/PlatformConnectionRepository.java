package com.likelion.yonsei.baton.domain.platform.repository;

import com.likelion.yonsei.baton.domain.platform.entity.PlatformConnection;
import com.likelion.yonsei.baton.domain.platform.entity.PlatformType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformConnectionRepository extends JpaRepository<PlatformConnection, Long> {

	List<PlatformConnection> findByUserId(Long userId);

	Optional<PlatformConnection> findByIdAndUserId(Long id, Long userId);

	Optional<PlatformConnection> findByUserIdAndPlatformTypeAndWorkspaceId(
			Long userId, PlatformType platformType, String workspaceId);
}
