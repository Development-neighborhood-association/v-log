package com.likelion.vlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @OneToOne(mappedBy = "user")
    private Blog blog;

//    @OneToMany(mappedBy = "user")
//    private List<Comment> comments =  new ArrayList<>();


    private String email;
    private String password;
    private String nickname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateNickName(String nickname) {
        this.nickname = nickname;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

}
