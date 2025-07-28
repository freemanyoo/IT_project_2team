package webproject_2team.lunch_matching.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPartyBoardEntity is a Querydsl query type for PartyBoardEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPartyBoardEntity extends EntityPathBase<PartyBoardEntity> {

    private static final long serialVersionUID = -478777046L;

    public static final QPartyBoardEntity partyBoardEntity = new QPartyBoardEntity("partyBoardEntity");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> deadline = createDateTime("deadline", java.time.LocalDateTime.class);

    public final StringPath foodCategory = createString("foodCategory");

    public final StringPath genderLimit = createString("genderLimit");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final StringPath locationName = createString("locationName");

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final DateTimePath<java.time.LocalDateTime> partyTime = createDateTime("partyTime", java.time.LocalDateTime.class);

    public final StringPath status = createString("status");

    public final StringPath title = createString("title");

    public final NumberPath<Long> writerId = createNumber("writerId", Long.class);

    public QPartyBoardEntity(String variable) {
        super(PartyBoardEntity.class, forVariable(variable));
    }

    public QPartyBoardEntity(Path<? extends PartyBoardEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPartyBoardEntity(PathMetadata metadata) {
        super(PartyBoardEntity.class, metadata);
    }

}

