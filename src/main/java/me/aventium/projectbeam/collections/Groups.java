package me.aventium.projectbeam.collections;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import me.aventium.projectbeam.documents.DBGroup;

import java.util.ArrayList;
import java.util.List;

@MongoCollection(collection = "groups", database = "beam_groups")
public class Groups extends Collection {

    public DBGroup findGroup(String groupName, List<String> scopes) {
        DBObject query = new BasicDBObject(DBGroup.NAME_LOWER_FIELD, groupName.toLowerCase());
        if (scopes != null && !scopes.isEmpty()) {
            query.put(DBGroup.SCOPE_FIELD, new BasicDBObject("$in", scopes));
        }
        DBObject obj = this.dbc().findOne(query);
        if (obj != null) {
            return new DBGroup(obj);
        } else {
            return null;
        }
    }

    public List<DBGroup> findAllGroups() {
        List<DBGroup> groups = new ArrayList<>();
        DBCursor curs = this.dbc().find();
        while(curs.hasNext()) {
            DBObject object = curs.next();
            DBGroup group = new DBGroup(object);
            groups.add(group);
        }
        return groups;
    }

    public DBGroup getDefaultGroup() {
        DBCursor c = this.dbc().find();
        while(c.hasNext()) {
            DBObject obj = c.next();
            DBGroup group = new DBGroup(obj);
            if(group.isDefaultGroup()) return group;
        }
        return null;
    }

    /*public List<DBGroup> findUserGroups(String username) {
        return this.findUserGroups(username, Collections.<String>emptyList());
    }

    public List<DBGroup> findUserGroups(String username, List<String> scopes) {
        List<DBGroup> result = Lists.newArrayList();

        Date now = new Date();

        DBObject startClause = new BasicDBObject("$or", ImmutableList.of(
                new BasicDBObject("start", new BasicDBObject("$lte", now)),
                new BasicDBObject("start", null)
        ));

        DBObject endClause = new BasicDBObject("$or", ImmutableList.of(
                new BasicDBObject("end", new BasicDBObject("$gt", now)),
                new BasicDBObject("end", null)
        ));

        DBObject elemMatch = new BasicDBObject("$and", ImmutableList.of(
                new BasicDBObject("user", username),
                startClause,
                endClause
        ));

        DBObject query = new BasicDBObject("$or", ImmutableList.of(
                new BasicDBObject(DBGroup.MEMBERS_FIELD, username),
                new BasicDBObject(DBGroup.MEMBERS_TIMED_FIELD, new BasicDBObject("$elemMatch", elemMatch))
        ));

        if (scopes != null && !scopes.isEmpty()) {
            query.put(DBGroup.SCOPE_FIELD, new BasicDBObject("$in", scopes));
        }

        DBCursor cur = this.dbc().find(query);

        while (cur.hasNext()) {
            DBGroup group = new DBGroup(cur.next());
            result.add(group);
        }

        return result;
    }

    public void addUserToGroup(String group, String username) {
        DBObject query = new BasicDBObject(DBGroup.NAME_FIELD, group);
        DBObject update = new BasicDBObject("$addToSet", new BasicDBObject(DBGroup.MEMBERS_FIELD, username));

        this.dbc().update(query, update);
    }*/

}
