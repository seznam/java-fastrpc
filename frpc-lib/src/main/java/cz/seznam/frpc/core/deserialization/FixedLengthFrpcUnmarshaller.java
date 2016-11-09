package cz.seznam.frpc.core.deserialization;

import java.io.InputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class FixedLengthFrpcUnmarshaller extends CountingFrpcUnmarshaller {

    private long contentLength;

    public FixedLengthFrpcUnmarshaller(InputStream inputStream, long contentLength) {
        super(inputStream);
        this.contentLength = contentLength;
    }

    public boolean isFinished() {
        return getBytesRead() == contentLength;
    }

}
