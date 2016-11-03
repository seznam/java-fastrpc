package cz.seznam.frpc.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FrpcClientUtils {

    public static URL createURL(String urlString) {
        try {
            return new URL(Objects.requireNonNull(urlString));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error while creating URL from string " + urlString, e);
        }
    }

}
