package com.likelion.vlog.service;

import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.dto.posts.PostListGetResponse;
import com.likelion.vlog.enums.SortField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PostServiceTest {

    @Autowired
    PostService postService;


    @Test
    void getPosts() {
        var req = new PostGetRequest();
        List<String> tags = List.of("java","security");
        req.setTag(tags);
        req.setSort(SortField.VIEW);

        var page = postService.getPosts(req);

        System.out.println("-------------------------------------------------------------------------");
        System.out.println(page.getPageInfo().getPage());
        System.out.println(page.getPageInfo().getSize());
        System.out.println(page.getPageInfo().getTotalElements());
        System.out.println(page.getPageInfo().getTotalPages());
        System.out.println("-------------------------------------------------------------------------\n\n");


        for (PostListGetResponse p : page.getContent()){
            System.out.println(p.getTitle());
            System.out.println(p.getViewCount());
            System.out.println("\n\n");
        }

    }
}