package me.aventium.projectbeam.documents;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Users;

import java.util.Date;
import java.util.UUID;

public class DBUser extends Document {

    public static final String USERNAME_FIELD = "username";
    public static final String USERNAME_LOWER_FIELD = "username_lower";
    public static final String UUID_FIELD = "uuid";
    public static final String SIGN_IN_COUNT_FIELD = "sign_in_count";
    public static final String LAST_SIGN_IN_DATE_FIELD = "last_sign_in_date";
    public static final String LAST_SIGN_IN_IP_FIELD = "last_sign_in_ip";
    public static final String GROUP_FIELD = "group";

    public DBUser(String username) {
        super();
        this.setUsername(username);
        this.setGroup(Database.getCollection(Users.class).find(new BasicDBObject(USERNAME_FIELD, username)) == null ? Database.getCollection(Groups.class).getDefaultGroup() : Database.getCollection(Users.class).find(new BasicDBObject(USERNAME_FIELD, username)).getGroup());
    }

    public DBUser(DBObject dbo) {
        super(dbo);
    }

    public String getUsername() {
        return DBO.getString(this.dbo, USERNAME_FIELD);
    }

    public String getUsernameLower() {
        return DBO.getString(this.dbo, USERNAME_LOWER_FIELD);
    }

    public UUID getUUID() {
        return UUID.fromString(DBO.getString(this.dbo, UUID_FIELD));
    }

    public void setUsername(String username) {
        this.dbo.put(USERNAME_FIELD, username);
        this.dbo.put(USERNAME_LOWER_FIELD, username.toLowerCase());
    }

    public void setUUID(UUID uuid) {
        this.dbo.put(UUID_FIELD, uuid.toString());
    }

    public int getSignInCount() {
        return DBO.getInt(this.dbo, SIGN_IN_COUNT_FIELD, 0);
    }

    public Date getLastSignInDate() {
        if(this.dbo.containsField(LAST_SIGN_IN_DATE_FIELD)) {
            return (Date) this.dbo.get(LAST_SIGN_IN_DATE_FIELD);
        } else {
            return new Date();
        }
    }

    public void setLastSignInDate(Date date) {
        this.dbo.put(LAST_SIGN_IN_DATE_FIELD, date);
    }

    public String getLastSignInIP() {
        return DBO.getString(this.dbo, LAST_SIGN_IN_IP_FIELD);
    }

    public void setLastSignInIP(String ip) {
        this.dbo.put(LAST_SIGN_IN_IP_FIELD, ip);
    }

    public DBGroup getGroup() {
        String group = DBO.getString(this.dbo, GROUP_FIELD);
        if(group == null) {
            DBGroup defGroup = Database.getCollection(Groups.class).getDefaultGroup();
            this.dbo.put(GROUP_FIELD, defGroup.getName());
            return defGroup;
        }
        return Database.getCollection(Groups.class).findGroup(DBO.getString(this.dbo, GROUP_FIELD), null);
    }

    public void setGroup(DBGroup group) {
        this.dbo.put(GROUP_FIELD, group.getName());
    }

}
