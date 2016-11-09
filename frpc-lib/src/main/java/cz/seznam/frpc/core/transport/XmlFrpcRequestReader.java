package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.FrpcDataException;
import cz.seznam.frpc.core.XmlRpcUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.parser.XmlRpcRequestParser;
import org.apache.xmlrpc.util.SAXParsers;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class XmlFrpcRequestReader implements FrpcRequestReader {

    @Override
    @SuppressWarnings("unchecked")
    public FrpcRequest read(InputStream inputStream, long contentLength) throws FrpcTransportException {
        // create new XmlRpcResponseParser as a content handler for SAX parser
        XmlRpcRequestParser requestParser = new XmlRpcRequestParser(XmlRpcUtils.defaultStreamConfig(),
                new TypeFactoryImpl(null));
        try {
            // create XML reader
            XMLReader xmlReader = SAXParsers.newXMLReader();
            // set content handler
            xmlReader.setContentHandler(requestParser);
            // try to parse the data
            xmlReader.parse(new InputSource(inputStream));
            // if the parsing succeeded, create new FrpcRequest out of parsed data and return it
            return new FrpcRequest(requestParser.getMethodName(), requestParser.getParams());
        } catch (XmlRpcException | SAXException | IOException e) {
            throw new FrpcDataException("Error while trying to parse request data", e);
        }
    }

}
