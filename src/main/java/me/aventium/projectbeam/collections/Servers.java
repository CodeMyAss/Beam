package me.aventium.projectbeam.collections;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import me.aventium.projectbeam.Beam;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.documents.DBServer;
import me.aventium.projectbeam.documents.Document;
import org.bson.types.ObjectId;
import org.joda.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@MongoCollection(collection = "servers", database = "beam_servers")
public class Servers extends Collection {
    private final ConcurrentMap<ObjectId, DBServer> servers = Maps.newConcurrentMap();

    public void cacheAllServers() {
        for(DBObject obj : this.dbc().find()) {
            DBServer server = new DBServer(obj);
            this.servers.put(server.getId(), server);
        }
    }

    public @Nullable DBServer findServer(ObjectId id) {
        DBServer server = this.servers.get(id);
        if(server != null) {
            return server;
        }

        DBObject result = this.dbc().findOne(new BasicDBObject(Document.ID_FIELD, id));
        if(result == null) {
            return null;
        }

        server = new DBServer(result);
        this.servers.put(id, server);
        return server;
    }

    public @Nullable String getServerName(ObjectId id) {
        DBServer server = this.servers.get(id);
        return server == null ? null : server.getName();
    }

    public @Nullable String getServerFamily(ObjectId id) {
        DBServer server = this.servers.get(id);
        return server == null ? null : server.getFamily();
    }

    public @Nullable String findServerName(ObjectId id) {
        DBServer server = this.findServer(id);
        return server == null ? null : server.getName();
    }

    public @Nullable String findServerFamily(ObjectId id) {
        DBServer server = this.findServer(id);
        return server == null ? null : server.getFamily();
    }

    public List<DBServer> findPublicServers() {
        return this.findPublicServers(null);
    }

    public List<DBServer> findPublicServers(@Nullable String family) {

        if(family == null) return null;

        BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
                .add(DBServer.SERVER_FAMILY_FIELD, Pattern.compile("^" + Pattern.quote(family), Pattern.CASE_INSENSITIVE))
                .push(DBServer.VISIBILITY_FIELD)
                .add("$in", ImmutableList.of(DBServer.Visibility.PUBLIC.getDatabaseRepresentation(), DBServer.Visibility.UNLISTED.getDatabaseRepresentation()))
                .pop();

        DBObject sort = BasicDBObjectBuilder.start().add(DBServer.PRIORITY_FIELD, 1).add(DBServer.NAME_FIELD, 1).get();

        List<DBServer> servers = Lists.newArrayList();

        DBCursor cur = this.dbc().find(query.get()).sort(sort);
        while(cur.hasNext()) {
            servers.add(new DBServer(cur.next()));
        }

        return servers;
    }

    public DBServer findPublicServer(@Nonnull ObjectId id) {
        DBObject query = new BasicDBObject(DBServer.ID_FIELD, id);

        return new DBServer(this.dbc().findOne(query));
    }

    protected DBObject getQuery() {
        return this.getQuery(Database.getServerId());
    }

    protected DBObject getQuery(ObjectId serverId) {
        return new BasicDBObject(Document.ID_FIELD, serverId);
    }

    protected @Nullable DBObject findByServerId(ObjectId serverId) {
        return this.dbc().findOne(this.getQuery(serverId));
    }

    public void addOnlinePlayer(String publicName, String realName) {
        DBObject append = new BasicDBObject();

        append.put(DBServer.ONLINE_PLAYERS_FIELD, publicName);

        this.dbc().update(this.getQuery(), new BasicDBObject("$addToSet", append));
    }

    public void removeOnlinePlayer(String publicName, String realName) {
        DBObject pull = new BasicDBObject();
        pull.put(DBServer.ONLINE_PLAYERS_FIELD, publicName);

        this.dbc().update(this.getQuery(), new BasicDBObject("$pull", pull));
    }

    public void resetOnlinePlayers() {
        DBObject unset = new BasicDBObject();
        unset.put(DBServer.ONLINE_PLAYERS_FIELD, true);

        this.dbc().update(this.getQuery(), new BasicDBObject("$unset", unset));
    }

    public @Nullable DBServer findPublicServer(String name) {
        BasicDBObjectBuilder query = BasicDBObjectBuilder.start()
                .add(DBServer.NAME_FIELD, Pattern.compile("^" + Pattern.quote(name), Pattern.CASE_INSENSITIVE))
                .push(DBServer.VISIBILITY_FIELD)
                .add("$in", ImmutableList.of(DBServer.Visibility.PUBLIC.getDatabaseRepresentation(), DBServer.Visibility.UNLISTED.getDatabaseRepresentation()))
                .pop();

        DBObject result = this.dbc().findOne(query.get());

        if (result != null) {
            return new DBServer(result);
        } else {
            return null;
        }
    }

    public void update(ObjectId serverId) {
        DBObject query = new BasicDBObject();

        query.put(DBServer.MAX_PLAYERS_FIELD, Beam.getInstance().getServer().getMaxPlayers());
        query.put(DBServer.UPDATED_AT_FIELD, new Date());

        this.dbc().update(this.getQuery(serverId), new BasicDBObject("$set", query));
    }

    public void updateStartTime(ObjectId serverId, Instant startTime) {
        this.dbc().update(this.getQuery(serverId), new BasicDBObject("$set", new BasicDBObject(DBServer.START_TIME_FIELD, startTime.toDate())));
    }

    public void updateStopTime(ObjectId serverId, Instant stopTime) {
        this.dbc().update(this.getQuery(serverId), new BasicDBObject("$set", new BasicDBObject(DBServer.STOP_TIME_FIELD, stopTime.toDate())));
    }

    public void setMaxPlayers(ObjectId serverId, int max) {
        DBObject query = new BasicDBObject(DBServer.MAX_PLAYERS_FIELD, max);

        this.dbc().update(this.getQuery(serverId), new BasicDBObject("$set", query));
    }

    public void setVisibility(ObjectId serverId, DBServer.Visibility visibility) {
        DBObject query = new BasicDBObject(DBServer.VISIBILITY_FIELD, visibility.getDatabaseRepresentation());

        this.dbc().update(this.getQuery(serverId), new BasicDBObject("$set", query));
    }

    public void setFamily(ObjectId serverId, String family) {
        DBObject query = new BasicDBObject(DBServer.SERVER_FAMILY_FIELD, family);

        this.dbc().update(this.getQuery(serverId), new BasicDBObject("$set", query));
    }

    public void queueFamilyRestart(String family) {
        DBObject query = new BasicDBObject(DBServer.SERVER_FAMILY_FIELD, family);

        this.dbc().update(query, new BasicDBObject("$set", true));
    }

    public void queueRestart(ObjectId serverId) {
        DBObject query = new BasicDBObject(DBServer.RESTART_QUEUED, true);

        this.dbc().update(this.getQuery(serverId), new BasicDBObject("$set", query));
    }

    public List<DBServer> findPlayerOnlineServers(String username) {
        List<DBServer> servers = Lists.newArrayList();

        DBObject query = new BasicDBObject(DBServer.ONLINE_PLAYERS_FIELD, username);

        DBCursor cur = this.dbc().find(query);
        while(cur.hasNext()) {
            servers.add(new DBServer(cur.next()));
        }

        return servers;
    }

    public void cleanUp(ObjectId serverId, boolean online) {
        DBObject query = new BasicDBObject();

        query.put(DBServer.ONLINE_FIELD, online);
        query.put(DBServer.MAX_PLAYERS_FIELD, Beam.getInstance().getServer().getMaxPlayers());
        query.put(DBServer.ONLINE_PLAYERS_FIELD, Lists.newArrayList());
        query.put(DBServer.ONLINE_STAFF_FIELD, Lists.newArrayList());
        query.put(DBServer.UPDATED_AT_FIELD, new Date());
        query.put(DBServer.RESTART_QUEUED, false);

        this.dbc().update(this.getQuery(serverId), new BasicDBObject("$set", query));
        System.out.println("Cleaned up server information!");
    }
}
