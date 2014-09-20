package me.aventium.projectbeam;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mongodb.*;
import org.bson.types.ObjectId;

import javax.annotation.Nullable;
import java.util.List;

public class DatabaseConfiguration {
    protected List<ServerAddress> addresses = Lists.newArrayList();
    protected String databaseName = null;
    protected ObjectId serverId = null;

    protected boolean useAuth = false;
    protected String username = null;
    protected String password = null;

    protected int connectionCount = -1;

    public List<ServerAddress> getAddresses() {
        return this.addresses;
    }

    public void setAddress(DBAddress address) {
        this.addresses.clear();
        this.addresses.add(new ServerAddress(address.getSocketAddress()));
        this.databaseName = address.getDBName();
    }

    public void setAddresses(List<ServerAddress> addresses) {
        this.addresses = addresses;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(String dbName) {
        this.databaseName = dbName;
    }

    public ObjectId getServerId() {
        return this.serverId;
    }

    public void setServerId(ObjectId id) {
        this.serverId = id;
    }

    public boolean getAuth() {
        return this.useAuth;
    }

    public void setAuth(boolean useAuth) {
        this.useAuth = useAuth;
    }

    public @Nullable String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public @Nullable String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionCount() {
        return this.connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public DB connect() throws IllegalStateException, MongoException {
        Preconditions.checkState(this.addresses.size() > 0, "No addresses available");
        Preconditions.checkState(this.databaseName != null, "No database specified");
        Preconditions.checkState(this.serverId != null, "No server id specified");

        if(this.useAuth) {
            Preconditions.checkState(this.username != null, "No username specified");
            Preconditions.checkState(this.password != null, "No password specified");
        }

        Preconditions.checkState(this.connectionCount > 0, "Invalid connection count specified");

        // set up addresses
        MongoClient mongo;
        MongoClientOptions options = MongoClientOptions.builder().connectionsPerHost(this.connectionCount).build();
        if(this.addresses.size() == 1) {
            mongo = new MongoClient(this.addresses.get(0), options);
        } else {
            mongo = new MongoClient(this.addresses, options);
        }
        mongo.setReadPreference(ReadPreference.primary());

        // get the database
        DB db = mongo.getDB(this.databaseName);

        // try to auth
        if(this.useAuth) {
            if(!db.authenticate(this.username, this.password.toCharArray())) {
                throw new MongoException("Failed to authenticate");
            }
        }

        // verify server
        System.out.println("Verifying server with id: " + this.serverId);
        DBObject serverObj = mongo.getDB("beam_servers").getCollection("servers").findOne(new BasicDBObject("_id", this.serverId));
        if(serverObj == null) {
            throw new MongoException("Failed to verify server");
        }

        return db;
    }
}
