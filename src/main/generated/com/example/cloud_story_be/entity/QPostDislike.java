package com.example.cloud_story_be.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostDislike is a Querydsl query type for PostDislike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostDislike extends EntityPathBase<PostDislike> {

    private static final long serialVersionUID = 1254280128L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostDislike postDislike = new QPostDislike("postDislike");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPost post;

    public final QUser user;

    public QPostDislike(String variable) {
        this(PostDislike.class, forVariable(variable), INITS);
    }

    public QPostDislike(Path<? extends PostDislike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostDislike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostDislike(PathMetadata metadata, PathInits inits) {
        this(PostDislike.class, metadata, inits);
    }

    public QPostDislike(Class<? extends PostDislike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new QPost(forProperty("post"), inits.get("post")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

