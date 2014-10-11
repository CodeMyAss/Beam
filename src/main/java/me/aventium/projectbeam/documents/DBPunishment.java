package me.aventium.projectbeam.documents;

import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.UUID;

public class DBPunishment extends Document {

    public static final String PLAYER_FIELD = "player";
    public static final String PLAYER_LOWER_FIELD = "player_lower";
    public static final String SERVER_ID_FIELD = "server_id";
    public static final String ISSUER_FIELD = "issuer";
    public static final String TYPE_FIELD = "type";
    public static final String REASON_FIELD = "reason";
    public static final String CREATED_FIELD = "time";
    public static final String EXPIRES_FIELD = "expires";
    public static final String ACTIVE_FIELD = "active";

    public DBPunishment() {
        super();
    }

    public DBPunishment(DBObject dbObject) {
        super(dbObject);
    }

    public String getPlayer() {
        return DBO.getString(this.dbo, PLAYER_FIELD);
    }

    public void setPlayer(UUID id) {
        this.dbo.put(PLAYER_FIELD, id);
    }

    public String getPlayerLower() {
        return DBO.getString(this.dbo, PLAYER_LOWER_FIELD);
    }

    public ObjectId getServerId() {
        return DBO.getObjectId(this.dbo, SERVER_ID_FIELD);
    }

    public void setServerId(ObjectId serverId) {
        this.dbo.put(SERVER_ID_FIELD, serverId);
    }

    public String getIssuer() {
        return DBO.getString(this.dbo, ISSUER_FIELD);
    }

    public void setIssuer(String username) {
        this.dbo.put(ISSUER_FIELD, username);
    }

    public Type getType() {
        String dbType = DBO.getString(this.dbo, TYPE_FIELD);

        for(Type type : Type.values()) {
            if(type.getDBValue().equalsIgnoreCase(dbType)) return type;
        }
        return null;
    }

    public void setType(Type type) {
        this.dbo.put(TYPE_FIELD, type.getDBValue());
    }

    public String getReason() {
        return DBO.getString(this.dbo, REASON_FIELD);
    }

    public void setReason(String reason) {
        this.dbo.put(REASON_FIELD, reason);
    }

    public Date getTimeCreated() {
        return (Date) this.dbo.get(CREATED_FIELD);
    }

    public void setTimeCreated(Date date) {
        this.dbo.put(CREATED_FIELD, date);
    }

    public Date getExpiry() {
        return (Date) this.dbo.get(EXPIRES_FIELD);
    }

    public void setExpiry(Date date) {
        this.dbo.put(EXPIRES_FIELD, date);
    }

    public boolean isActive() {
        return DBO.getBoolean(this.dbo, ACTIVE_FIELD, true);
    }

    public void setActive(boolean active) {
        this.dbo.put(ACTIVE_FIELD, active);
    }


    public static enum Type {
        WARN("warn"),
        KICK("kick"),
        MUTE("mute"),
        BAN("ban");

        private final String dbVal;

        private Type(String dbVal) {
            this.dbVal = dbVal;
        }

        public String getDBValue() {
            return dbVal;
        }
    }

}
