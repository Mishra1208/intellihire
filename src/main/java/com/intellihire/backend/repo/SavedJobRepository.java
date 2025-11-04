package com.intellihire.backend.repo;

import com.intellihire.backend.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {
}