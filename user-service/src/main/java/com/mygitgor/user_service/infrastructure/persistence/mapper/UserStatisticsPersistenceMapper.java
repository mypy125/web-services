package com.mygitgor.user_service.infrastructure.persistence.mapper;

import com.mygitgor.user_service.domain.model.UserStatistics;
import com.mygitgor.user_service.infrastructure.persistence.entity.UserStatisticsEntity;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserStatisticsPersistenceMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserStatisticsEntity toEntity(UserStatistics domain);

    @Mapping(target = "userId", source = "userId")
    UserStatistics toDomain(UserStatisticsEntity entity);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget UserStatisticsEntity entity, UserStatistics domain);

    default UUID mapUserIdToUuid(UserId userId) {
        if (userId == null || userId.getValue() == null) return null;
        return userId.getValue();
    }

    default UserId mapUuidToUserId(UUID uuid) {
        if (uuid == null) return null;
        return new UserId(uuid);
    }

    @AfterMapping
    default void updateTimestamp(@MappingTarget UserStatisticsEntity entity) {
        if (entity != null) {
            entity.setUpdatedAt(LocalDateTime.now());
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(LocalDateTime.now());
            }
        }
    }
}