package com.example.cloud_story_be.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCommentDislike is a Querydsl query type for CommentDislike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCommentDislike extends EntityPathBase<CommentDislike> {

    private static final long serialVersionUID = -786839413L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCommentDislike commentDislike = new QCommentDislike("commentDislike");

    public final QComment comment;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QUser user;

    public QCommentDislike(String variable) {
        this(CommentDislike.class, forVariable(variable), INITS);
    }

    public QCommentDislike(Path<? extends CommentDislike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCommentDislike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCommentDislike(PathMetadata metadata, PathInits inits) {
        this(CommentDislike.class, metadata, inits);
    }

    public QCommentDislike(Class<? extends CommentDislike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.comment = inits.isInitialized("comment") ? new QComment(forProperty("comment"), inits.get("comment")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

