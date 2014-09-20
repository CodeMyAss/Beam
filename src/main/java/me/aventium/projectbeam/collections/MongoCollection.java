package me.aventium.projectbeam.collections;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MongoCollection {
    public String collection();

    public String database();
}
