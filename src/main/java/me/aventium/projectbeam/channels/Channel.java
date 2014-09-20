package me.aventium.projectbeam.channels;

public interface Channel<T> {
    boolean queue(T obj);

    void flush();

    void shutdown();

    void terminate();
}
