package com.likelion.vlog.service;

import com.likelion.vlog.entity.Tag;
import com.likelion.vlog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TagServiceTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        tagRepository.save(Tag.of("Java"));
    }

    @Test
    @DisplayName("태그 조회 성공")
    void getTag_Success() {
        var result = tagService.getTag("Java");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java");
    }

    @Test
    @DisplayName("존재하지 않는 태그 조회시 null 반환")
    void getTag_NotFound() {
        var result = tagService.getTag("NonExistentTag");

        assertThat(result).isNull();
    }
}