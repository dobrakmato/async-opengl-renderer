package eu.matejkormuth.ogge.engine.content;

import eu.matejkormuth.ogge.engine.Application;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class RequestBuilder<T, D> {
    private Path path = null;
    private RequestState state = RequestState.CREATED;
    private Priority priority = Priority.NORMAL;
    private boolean timingsEnabled = Application.REQUEST_TIMING;
    private Decoder<T, D> decoder;
    private Uploader<T, D> uploader;
    private Consumer<Request<T, D>> successCallback = null;
    private Consumer<Request<T, D>> errorCallback = null;


    public RequestBuilder<T, D> path(Path path) {
        this.path = path;
        return this;
    }

    public RequestBuilder<T, D> state(RequestState state) {
        this.state = state;
        return this;
    }

    public RequestBuilder<T, D> priority(Priority priority) {
        this.priority = priority;
        return this;
    }

    public RequestBuilder<T, D> timings(boolean timingsEnabled) {
        this.timingsEnabled = timingsEnabled;
        return this;
    }

    public RequestBuilder<T, D> decoder(Decoder<T, D> decoder) {
        this.decoder = decoder;
        return this;
    }

    public RequestBuilder<T, D> uploader(Uploader<T, D> uploader) {
        this.uploader = uploader;
        return this;
    }

    public RequestBuilder<T, D> success(Consumer<Request<T, D>> successCallback) {
        this.successCallback = successCallback;
        return this;
    }

    public RequestBuilder<T, D> error(Consumer<Request<T, D>> errorCallback) {
        this.errorCallback = errorCallback;
        return this;
    }

    public Request<T, D> dispatch() {
        if (path == null) {
            throw new IllegalStateException("path is null!");
        }

        RequestTiming timings = timingsEnabled ? new RequestTiming() : null;
        Request<T, D> req = new Request<>(priority, path, state, decoder, uploader, successCallback, errorCallback, timings);
        Content.request(req);
        return req;
    }
}
