package com.likelion.vlog.repository.querydsl.expresion;

import com.likelion.vlog.entity.QPost;
import com.likelion.vlog.entity.QTag;
import com.likelion.vlog.entity.QTagMap;
import com.likelion.vlog.entity.TagMap;
import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;

public class TagMapExpression {
    @QueryDelegate(TagMap.class)
    public static Predicate existsOnPostWithTitle(QTagMap tagMap, QPost post, String title) {
        if (title == null || title.isBlank()) return null;

        QTag tag = QTag.tag;

        return JPAExpressions.selectOne()
                .from(tagMap)
                .join(tagMap.tag, tag)
                .where(tagMap.post.eq(post).and(tag.title.eq(title)))
                .exists();
    }
}
