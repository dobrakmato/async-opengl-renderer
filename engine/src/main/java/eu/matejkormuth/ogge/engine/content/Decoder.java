package eu.matejkormuth.ogge.engine.content;

import eu.matejkormuth.ogge.engine.AutoFreed;
import eu.matejkormuth.ogge.engine.ThreadSafe;

import java.nio.ByteBuffer;

public interface Decoder<T, D> {
    /**
     * @param request request to decode
     * @param read    read data from file
     * @return internalDecodedObject object from data
     * @throws Exception when exception occurs during decoding
     */
    @ThreadSafe
    D decode(Request<T, D> request, @AutoFreed ByteBuffer read) throws Exception;
}
