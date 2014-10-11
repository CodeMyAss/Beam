package me.aventium.projectbeam.collections;

import com.google.common.collect.Lists;
import com.mongodb.*;
import me.aventium.projectbeam.documents.DBSession;
import me.aventium.projectbeam.documents.Document;
import me.aventium.projectbeam.utils.MongoUtils;
import org.bson.types.ObjectId;
import org.joda.time.Instant;
import org.joda.time.Interval;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

@MongoCollection(collection = "sessions", database = "beam_sessions")
public class Sessions extends Collection {
    public void updateEnd(ObjectId serverId, String username, Instant end) {
        DBObject query = getEndQuery(serverId, end);
        query.put(DBSession.USER_FIELD, username);

        this.dbc().findAndModify(query, new BasicDBObject(DBSession.START_FIELD, -1), MongoUtils.setKeyValue(DBSession.END_FIELD, end.toDate()));
    }

    public void updateEnds(ObjectId serverId, Instant end) {
        DBObject update = BasicDBObjectBuilder.start().add(DBSession.END_FIELD, end.toDate()).add(DBSession.POST_TERM, true).get();

        this.dbc().update(getEndQuery(serverId, end), new BasicDBObject("$set", update), false, true);
    }

    private static DBObject getEndQuery(ObjectId serverId, Instant end) {
        return BasicDBObjectBuilder.start()
                .add(DBSession.SERVER_ID_FIELD, serverId)
                .add(DBSession.END_FIELD, new BasicDBObject("$exists", false))
                .get();
    }

    public @Nullable DBSession getLastSessionByUsername(String username, boolean seeThroughNicks) {
        BasicDBObject query = new BasicDBObject(DBSession.USER_FIELD, username);

        DBCursor result = this.dbc().find(query).sort(new BasicDBObject(DBSession.START_FIELD, -1)).limit(1);

        if(result.size() > 0) {
            return new DBSession(result.next());
        } else {
            return null;
        }
    }

    public @Nullable DBSession getLastSessionById(String id) {
        BasicDBObject query = new BasicDBObject(DBSession.USER_FIELD, id);

        DBCursor result = this.dbc().find(query).sort(new BasicDBObject(DBSession.START_FIELD, -1)).limit(1);

        if(result.size() > 0) {
            return new DBSession(result.next());
        } else {
            return null;
        }
    }

    public List<DBSession> findSessionsStartingBetween(Interval interval) {
        return this.findSessionsStartingBetween(interval, null);
    }

    public List<DBSession> findSessionsStartingBetween(Interval interval, List<String> families) {
        return this.findSessionsStartingBetween(interval.getStart().toDate(), interval.getEnd().toDate(), families);
    }

    public List<DBSession> findSessionsStartingBetween(Date start, Date end, List<String> families) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add(DBSession.START_FIELD, MongoUtils.range(start, end));
        if(families != null && !families.isEmpty()) {
            builder.add(Document.SERVER_FAMILY_FIELD, new BasicDBObject("$in", families)).get();
        }
        return fromCursor(this.dbc().find(builder.get()));
    }

    public List<DBSession> findSessionsEndingBetween(Interval interval) {
        return this.findSessionsEndingBetween(interval, null);
    }

    public List<DBSession> findSessionsEndingBetween(Interval interval, List<String> families) {
        return this.findSessionsEndingBetween(interval.getStart().toDate(), interval.getEnd().toDate(), families);
    }

    public List<DBSession> findSessionsEndingBetween(Date start, Date end, List<String> families) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add(DBSession.END_FIELD, MongoUtils.range(start, end));
        if(families != null && !families.isEmpty()) {
            builder.add(Document.SERVER_FAMILY_FIELD, new BasicDBObject("$in", families)).get();
        }
        return fromCursor(this.dbc().find(builder.get()));
    }

    private static List<DBSession> fromCursor(DBCursor cur) {
        List<DBSession> result = Lists.newArrayList();

        while(cur.hasNext()) {
            result.add(new DBSession(cur.next()));
        }

        return result;
    }

    @Override
    public DBCollection dbc() {
        return this.dbc(ReadPreference.primary());
    }
}
