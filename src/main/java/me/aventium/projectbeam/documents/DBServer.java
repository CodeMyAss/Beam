package me.aventium.projectbeam.documents;

import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;
import org.bson.types.ObjectId;

import javax.annotation.Nullable;
import java.util.List;

public class DBServer extends Document {

    public static final String NAME_FIELD = "name";
    public static final String BUNGEE_NAME_FIELD = "bungee_name";
    public static final String SERVER_BOX_FIELD = "box";
    public static final String VISIBILITY_FIELD = "visibility";
    public static final String PRIORITY_FIELD = "priority";
    public static final String ONLINE_FIELD = "online";
    public static final String ONLINE_PLAYERS_FIELD = "online_players";
    public static final String ONLINE_STAFF_FIELD = "online_staff";
    public static final String MAX_PLAYERS_FIELD = "max_players";
    public static final String RESTRICTED_FIELD = "restricted";
    public static final String UPDATED_AT_FIELD = "updated_at";
    public static final String START_TIME_FIELD = "start_time";
    public static final String STOP_TIME_FIELD = "stop_time";
    public static final String RESTART_QUEUED = "restart_needed";

    public DBServer(DBObject obj) {
        super(obj);
    }

    public String getName() {
        return DBO.getString(this.dbo, NAME_FIELD);
    }

    public String getBungeeName() {
        return DBO.getString(this.dbo, BUNGEE_NAME_FIELD);
    }

    @Override
    public ObjectId getServerId() {
        return this.getId();
    }

    public String getServerBox() {
        return DBO.getString(this.dbo, SERVER_BOX_FIELD);
    }

    public Visibility getVisibility() {
        String databaseRepr = DBO.getString(this.dbo, VISIBILITY_FIELD);
        return Visibility.fromDatabase(databaseRepr);
    }

    public int getPriority() {
        return DBO.getInt(this.dbo, PRIORITY_FIELD, -1);
    }

    public boolean isOnline() {
        return DBO.getBoolean(this.dbo, ONLINE_FIELD, false);
    }

    public List<String> getOnlinePlayers() {
        return DBO.getStringList(this.dbo, ONLINE_PLAYERS_FIELD);
    }

    public List<String> getOnlineStaff() {
        return DBO.getStringList(this.dbo, ONLINE_STAFF_FIELD);
    }

    public int getMaxPlayers() {
        return DBO.getInt(this.dbo, MAX_PLAYERS_FIELD);
    }

    public boolean isRestricted() {
        return DBO.getBoolean(this.dbo, RESTRICTED_FIELD, false);
    }

    public boolean isRestartNeeded() {
        return DBO.getBoolean(this.dbo, RESTART_QUEUED, false);
    }

    public static enum Visibility {
        PUBLIC("public"),
        PRIVATE("private"),
        UNLISTED("unlisted"),
        UNKNOWN(null);

        private final @Nullable String databaseRepresentation;

        Visibility(@Nullable String databaseRepresentation) {
            this.databaseRepresentation = databaseRepresentation;
        }

        public @Nullable String getDatabaseRepresentation() {
            return this.databaseRepresentation;
        }

        public static Visibility fromDatabase(String databaseRepresentation) {
            for (Visibility vis : Visibility.values()) {
                if (vis.getDatabaseRepresentation() != null && vis.getDatabaseRepresentation().equals(databaseRepresentation)) {
                    return vis;
                }
            }
            return Visibility.UNKNOWN;
        }
    }
}
