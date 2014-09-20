package me.aventium.projectbeam.commands;

import me.aventium.projectbeam.collections.Collection;
import me.aventium.projectbeam.documents.Document;

public class SaveCommand extends DatabaseCommand {
    private final Collection collection;
    private final Document doc;

    public SaveCommand(Collection collection, Document doc) {
        this.collection = collection;
        this.doc = doc;
    }

    @Override
    public void run() {
        this.collection.save(this.doc);
    }
}
