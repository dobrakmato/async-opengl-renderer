package eu.matejkormuth.ogge.engine.content;

import eu.matejkormuth.ogge.engine.ThreadSafe;

import javax.annotation.Nonnull;

@ThreadSafe
public interface Uploader<T, D> {
    /**
     * @param request request to upload
     * @param decoded internalDecodedObject object
     * @return whether the uploading is finished (RequestState.UPLOAD_DONE) or
     * in progress (RequestState.UPLOADING_ASYNC). Other values are prohibited.
     * @throws Exception when exception occurs during upload
     */
    RequestState upload(Request<T, D> request, @Nonnull D decoded) throws Exception;
}
