package eu.matejkormuth.ogge.engine.content;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.nio.file.Path;

@UtilityClass
public class Decoders {

    public static <T, D> Decoder<T, D> choosePerfect(@Nonnull Path path) {
        String ext = getFileExtensions(path);
        Decoder<T, D> decoder = (Decoder<T, D>) Content.decoders.get(ext);
        if (decoder == null) {
            throw new RuntimeException("No decoder found for file of type: " + ext);
        }
        return decoder;
    }

    private static String getFileExtensions(Path path) {
        String name = path.toString();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }
}
