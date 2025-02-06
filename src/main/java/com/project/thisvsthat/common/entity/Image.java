package com.project.thisvsthat.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column(length = 255)
    private String imageUrl;  // 이미지 URL 저장
}