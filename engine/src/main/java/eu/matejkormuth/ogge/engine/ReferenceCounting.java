package eu.matejkormuth.ogge.engine;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class ReferenceCounting implements Disposable {

    private final AtomicInteger refCount = new AtomicInteger(0);

    @Getter
    @Setter
    private boolean autoDispose = true;

    public int getReferenceCount() {
        return refCount.get();
    }

    public <T> T addReference() {
        refCount.incrementAndGet();
        return (T) this;
    }

    public <T> T removeReference() {
        int cnt = refCount.decrementAndGet();
        if (autoDispose && cnt <= 0) {
            log.info("Disposing object {}. Reference count reached zero.", this);
            this.dispose();
        }
        return (T) this;
    }


}