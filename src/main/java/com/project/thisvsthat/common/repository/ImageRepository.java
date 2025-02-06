package com.project.thisvsthat.common.repository;

import com.project.thisvsthat.common.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}