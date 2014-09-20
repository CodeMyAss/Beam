package me.aventium.projectbeam.collections;

import com.google.common.base.Preconditions;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import me.aventium.projectbeam.Database;
import me.aventium.projectbeam.documents.DBGroup;
import me.aventium.projectbeam.documents.Document;

public abstract class Collection {
    protected final String databaseName;
    protected final String collectionName;

    public Collection() {
        MongoCollection info = this.getClass().getAnnotation(MongoCollection.class);
        Preconditions.checkState(info != null, "Collection must have the MongoCollection annotation");

        this.databaseName = info.database();
        this.collectionName = info.collection();
    }

    public void save(Document doc) {
        // apply some global settings
        DBObject dbo = doc.getDBO();

        if(!(doc instanceof DBGroup)) {
            if(doc.getServerId() == null) {
                dbo.put(Document.SERVER_ID_FIELD, Database.getServerId());
            }

            if(doc.getFamily() == null) {
                String family = Database.getCollection(Servers.class).findServerFamily(doc.getServerId()); // hopefully cached
                if(family != null) {
                    dbo.put(Document.SERVER_FAMILY_FIELD, family);
                }
            }
        }

        this.dbc().save(dbo);
    }

    public void update(DBObject q, DBObject o) {
        this.dbc().update(q, o);
    }

    protected DB getDB() {
        return Database.getMongo().getDB(this.databaseName);
    }

    protected DBCollection dbc() {
        return this.dbc(this.getDB().getReadPreference());
    }

    protected DBCollection dbc(ReadPreference readPref) {
        DBCollection collection = this.getDB().getCollection(this.collectionName);
        collection.setReadPreference(readPref);
        return collection;
    }

    @Override
    public String toString() {
        return "Collection{databaseName=" + this.databaseName + ", collectionName=" + this.collectionName + "}";
    }
}
