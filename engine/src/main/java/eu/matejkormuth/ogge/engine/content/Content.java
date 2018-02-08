package eu.matejkormuth.ogge.engine.content;

import eu.matejkormuth.ogge.engine.Pair;
import eu.matejkormuth.ogge.engine.content.decoders.STBImageDecoder;
import eu.matejkormuth.ogge.engine.content.uploaders.SimpleAsyncTextureUploader;
import eu.matejkormuth.ogge.engine.gl.api.Texture2D;
import eu.matejkormuth.ogge.engine.image.SimpleImage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@UtilityClass
public class Content {

    private static ContentLoader contentLoader;

    static final Map<String, Decoder<?, ?>> decoders = new HashMap<>();
    static final Map<Pair<Class<?>, Class<?>>, Uploader<?, ?>> uploaders = new HashMap<>();

    static final Map<Class<?>, Decoder<?, ?>> decodersByName = new HashMap<>();
    static final Map<Class<?>, Uploader<?, ?>> uploadersByName = new HashMap<>();

    static final List<Path> roots = new ArrayList<>();

    public static void initialize() {
        if (contentLoader != null) {
            throw new RuntimeException("already initialized");
        }

        addDecoder(ext("png", "jpeg", "jpg", "bmp", "tga", "gif", "psd", "hdr", "pic"), new STBImageDecoder());
        addUploader(pair(Texture2D.class, SimpleImage.class), new SimpleAsyncTextureUploader());

        contentLoader = new ContentLoader();
        contentLoader.start();

        addRoot(Paths.get("C:\\Users\\Matej\\IdeaProjects\\ogge\\engine\\src\\main\\resources"));

        printDebug();
    }

    private static void printDebug() {
        log.trace("Registered decoders: ");
        for (Map.Entry<String, Decoder<?, ?>> entry : decoders.entrySet()) {
            log.trace(" Extension: .{}\tDecoder: {}", entry.getKey(), entry.getValue().getClass().getSimpleName());
        }
    }

    private static void addRoot(Path path) {
        path = path.toAbsolutePath();
        if (!Files.exists(path)) {
            log.warn("Adding non-existing content root {}!", path);
        }
        roots.add(path);
    }

    public Path resolve(Path relative) {
        for (Path root : roots) {
            Path resolved = root.resolve(relative);
            if (Files.exists(resolved)) {
                return resolved;
            }
        }
        throw new RuntimeException("File " + relative + " not found in any root!");
    }

    public static void shutdown() {
        contentLoader.stop();
    }

    private static <T, D> void addDecoder(String[] extensions, Decoder<T, D> decoder) {
        decodersByName.put(decoder.getClass(), decoder);
        for (String ext : extensions) {
            decoders.put(ext, decoder);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T, D> void addUploader(Pair<Class<?>, Class<?>> pair, Uploader<T, D> uploader) {
        Pair<Class<?>, Class<?>>[] pairs = new Pair[]{pair};
        addUploader(pairs, uploader);
    }

    private static <T, D> void addUploader(Pair<Class<?>, Class<?>>[] pairs, Uploader<T, D> uploader) {
        uploadersByName.put(uploader.getClass(), uploader);
        for (Pair<Class<?>, Class<?>> pair : pairs) {
            uploaders.put(pair, uploader);
        }
    }

    private static Pair<Class<?>, Class<?>> pair(Class<?> t, Class<?> d) {
        return new Pair<>(t, d);
    }

    private static String[] ext(String... extensions) {
        return extensions;
    }

    public static <T, D> Request<T, D> request(Path path, Consumer<Request<T, D>> success,
                                               Consumer<Request<T, D>> error) {
        Request<T, D> request = new Request<>(path, RequestState.CREATED, Priority.NORMAL, success, error);
        contentLoader.addRequest(request);
        return request;
    }

    public static <T, D> Request<T, D> request(Path path, Consumer<Request<T, D>> success,
                                               Consumer<Request<T, D>> error, Decoder<T, D> decoder,
                                               Uploader<T, D> uploader) {
        Request<T, D> request = new Request<>(path, RequestState.CREATED, Priority.NORMAL, success, error,
                decoder, uploader);
        contentLoader.addRequest(request);
        return request;
    }

    static void request(Request req) {
        contentLoader.addRequest(req);
    }

    public static <T, D> RequestBuilder<T, D> request() {
        return new RequestBuilder<>();
    }

    public static String loadText(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
