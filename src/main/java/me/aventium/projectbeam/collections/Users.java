package me.aventium.projectbeam.collections;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.documents.DBUser;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@MongoCollection(collection = "users", database = "beam_users")
public class Users extends Collection {

    public BasicDBObject getNameQuery(String username) {
        return new BasicDBObject(DBUser.USERNAME_LOWER_FIELD, username.toLowerCase());
    }

    public BasicDBObject getEmailQuery(String email) {
        return new BasicDBObject(DBUser.EMAIL_FIELD, email.toLowerCase());
    }

    public DBUser find(DBObject query) {
        DBObject obj = this.dbc().findOne(query);
        return obj == null ? null : new DBUser(obj);
    }

    public DBUser findOrCreateByName(String username, UUID id) {
        DBUser user = this.find(this.getNameQuery(username));
        return user != null ? user : (id == null ? null : new DBUser(id, username));
    }

    public DBUser findOrCreate(Player player) {
        return this.findOrCreateByName(player.getName(), player.getUniqueId());
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

    public int usersWithEmail(String email) {
        return (int) this.dbc().count(this.getEmailQuery(email));
    }

    public DBUser handleLogin(String username, UUID uuid, String ip) {
        DBObject query = this.getNameQuery(username);

        DBUser user = new DBUser(uuid, username);
        user.setUUID(uuid.toString());
        user.setLastSignInDate(new Date());
        user.setLastSignInIP(ip);

        DBObject update = new BasicDBObject();
        update.put("$set", user.getDBO());
        update.put("$inc", new BasicDBObject(DBUser.SIGN_IN_COUNT_FIELD, 1));

        DBObject result = this.dbc().findAndModify(query, null, null, false, update, true, true);

        return new DBUser(result);
    }
}
