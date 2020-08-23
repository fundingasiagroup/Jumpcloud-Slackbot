package com.fundingsocieties.jumpcloudbot.repositories;

import com.fundingsocieties.jumpcloudbot.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface AdminsRepository extends JpaRepository<Admin, UUID> {

    @Query("Select a from Admin a where a.slackUserId = :slackUserId and a.deletedAt IS NULL")
    Admin findActiveAdminBySlackUserId(@Param("slackUserId") String slackUserId);

    Admin findBySlackUserId(String slackUserId);
}
