package me.aventium.projectbeam;

import org.bson.types.ObjectId;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.util.List;

public class Config {
    public static Configuration getConfiguration() {
        Beam pb = Beam.getInstance();
        if(pb != null) {
            return pb.getConfig();
        } else {
            return new YamlConfiguration();
        }
    }

    public static class Mongo {
        public static List<String> addresses() {
            return getConfiguration().getStringList("mongo.addresses");
        }

        public static String database() {
            return getConfiguration().getString("mongo.database", "");
        }

        public static @Nullable ObjectId serverId() {
            return ObjectId.massageToObjectId(getConfiguration().get("mongo.server_id"));
        }

        public static class Auth {
            public static boolean enabled() {
                return getConfiguration().getBoolean("mongo.auth.enabled", false);
            }

            public static String username() {
                return getConfiguration().getString("mongo.auth.username");
            }

            public static String password() {
                return getConfiguration().getString("mongo.auth.password");
            }
        }

        public static int connections() {
            return getConfiguration().getInt("mongo.connections", 10);
        }
    }

    public static class Tasks {
        public static int threadCount() {
            return getConfiguration().getInt("tasks.thread-count", 10);
        }
    }

    public static class Bungee {
        public static String fallbackServer() { return getConfiguration().getString("bungee.fallback-server-name", "fallback"); }
    }
}
