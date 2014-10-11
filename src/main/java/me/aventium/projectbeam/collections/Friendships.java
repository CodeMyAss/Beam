package me.aventium.projectbeam.collections;

import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.documents.DBFriendship;
import me.aventium.projectbeam.utils.MongoUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MongoCollection(collection = "friendships", database = "beam_friendships")
public class Friendships extends Collection {

    public List<DBFriendship> findRequests(String user) {
        DBObject object = new BasicDBObject(DBFriendship.FRIENDED_FIELD, user);
        object.put(DBFriendship.RESULT_FIELD, DBFriendship.Result.PENDING.toDBValue());

        List<DBFriendship> friends = new ArrayList<>();

        DBCursor curs = this.dbc().find(object);
        while(curs.hasNext()) {
            DBFriendship friendship = new DBFriendship(curs.next());
            friends.add(friendship);
        }
        return friends;
    }

    public List<String> findFriends(String exactUser) {
        BasicDBObjectBuilder query = BasicDBObjectBuilder.start();
        query.add(DBFriendship.FRIENDED_FIELD, MongoUtils.exists());
        query.add(DBFriendship.FRIENDER_FIELD, MongoUtils.exists());
        query.add("$or", ImmutableList.of(new BasicDBObject(DBFriendship.FRIENDED_FIELD, exactUser), new BasicDBObject(DBFriendship.FRIENDER_FIELD, exactUser)));
        query.add(DBFriendship.RESULT_FIELD, DBFriendship.Result.ACCEPTED.toDBValue());

        List<String> friends = new ArrayList<>();

        DBCursor cursor = this.dbc().find(query.get(), MongoUtils.fields(DBFriendship.FRIENDED_FIELD, DBFriendship.FRIENDER_FIELD));
        while (cursor.hasNext()) {
            DBFriendship friendship = new DBFriendship(cursor.next());
            String friendName = friendship.getFriend(exactUser);
            if (friendName != null) friends.add(friendName);
        }

        return friends;
    }

    public DBFriendship createFriendship(String friender, String friended) {
        DBObject dbo = new BasicDBObject();
        dbo.put(DBFriendship.FRIENDER_FIELD, friender);
        dbo.put(DBFriendship.FRIENDED_FIELD, friended);
        dbo.put(DBFriendship.SENT_DATE_FIELD, new Date());
        dbo.put(DBFriendship.SERVER_ID_FIELD, Database.getServerId());
        dbo.put(DBFriendship.SERVER_FAMILY_FIELD, Database.getServer().getFamily());
        DBFriendship friendship = new DBFriendship(dbo);
        save(friendship);
        return friendship;
    }

}
