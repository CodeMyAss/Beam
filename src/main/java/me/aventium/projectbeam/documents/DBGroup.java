package me.aventium.projectbeam.documents;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;

import java.util.List;
import java.util.Map;

public class DBGroup extends Document {

    public static final String NAME_FIELD = "name";
    public static final String NAME_LOWER_FIELD = "name_lower";
    public static final String TYPE_FIELD = "type";
    public static final String PREFIX_FIELD = "prefix";
    public static final String PERMISSIONS_FIELD = "permissions";
    public static final String SCOPE_FIELD = "scope";
    public static final String DEFAULT_GROUP_FIELD = "default_group";

    public DBGroup(DBObject object) {
        super(object);
    }

    public DBGroup() {
        super();
    }

    public String getName() {
        return DBO.getString(this.dbo, NAME_FIELD);
    }

    public void setName(String name) {
        this.dbo.put(NAME_FIELD, name);
        this.dbo.put(NAME_LOWER_FIELD, name.toLowerCase());
    }

    public String getNameLower() {
        return DBO.getString(this.dbo, NAME_LOWER_FIELD);
    }

    public Type getType() { return Type.getFromDatabaseRepresentation(DBO.getString(this.dbo, TYPE_FIELD)); }

    public void setType(Type type) { this.dbo.put(TYPE_FIELD, type.name().toLowerCase()); }

    public String getServer() { return DBO.getString(this.dbo, SERVER_FAMILY_FIELD); }

    public void setServerFamily(String family) { this.dbo.put(SERVER_FAMILY_FIELD, family); }

    public String getPrefix() {
        return DBO.getString(this.dbo, PREFIX_FIELD);
    }

    public void setPrefix(String prefix) {
        this.dbo.put(PREFIX_FIELD, prefix);
    }

    public boolean isDefaultGroup() {
        return DBO.getBoolean(this.dbo, DEFAULT_GROUP_FIELD, false);
    }

    public void setDefaultGroup(boolean defaultGroup) {
        this.dbo.put(DEFAULT_GROUP_FIELD, defaultGroup);
    }

    public Map<String, Boolean> getPermissions() {
        Map<String, Boolean> result = Maps.newHashMap();

        List<String> permissions = DBO.getStringList(this.dbo, PERMISSIONS_FIELD);

        for(String perm : permissions) {
            boolean value = true;
            if(perm.startsWith("-")) {
                value = false;
                perm = perm.substring(1); // remove the prepended "-"
            }

            result.put(perm, value);
        }

        return result;
    }

    public void setPermissions(Map<String, Boolean> perms) {
        List<String> result = Lists.newArrayList();

        for(Map.Entry<String, Boolean> perm : perms.entrySet()) {
            result.add((perm.getValue() ? "" : "-") + perm.getKey());
        }

        this.dbo.put(PERMISSIONS_FIELD, result);
    }

    public static enum Type {
        SERVER,
        NETWORK;

        public static Type getFromDatabaseRepresentation(String str) {
            for(Type t : values()) {
                if(t.name().equalsIgnoreCase(str)) return t;
            }
            return null;
        }
    }

}
