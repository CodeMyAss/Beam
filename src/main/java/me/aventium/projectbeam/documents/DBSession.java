package me.aventium.projectbeam.documents;

import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;
import org.bson.types.ObjectId;

import java.util.Date;

public class DBSession extends Document {

    public static final String SERVER_ID_FIELD = "server_id";
    public static final String USER_FIELD = "user";
    public static final String IP_FIELD = "ip";
    public static final String START_FIELD = "start";
    public static final String END_FIELD = "end";
    public static final String POST_TERM = "post_term";

    public DBSession() {
        super();
    }

    public DBSession(DBObject dbo) {
        super(dbo);
    }

    public DBSession(ObjectId serverId, String username, String ip) {
        super();
        this.setServerId(serverId);
        this.setUser(username);
        this.setIPAddress(ip);
    }

    public ObjectId getServerId() {
        return DBO.getObjectId(this.dbo, SERVER_ID_FIELD);
    }

    public void setServerId(ObjectId id) {
        this.dbo.put(SERVER_ID_FIELD, id);
    }

    public String getUser() {
        return DBO.getString(this.dbo, USER_FIELD);
    }

    public void setUser(String username) {
        this.dbo.put(USER_FIELD, username);
    }

    public String getIPAddress() {
        return DBO.getString(this.dbo, IP_FIELD);
    }

    public void setIPAddress(String ip) {
        this.dbo.put(IP_FIELD, ip);
    }

    public Date getStart() {
        return (Date) this.dbo.get(START_FIELD);
    }

    public void setStart(Date when) {
        this.dbo.put(START_FIELD, when);
    }

    public Date getEnd() {
        return (Date) this.dbo.get(END_FIELD);
    }

    public void setEnd(Date when) {
        this.dbo.put(END_FIELD, when);
    }

    public boolean wasPostTerm() {
        return DBO.getBoolean(this.dbo, POST_TERM, false);
    }

    public void setPostTerm(boolean postTerm) {
        this.dbo.put(POST_TERM, postTerm);
    }

}
