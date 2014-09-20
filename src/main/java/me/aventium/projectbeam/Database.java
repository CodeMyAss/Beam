package me.aventium.projectbeam;

import com.google.common.collect.Maps;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import me.aventium.projectbeam.collections.Collection;
import me.aventium.projectbeam.collections.Servers;
import me.aventium.projectbeam.documents.DBServer;
import org.bson.types.ObjectId;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Overcast's?
 */
public class Database {

    private static MongoClient _mongo = null;
    private static DB _db = null;
    private static ObjectId _serverId = null;

    private static DatabaseConfiguration _config = new DatabaseConfiguration();

    private static Map<Class<? extends Collection>, Collection> _collections = Maps.newConcurrentMap();

    private static MongoExecutorService _executor = null;

    public static boolean isConnected() {
        return _db != null && _serverId != null;
    }

    public static DB getMasterDB() throws IllegalStateException {
        if(_db != null) {
            return _db;
        } else {
            throw new MongoException.Network("database connection is down", new IOException("no connection to mongo database found"));
        }
    }

    public static MongoClient getMongo() throws IllegalStateException {
        if(_mongo != null) {
            return _mongo;
        } else {
            throw new MongoException.Network("database connection is down", new IOException("no connection to mongo database found"));
        }
    }

    public static MongoExecutorService getExecutorService() {
        return _executor;
    }

    public static @Nullable ObjectId getServerId() {
        return _serverId;
    }

    public static @Nullable
    DBServer getServer() {
        return _serverId == null ? null : getCollection(Servers.class).findServer(_serverId);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Collection> T getCollection(Class<T> cls) {
        return (T) _collections.get(cls);
    }

    public static DatabaseConfiguration getConfig() {
        return _config;
    }

    public static void setConfig(DatabaseConfiguration config) {
        _config = config;
    }

    public static void reconnect() throws IllegalStateException, MongoException {
        DB newDB = _config.connect(); // may throw
        _serverId = _config.getServerId();
        _db = newDB;
        _mongo = ((MongoClient) newDB.getMongo());
    }

    public static void disconnect() {
        _db.getMongo().close();
        _db = null;
        _mongo = null;
    }

    public static void setUpExecutorService(int threadCount) {
        _executor = new MongoExecutorService(Executors.newFixedThreadPool(threadCount));
    }

    public static void tearDownExecutorService() {
        tearDownExecutorService(false);
    }

    public static void tearDownExecutorService(boolean immediate) {
        if (immediate) {
            _executor.shutdownNow();
        } else {
            _executor.shutdown();
        }
        _executor = null;
    }

    public static void registerCollection(Collection coll) {
        _collections.put(coll.getClass(), coll);
    }

    static {
        registerCollection(new Servers());
    }

}
