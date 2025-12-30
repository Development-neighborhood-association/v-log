package com.likelion.vlog.repository;

import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostRepositoryTest {

    @Autowired
    PostRepository postRepository;

    @Test
    void searchTest(){

        var req = new PostGetRequest();

        List<String> tags = List.of("JAVA", "Spring");
        req.setTag(tags);

        var page = postRepository.search(req);

        for (Post p : page){
            System.out.println(p.getTitle());
        }

    }

}