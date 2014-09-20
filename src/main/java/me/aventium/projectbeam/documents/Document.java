package me.aventium.projectbeam.documents;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import me.aventium.projectbeam.DBO;
import org.bson.types.ObjectId;

import javax.annotation.Nullable;

public abstract class Document {

    public static final String ID_FIELD = "_id";
    public static final String SERVER_ID_FIELD = "server_id";
    public static final String SERVER_FAMILY_FIELD = "family";

    protected final DBObject dbo;

    public Document() {
        this(new BasicDBObject());
    }

    public Document(DBObject dbo) {
        this.dbo = dbo == null ? new BasicDBObject() : dbo;
    }

    public ObjectId getId() {
        return (ObjectId) this.dbo.get(ID_FIELD);
    }

    public void setId(ObjectId id) {
        if(this.dbo.containsField(ID_FIELD)) {
            throw new IllegalArgumentException("May not set a new id if there is already one");
        } else {
            this.dbo.put(ID_FIELD, id);
        }
    }

    public @Nullable
    ObjectId getServerId() {
        return DBO.getObjectId(this.dbo, SERVER_ID_FIELD);
    }

    public @Nullable String getFamily() {
        return DBO.getString(this.dbo, SERVER_FAMILY_FIELD);
    }

    public DBObject getDBO() {
        return this.dbo;
    }
}
