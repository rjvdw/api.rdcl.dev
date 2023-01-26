package dev.rdcl.www.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class Resources {

    /**
     * Read the specified resource as a byte array.
     *
     * @param name The name of the resource to read.
     * @return a byte array containing the bytes read from this input stream
     * @throws IOException if an I/O error occurs
     */
    public static byte[] readResource(String name) throws IOException {
        try (InputStream is = getClassLoader().getResourceAsStream(name)) {
            Objects.requireNonNull(is);
            return is.readAllBytes();
        }
    }

    /**
     * Returns the context {@code ClassLoader} for this thread.
     *
     * @return the context {@code ClassLoader} for this thread,
     * or {@code null} indicating the system class loader
     * (or, failing that, the bootstrap class loader)
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
