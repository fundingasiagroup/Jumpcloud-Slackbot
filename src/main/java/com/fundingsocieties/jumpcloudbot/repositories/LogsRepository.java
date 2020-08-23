package com.fundingsocieties.jumpcloudbot.repositories;

import com.fundingsocieties.jumpcloudbot.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface LogsRepository extends JpaRepository<Log, UUID> {
}
