package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.XmlRpcUtils;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.xml.sax.SAXException;

import java.io.OutputStream;

/**
 * Implementation of {@link AbstractFrpcResponseWriter} capable of writing responses into {@code XML-RPC} format.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class XmlFrpcResponseWriter extends AbstractFrpcResponseWriter {

    @Override
    protected void writeResponse(Object response, OutputStream outputStream) throws FrpcTransportException {
        // create new XmlRpcWriter
        XmlRpcWriter writer = XmlRpcUtils.newXmlRpcWriter(outputStream);
        // write the response
        try {
            writer.write(XmlRpcUtils.defaultRequestConfig(), response);
        } catch (SAXException e) {
            throw new FrpcTransportException("Error while writing XML-RPC response data", e);
        }
    }

    @Override
    protected void writeFault(FrpcFault fault, OutputStream outputStream) throws FrpcTransportException {
        // create new XmlRpcWriter
        XmlRpcWriter writer = XmlRpcUtils.newXmlRpcWriter(outputStream);
        // write the response
        try {
            writer.write(XmlRpcUtils.defaultRequestConfig(), fault.getStatusCode(), fault.getStatusMessage());
        } catch (SAXException e) {
            throw new FrpcTransportException("Error while writing XML-RPC response data", e);
        }
    }

}
