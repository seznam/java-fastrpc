package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.XmlRpcUtils;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcRequestConfig;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.xml.sax.SAXException;

import java.io.OutputStream;
import java.util.List;

/**
 * Specialization of {@link FrpcRequestWriter} capable of writing {@code FrpcRequest}s into {@code XML-RPC} format.
 *
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class XmlFrpcRequestWriter implements FrpcRequestWriter {

    @Override
    public void write(FrpcRequest request, OutputStream outputStream) throws FrpcTransportException {
        // create new XmlRpcWriter
        XmlRpcWriter writer = XmlRpcUtils.newXmlRpcWriter(outputStream);
        // create XmlRpcRequest to be written
        XmlRpcRequest xmlRpcRequest = new XmlRpcRequestImpl(request.getMethodName(), request.getParameters());
        // try to write it
        try {
            writer.write(xmlRpcRequest);
        } catch (SAXException e) {
            throw new FrpcTransportException("Error while writing XML-RPC request data", e);
        }
    }

    private class XmlRpcRequestImpl implements XmlRpcRequest {

        private String methodName;
        private List<Object> parameters;

        private XmlRpcRequestImpl(String methodName, List<Object> parameters) {
            this.methodName = methodName;
            this.parameters = parameters;
        }

        @Override
        public XmlRpcRequestConfig getConfig() {
            return XmlRpcUtils.defaultRequestConfig();
        }

        @Override
        public String getMethodName() {
            return methodName;
        }

        @Override
        public int getParameterCount() {
            return parameters.size();
        }

        @Override
        public Object getParameter(int pIndex) {
            return parameters.get(pIndex);
        }

    }

}
