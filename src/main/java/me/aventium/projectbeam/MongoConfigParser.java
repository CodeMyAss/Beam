package me.aventium.projectbeam;

import com.google.common.collect.Lists;
import com.mongodb.ServerAddress;
import org.bson.types.ObjectId;
import org.bukkit.configuration.Configuration;

import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Logger;

public class MongoConfigParser {
    protected final Logger logger;

    public MongoConfigParser(Logger logger) {
        this.logger = logger;
    }

    public DatabaseConfiguration parse(Configuration config) {
        DatabaseConfiguration dbconf = new DatabaseConfiguration();

        dbconf.setAddresses(this.parseAddresses(config.getStringList("mongo.addresses")));
        dbconf.setDatabaseName(config.getString("mongo.database"));
        dbconf.setServerId(ObjectId.massageToObjectId(config.getString("mongo.server_id")));

        if(config.getBoolean("mongo.auth.enabled", false)) {
            dbconf.setAuth(true);
            dbconf.setUsername(config.getString("mongo.auth.username"));
            dbconf.setPassword(config.getString("mongo.auth.password"));
        }

        dbconf.setConnectionCount(config.getInt("mongo.connection-count", Config.Mongo.connections()));

        return dbconf;
    }

    private List<ServerAddress> parseAddresses(List<String> rawAddresses) {
        List<ServerAddress> addresses = Lists.newArrayList();
        for(String rawAddress : rawAddresses) {
            try {
                addresses.add(parseServerAddress(rawAddress));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return addresses;
    }

    public static ServerAddress parseServerAddress(String raw) throws IllegalArgumentException, UnknownHostException {
        String[] parts = raw.split(":");
        String host = parts[0];
        int port = 27017;
        if(parts.length > 1) {
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to parse port number for '" + host + "'");
            }
        }
        return new ServerAddress(host, port);
    }
}
