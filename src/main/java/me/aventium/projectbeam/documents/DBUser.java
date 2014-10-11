package me.aventium.projectbeam.documents;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.collections.Groups;
import me.aventium.projectbeam.collections.Users;
import me.aventium.projectbeam.events.PlayerGroupChangeEvent;
import org.bukkit.Bukkit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;

public class DBUser extends Document {

    public static final String USERNAME_FIELD = "username";
    public static final String USERNAME_LOWER_FIELD = "username_lower";
    public static final String UUID_FIELD = "uuid";
    public static final String DATE_JOINED_FIELD = "date_joined";
    public static final String SIGN_IN_COUNT_FIELD = "sign_in_count";
    public static final String LAST_SIGN_IN_DATE_FIELD = "last_sign_in_date";
    public static final String LAST_SIGN_IN_IP_FIELD = "last_sign_in_ip";
    public static final String GROUP_FIELD = "group";
    public static final String PERMISSIONS_FIELD = "permissions";
    public static final String EMAIL_FIELD = "email";

    public static final String CONFIRMATION_TOKEN_FIELD = "confirmation_token";
    public static final String CONFIRMED_DATE_FIELD = "confirmed_at";

    public static final String TUTORIALS_COMPLETED_FIELD = "tutorials_completed";

    public DBUser(UUID uuid, String username) {
        super();
        this.setUsername(username);
        this.setUUID(uuid);
        DBUser u = Database.getCollection(Users.class).find(new BasicDBObject(UUID_FIELD, uuid.toString()));
        if(u == null || u.getGroup() == null) {
            this.setDateJoined(new Date());
            this.setGroup(Database.getCollection(Groups.class).getDefaultGroup());
            Database.getCollection(Users.class).save(this);
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

    public Date getDateJoined() {
        return DBO.getDate(this.dbo, DATE_JOINED_FIELD);
    }

    public void setDateJoined(Date date) {
        this.dbo.put(DATE_JOINED_FIELD, date);
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

    public DBGroup getGroup() {
        return Database.getCollection(Groups.class).findGroup(DBO.getString(this.dbo, GROUP_FIELD), null);
    }

    public void setGroup(DBGroup group) {
        this.dbo.put(GROUP_FIELD, group.getName());
        Bukkit.getPluginManager().callEvent(new PlayerGroupChangeEvent(this, group));
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

    public String getEmail() {
        return DBO.getString(this.dbo, EMAIL_FIELD);
    }

    public void setEmail(String email) {
        this.dbo.put(EMAIL_FIELD, email);
    }

    public String getConfirmationToken() {
        return DBO.getString(this.dbo, CONFIRMATION_TOKEN_FIELD);
    }

    public void setConfirmationToken(String token) {
        this.dbo.put(CONFIRMATION_TOKEN_FIELD, token);
    }

    public Date getConfirmedDate() {
        return (Date) this.dbo.get(CONFIRMED_DATE_FIELD);
    }

    public void setConfirmedDate(Date date) {
        this.dbo.put(CONFIRMED_DATE_FIELD, date);
    }

    public List<String> getTutorialsCompleted() {
        return DBO.getStringList(this.dbo, this.TUTORIALS_COMPLETED_FIELD);
    }

    public void completeTutorial(String tutorial) {
        List<String> C = getTutorialsCompleted();
        C.add(tutorial);
        this.dbo.put(this.TUTORIALS_COMPLETED_FIELD, C);
    }

    public boolean sendConfirmationEmail() {
        try {

            URL url = new URL("http://breakmc.com/sendconfirmation.php");
            URLConnection urlConn = url.openConnection();

            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(true);
            urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            DataOutputStream outputStream = new DataOutputStream(urlConn.getOutputStream());
            String content = "user_email" + URLEncoder.encode(this.getEmail(), "UTF-8");

            outputStream.writeBytes(content);
            outputStream.flush();
            outputStream.close();

            DataInputStream inputStream = new DataInputStream(urlConn.getInputStream());
            inputStream.close();

            return true;
        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
