package kom.utils;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.impl.java.CompilingClassLoader;

/**
 * User: syungman
 * Date: 20.06.13
 */
public class ClassLoadHelper {
    private static final Logger logger = LoggerFactory.getLogger(ClassLoadHelper.class);

    public static  <T> Class<T> loadClass(ClassLoader cl, String main, Class<T> targetClass)
            throws IllegalArgumentException {

        Class<?> result;

        try {
            if (isJavaSource(main)) {
                result = compileAndLoad(cl, main);
            } else {
                result = cl.loadClass(main);
            }
        } catch (ClassNotFoundException e) {
            final String message = "Class " + main + " not found";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }

        if (!targetClass.isAssignableFrom(result)) {
            final String message = "Class " + main
                    + " does not implement " + targetClass.getName();

            logger.error(message);
            throw new IllegalArgumentException(message);
        }

        //noinspection unchecked
        return (Class<T>)result;
    }

    private static Class<?> compileAndLoad(ClassLoader cl, String className) throws ClassNotFoundException {
        CompilingClassLoader compilingLoader = new CompilingClassLoader(cl, className);
        return compilingLoader.loadClass(compilingLoader.resolveMainClassName());
    }

    private static boolean isJavaSource(String main) {
        return main.endsWith(".java");
    }
}
