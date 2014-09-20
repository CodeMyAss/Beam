package me.aventium.projectbeam.collections;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * INDEXES:
 *
 *      db.users.ensureIndex({"nickname_lower": 1}, {sparse: true, unique: true})
 */

@MongoCollection(collection = "users", database = "beam_users")
public class Users extends Collection {

    public BasicDBObject getNameQuery(String username) {
        return new BasicDBObject(DBUser.USERNAME_LOWER_FIELD, username.toLowerCase());
    }

    public DBUser find(DBObject query) {
        DBObject obj = this.dbc().findOne(query);
        return obj == null ? null : new DBUser(obj);
    }

    public DBUser findOrCreateByName(String username) {
        DBUser user = this.find(this.getNameQuery(username));
        return user != null ? user : new DBUser(username);
    }

    public DBUser findOrCreate(Player player) {
        return this.findOrCreateByName(player.getName());
    }

    public DBUser findByName(String name) {
        return this.find(this.getNameQuery(name));
    }

    public List<DBUser> findMulti(DBObject query) {
        List<DBUser> results = new ArrayList<>();
        for(DBObject obj : this.dbc().find(query)) {
            results.add(new DBUser(obj));
        }
        return results;
    }

    public List<DBUser> findMultiByName(List<String> names) {
        BasicDBList lowerNames = new BasicDBList();
        for(String name : names) {
            lowerNames.add(name.toLowerCase());
        }
        DBObject query = new BasicDBObject(DBUser.USERNAME_LOWER_FIELD, new BasicDBObject("$in", lowerNames));
        return this.findMulti(query);
    }

    public DBUser handleLogin(String username, UUID uuid, String ip) {
        DBObject query = this.getNameQuery(username);

        DBUser user = new DBUser(username);
        user.setUUID(uuid);
        user.setLastSignInDate(new Date());
        user.setLastSignInIP(ip);

        DBObject update = new BasicDBObject();
        update.put("$set", user.getDBO());
        if(Database.getCollection(Sessions.class).getLastSessionByUsername(username, true).getEnd() != null) update.put("$inc", new BasicDBObject(DBUser.SIGN_IN_COUNT_FIELD, 1));

        DBObject result = this.dbc().findAndModify(query, null, null, false, update, true, true);

        return new DBUser(result);
    }
}
