package com.project.thisvsthat.common.dto;

import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.Gender;
import com.project.thisvsthat.common.enums.SocialType;
import com.project.thisvsthat.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

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
                user.getUserStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    //연령대 계산 메서드
    public String getAgeGroup() {
        if(birthDate == null) {
            return "알 수 없음";
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        int ageGroup = (age / 10) * 10;
        return ageGroup + "대";
    }
}