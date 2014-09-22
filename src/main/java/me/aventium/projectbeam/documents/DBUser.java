package me.aventium.projectbeam.documents;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DBUser extends Document {

    public static final String USERNAME_FIELD = "username";
    public static final String USERNAME_LOWER_FIELD = "username_lower";
    public static final String UUID_FIELD = "uuid";
    public static final String SIGN_IN_COUNT_FIELD = "sign_in_count";
    public static final String LAST_SIGN_IN_DATE_FIELD = "last_sign_in_date";
    public static final String LAST_SIGN_IN_IP_FIELD = "last_sign_in_ip";
    public static final String GROUPS_FIELD = "groups";

    public DBUser(String username) {
        super();
        this.setUsername(username);
        DBUser u = Database.getCollection(Users.class).find(new BasicDBObject(USERNAME_FIELD, username));
        if(u == null || u.getGroups() == null || u.getGroups().size() == 0) {
            this.addGroup(Database.getCollection(Groups.class).getDefaultGroup());
        } else {
            for(DBGroup g : u.getGroups()) {
                this.addGroup(g);
            }
        }
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

    public List<DBGroup> getGroups() {
        List<DBGroup> list = new ArrayList<>();
        for(String g : DBO.getStringList(this.dbo, GROUPS_FIELD)) {
            DBGroup gr = Database.getCollection(Groups.class).findGroup(g.split(":")[0], g.split(":")[1], null);
            if(gr != null) list.add(gr);
        }
        return list;
    }

    public void addGroup(DBGroup group) {
        List<DBGroup> groups = getGroups();
        List<String> sgroups = new ArrayList<>();
        for(DBGroup gr : groups) {
            sgroups.add(gr.getName() + ":" + gr.getFamily());
        }
        sgroups.add(group.getName() + ":" + group.getFamily());
        this.dbo.put(GROUPS_FIELD, sgroups);
    }

    public void removeGroup(DBGroup group) {
        List<String> sgroups = new ArrayList<>();
        for(DBGroup g : getGroups()) {
            if(!g.getName().equalsIgnoreCase(group.getName()) && !g.getFamily().equalsIgnoreCase(group.getFamily())) sgroups.add(g.getName() + ":" + g.getFamily());
        }

        this.dbo.put(GROUPS_FIELD, sgroups);
    }

}
