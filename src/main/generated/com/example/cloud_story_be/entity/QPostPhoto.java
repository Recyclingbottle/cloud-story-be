package com.example.cloud_story_be.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostPhoto is a Querydsl query type for PostPhoto
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostPhoto extends EntityPathBase<PostPhoto> {

    private static final long serialVersionUID = 1639167885L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostPhoto postPhoto = new QPostPhoto("postPhoto");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> photoOrder = createNumber("photoOrder", Integer.class);

    public final QPost post;

    public final StringPath url = createString("url");

    public QPostPhoto(String variable) {
        this(PostPhoto.class, forVariable(variable), INITS);
    }

    public QPostPhoto(Path<? extends PostPhoto> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostPhoto(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostPhoto(PathMetadata metadata, PathInits inits) {
        this(PostPhoto.class, metadata, inits);
    }

    public QPostPhoto(Class<? extends PostPhoto> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.post = inits.isInitialized("post") ? new QPost(forProperty("post"), inits.get("post")) : null;
    }

}

