package com.likelion.vlog.repository.querydsl.custom;

import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.QPost;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Post> search(PostGetRequest request) {
        QPost post = QPost.post;

        int page = request.getPage();
        int size = request.getSize();

        var query = jpaQueryFactory
                .selectFrom(post)
                .where(post.search(request));

        OrderSpecifier<?> order = post.sort(request);
        query.orderBy(order, post.id.desc());

        List<Post> content = query
                .offset((long) page * size)
                .limit(size)
                .fetch();

        // total count (정렬/페이징 없이)
        Long total = jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(post.search(request))
                .fetchOne();

        long totalElements = total == null ? 0L : total;

        return new PageImpl<>(content, PageRequest.of(page, size), totalElements);
    }

}
