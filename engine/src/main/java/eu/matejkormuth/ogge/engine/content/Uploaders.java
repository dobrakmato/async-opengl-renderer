package eu.matejkormuth.ogge.engine.content;

import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import java.nio.file.Path;

@UtilityClass
public class Uploaders {

    public static <T, D> Uploader<T, D> get(Class<? extends Uploader> clazz) {
        return (Uploader<T, D>) Content.uploadersByName.get(clazz);
    }

    public static <T, D> Uploader<T, D> choosePerfect(@Nonnull Path path) {


        return null;
    }
}
