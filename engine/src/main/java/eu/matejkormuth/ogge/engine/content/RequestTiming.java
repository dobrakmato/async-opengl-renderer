package eu.matejkormuth.ogge.engine.content;

import lombok.Getter;


public final class RequestTiming {

    /**
     * Time (milliseconds) spent while request was waiting in queue.
     */
    @Getter
    double queueWaitTime;

    /**
     * Time (milliseconds) it took to read the file from disk.
     */
    @Getter
    double diskTime; //

    /**
     * Time (milliseconds) it took to download the file from remote server.
     */
    @Getter
    double networkTime;

    /**
     * Time (milliseconds) it took to decode the file using decoder.
     */
    @Getter
    double decodeTime;

    /**
     * Time (milliseconds) it took to finish synchronous part of uploader.
     */
    @Getter
    double uploadSyncTime;

    /**
     * Time (milliseconds) it took to finish (if applicable) asynchronous
     * part (from start of async. uploading to signalization of success
     * or error) of uploader.
     */
    @Getter
    double uploadAsyncTime;

    /**
     * Returns total time spent on this request in milliseconds.
     *
     * @return time spent on this request in milliseconds
     */
    public double getTotalTime() {
        return queueWaitTime + diskTime + networkTime + decodeTime + uploadSyncTime + uploadAsyncTime;
    }

    /**
     * Returns total time spent on this request in milliseconds.
     *
     * @return time spent on this request in milliseconds
     */
    public double getTotalProcessingTime() {
        return diskTime + networkTime + decodeTime + uploadSyncTime + uploadAsyncTime;
    }

    private long queueEnterNanos; // added to queue temp var
    private long asyncUploadStartNanos; // UPLOADING_ASYNC returned

    void markQueueEnter() {
        queueEnterNanos = System.nanoTime();
    }

    void markQueueLeave() {
        queueWaitTime += (System.nanoTime() - queueEnterNanos) / 1_000_000.0;
    }

    void markAsyncUploadStart() {
        asyncUploadStartNanos = System.nanoTime();
    }

    void markAsyncUploadDone() {
        uploadAsyncTime += (System.nanoTime() - asyncUploadStartNanos) / 1_000_000.0;
    }

    @Override
    public String toString() {
        return String.format("RequestTiming{queueWaitTime=%.2fms, diskTime=%.2fms, networkTime=%.2fms, decodeTime=%.2fms, " +
                        "uploadSyncTime=%.2fms, uploadAsyncTime=%.2fms, totalProcessingTime=%.2fms, totalTime=%.2fms}",
                queueWaitTime, diskTime, networkTime, decodeTime, uploadSyncTime,
                uploadAsyncTime, getTotalProcessingTime(), getTotalTime());
    }
}
