package com.likelion.vlog.repository.querydsl.expresion;

import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.QLike;
import com.likelion.vlog.entity.QPost;
import com.likelion.vlog.entity.QTagMap;
import com.likelion.vlog.enums.SearchFiled;


import com.likelion.vlog.enums.SortField;
import com.likelion.vlog.enums.SortOrder;
import com.likelion.vlog.enums.TagMode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.annotations.QueryDelegate;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;

import java.util.List;
import java.util.Objects;


public class PostExpression {
    @QueryDelegate(Post.class) //tags 목록에 있는 모든 테그를 포함하는 게시물만 통과, 더 많아도 됨
    public static Predicate hasAllTags(QPost post, List<String> tags){
        List<String> sanitized = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (sanitized.isEmpty()) return null;

        QTagMap tagMap = QTagMap.tagMap;

        BooleanBuilder builder = new BooleanBuilder();
        for (String t : sanitized) {
            builder.and(tagMap.existsOnPostWithTitle(post, t));
        }
        return builder;
    }

    @QueryDelegate(Post.class) //게시물의 태그 중 하나라도 tags 목록에 포함되면 통과
    public static Predicate oneOfTags(QPost post, List<String> tags){
        return post.tagMapList.any().tag.title.in(tags);
    }


    @QueryDelegate(Post.class)
    public static Predicate search (QPost post, PostGetRequest request){

        Long blogId = request.getBlogId();
        String keyword = request.getKeyword();
        List<String> tags = request.getTag();
        SearchFiled search = request.getSearch();
        TagMode tagMode = request.getTagMode();

        BooleanBuilder builder = new BooleanBuilder();

        if (blogId != null) {
            builder.and(post.blog.id.eq(blogId));
        }

        if (keyword != null) {
            switch (search) {
                case BLOG ->  builder.and(post.blog.title.containsIgnoreCase(keyword));
                case NICKNAME -> builder.and(post.blog.user.nickname.containsIgnoreCase(keyword));
                case TITLE -> builder.and(post.title.containsIgnoreCase(keyword));
            }
        }

        if (tags != null) {
            switch (tagMode) {
                case AND -> builder.and(hasAllTags(post, tags));
                case OR -> builder.and(oneOfTags(post, tags));
            }
        }

        return null;
    }

    @QueryDelegate(Post.class)
    public static NumberExpression<Long> likeCount(QPost post) {
        QLike like = QLike.like;

        var subquery = JPAExpressions
                .select(like.count())
                .from(like)
                .where(like.post.eq(post));

        return Expressions.numberTemplate(Long.class, "({0})", subquery);
    }



    @QueryDelegate(Post.class)
    public static OrderSpecifier<?> sort(QPost post, PostGetRequest request){
        SortField sort = request.getSort();
        boolean asc = request.getOrder() == SortOrder.ASC;

        return switch (sort) {
            case LIKE -> asc ? post.likeCount().asc() : post.likeCount().desc();
            case VIEW -> asc ? post.viewCount.asc() : post.viewCount.desc();
            case CREATED_AT -> asc ? post.createdAt.asc() : post.createdAt.desc();
            case UPDATED_AT -> asc ? post.updatedAt.asc() : post.updatedAt.desc();
        };
    }
}
