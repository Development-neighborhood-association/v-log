package com.likelion.vlog.entity.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "follows")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long id;

    // 팔로우 하는사람
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User follower;


    //팔로우 대상
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private User following;


    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
