package me.aventium.projectbeam.collections;

import com.google.common.collect.Lists;
import com.mongodb.*;
import me.aventium.projectbeam.documents.DBPunishment;
import me.aventium.projectbeam.documents.Document;
import me.aventium.projectbeam.utils.MongoUtils;
import org.joda.time.Interval;

import java.util.Date;
import java.util.List;

@MongoCollection(collection = "punishments", database = "beam_punishments")
public class Punishments extends Collection {
    public List<DBPunishment> getActivePunishments(String playerName) {
        List<DBPunishment> result = Lists.newArrayList();

        DBObject query = new BasicDBObject();
        query.put(DBPunishment.PLAYER_LOWER_FIELD, playerName.toLowerCase());
        query.put(DBPunishment.ACTIVE_FIELD, true);

        DBCursor cur = this.dbc().find(query).sort(new BasicDBObject(DBPunishment.CREATED_FIELD, -1)).setReadPreference(ReadPreference.primary());
        while(cur.hasNext()) {
            result.add(new DBPunishment(cur.next()));
        }

        return result;
    }

    public List<DBPunishment> findPunishmentsBetween(Interval interval) {
        return this.findPunishmentsBetween(interval, null);
    }

    public List<DBPunishment> findPunishmentsBetween(Interval interval, List<String> families) {
        return this.findPunishmentsBetween(interval.getStart().toDate(), interval.getEnd().toDate(), families);
    }

    public List<DBPunishment> findPunishmentsBetween(Date start, Date end, List<String> families) {
        List<DBPunishment> result = Lists.newArrayList();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add(DBPunishment.CREATED_FIELD, MongoUtils.range(start, end));
        if(families != null && !families.isEmpty()) {
            builder.add(Document.SERVER_FAMILY_FIELD, new BasicDBObject("$in", families));
        }

        DBCursor cur = this.dbc().find(builder.get());
        while(cur.hasNext()) {
            result.add(new DBPunishment(cur.next()));
        }

        return result;
    }
}
