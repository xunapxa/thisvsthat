package com.project.thisvsthat.common.dto;

import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.Gender;
import com.project.thisvsthat.common.enums.SocialType;
import com.project.thisvsthat.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private LocalDate birthDate;
    private Gender gender;
    private SocialType socialType;
    private String socialId;
    private Integer reportCount;
    private UserStatus userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserDTO fromEntity(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getBirthDate(),
                user.getGender(),
                user.getSocialType(),
                user.getSocialId(),
                user.getReportCount(),
                user.getUserStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}