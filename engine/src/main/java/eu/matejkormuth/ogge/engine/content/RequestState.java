package eu.matejkormuth.ogge.engine.content;

public enum RequestState {
    /**
     * Load request is created and item is waiting to be loaded from disk.
     */
    CREATED,
    /**
     * Item is read from disk and now it's waiting to be internalDecodedObject.
     */
    IO_DONE,
    /**
     * Item is internalDecodedObject and waiting to be uploaded to GPU.
     */
    DECODED,
    /**
     * Item is being asynchronously uploaded.
     */
    UPLOADING_ASYNC,
    /**
     * Item is uploaded or finally processed and ready to use.
     */
    UPLOAD_DONE,
    /**
     * An error occurred while processing this request.
     */
    ERROR
}
