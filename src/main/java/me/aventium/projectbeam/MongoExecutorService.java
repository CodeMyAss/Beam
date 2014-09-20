package me.aventium.projectbeam;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ForwardingExecutorService;
import com.mongodb.MongoException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class MongoExecutorService extends ForwardingExecutorService {
    public MongoExecutorService(@Nonnull ExecutorService backend) {
        Preconditions.checkNotNull(backend, "backend");

        this.backend = backend;
    }

    public <T> Future<T> submit(MongoCallable<T> task) {
        return this.delegate().submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return this.delegate().submit(new MongoCallable<>(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return this.delegate().submit(new MongoRunnable<>(task, null));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return this.delegate().submit(new MongoRunnable<>(task, result));
    }

    @Override
    public ExecutorService delegate() {
        return this.backend;
    }

    private @Nonnull ExecutorService backend;

    private static abstract class MongoTask<T> implements Callable<T> {
        public abstract T innerCall() throws Exception;
        public abstract String getInnerClassName();

        @Override
        public T call() throws Exception {
            while(true) {
                try {
                    return this.innerCall();
                } catch (MongoException.Network | IOException e) {
                    // if mongo has a network exception, try again
                } catch (Throwable e) {
                    Beam.getInstance().getLogger().log(Level.SEVERE,
                            "Uncaught " + e.getClass().getName() + " in async " + this.getInnerClassName(),
                            e);
                    throw e;
                }
            }
        }
    }

    private static class MongoCallable<T> extends MongoTask<T> {
        private final Callable<T> callable;

        public MongoCallable(@Nonnull Callable<T> callable) {
            Preconditions.checkNotNull(callable, "callable");

            this.callable = callable;
        }

        @Override
        public String getInnerClassName() {
            return this.callable.getClass().getName();
        }

        @Override
        public T innerCall() throws Exception {
            return this.callable.call();
        }
    }

    private static class MongoRunnable<T> extends MongoTask<T> {
        private final Runnable runnable;
        private final T result;

        private MongoRunnable(@Nonnull Runnable runnable, T result) {
            Preconditions.checkNotNull(runnable, "runnable");
            this.runnable = runnable;
            this.result = result;
        }

        @Override
        public String getInnerClassName() {
            return this.runnable.getClass().getName();
        }

        @Override
        public T innerCall() throws Exception {
            this.runnable.run();
            return this.result;
        }
    }
}