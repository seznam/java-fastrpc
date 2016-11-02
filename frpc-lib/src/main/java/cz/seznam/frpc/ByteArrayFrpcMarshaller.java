package cz.seznam.frpc;

import java.io.ByteArrayOutputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class ByteArrayFrpcMarshaller extends FrpcMarshaller {

    public ByteArrayFrpcMarshaller() {
        outputStream = new ByteArrayOutputStream();
    }

    public byte[] getBytes() {
        return ((ByteArrayOutputStream) outputStream).toByteArray();
    }

}
