package eu.matejkormuth.ogge.engine.content;

import eu.matejkormuth.ogge.engine.Application;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Slf4j
public class Request<T, D> {

    @Getter
    private final Priority priority;
    @Getter
    private final Path path;
    @Getter
    private final Decoder<T, D> decoder;
    @Getter
    private final Uploader<T, D> uploader;
    private final Consumer<Request<T, D>> successCallback;
    private final Consumer<Request<T, D>> errorCallback;

    @Getter
    final RequestTiming timing;

    @Getter
    RequestState state;
    @Getter
    String error = null;

    private ReentrantLock writeLock = new ReentrantLock();
    @Getter
    T item;

    /* malloc()-ated */ /* internal */ ByteBuffer internalFileContents;

    public Request(@Nonnull Path path) {
        this(path, RequestState.CREATED, Priority.NORMAL);
    }

    public Request(@Nonnull Path path, @Nonnull Priority priority) {
        this(path, RequestState.CREATED, priority);
    }

    public Request(@Nonnull Path path, @Nonnull RequestState state, @Nonnull Priority priority) {
        this.priority = priority;
        this.state = state;
        this.path = path;
        this.successCallback = null;
        this.errorCallback = null;

        this.decoder = Decoders.choosePerfect(path);
        this.uploader = Uploaders.choosePerfect(path);
        this.timing = Application.REQUEST_TIMING ? new RequestTiming() : null;
    }

    public Request(@Nonnull Path path, @Nonnull RequestState state, @Nonnull Priority priority,
                   @Nullable Consumer<Request<T, D>> successCallback, @Nullable Consumer<Request<T, D>> errorCallback) {
        this.priority = priority;
        this.state = state;
        this.path = path;
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;

        this.decoder = Decoders.choosePerfect(path);
        this.uploader = Uploaders.choosePerfect(path);
        this.timing = Application.REQUEST_TIMING ? new RequestTiming() : null;
    }

    public Request(@Nonnull Path path, @Nonnull RequestState state, @Nonnull Priority priority,
                   @Nullable Consumer<Request<T, D>> successCallback, @Nullable Consumer<Request<T, D>> errorCallback,
                   @Nonnull Decoder<T, D> decoder, @Nonnull Uploader<T, D> uploader) {
        this.priority = priority;
        this.state = state;
        this.path = path;
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;

        this.decoder = decoder;
        this.uploader = uploader;
        this.timing = Application.REQUEST_TIMING ? new RequestTiming() : null;
    }

    Request(Priority priority, Path path, RequestState state, Decoder<T, D> decoder, Uploader<T, D> uploader, Consumer<Request<T, D>>
            successCallback, Consumer<Request<T, D>> errorCallback, RequestTiming timing) {
        this.priority = priority;
        this.path = path;
        this.state = state;
        this.decoder = decoder;
        this.uploader = uploader;
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;
        this.timing = timing;
    }

    void callErrorCallback() {
        log.trace("Request {} errored in {}ms!", this, timing != null ? timing.getTotalTime() : "(timing not enabled)");

        if (errorCallback != null) {
            errorCallback.accept(this);
        }
    }

    void callSuccessCallback() {
        log.trace("Request {} completed in {}ms!", this, timing != null ? timing.getTotalTime() : "(timing not enabled)");

        if (successCallback != null) {
            successCallback.accept(this);
        }
    }

    public void setItem(T item) {
        writeLock.lock();
        this.item = item;
        writeLock.unlock();
    }

    @Override
    public String toString() {
        return "Request{" +
                "path=" + path +
                ", state=" + state +
                ", error='" + error + '\'' +
                '}';
    }

    public void complete(@Nonnull T item) {
        if (this.state == RequestState.UPLOADING_ASYNC) {
            if (this.timing != null) {
                timing.markAsyncUploadDone();
            }
        }

        this.item = item;
        this.state = RequestState.UPLOAD_DONE;
        callSuccessCallback();
    }
}
