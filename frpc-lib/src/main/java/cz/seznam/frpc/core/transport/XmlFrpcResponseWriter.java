package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.XmlRpcUtils;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.xml.sax.SAXException;

import java.io.OutputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class XmlFrpcResponseWriter implements FrpcResponseWriter {

    @Override
    public void write(Object response, OutputStream outputStream) throws FrpcTransportException {
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
    public void writeFault(FrpcFault fault, OutputStream outputStream) throws FrpcTransportException {
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
