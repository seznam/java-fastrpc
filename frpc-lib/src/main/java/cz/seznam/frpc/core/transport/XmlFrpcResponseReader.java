package cz.seznam.frpc.core.transport;

import cz.seznam.frpc.core.FrpcDataException;
import cz.seznam.frpc.core.FrpcResponseUtils;
import cz.seznam.frpc.core.XmlRpcUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.parser.XmlRpcResponseParser;
import org.apache.xmlrpc.util.SAXParsers;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class XmlFrpcResponseReader implements FrpcResponseReader {

    @Override
    public Object read(InputStream inputStream, long contentLength) throws FrpcTransportException {
        // create new XmlRpcResponseParser as a content handler for SAX parser
        XmlRpcResponseParser requestParser = new XmlRpcResponseParser(XmlRpcUtils.defaultStreamRequestConfig(),
                new TypeFactoryImpl(null));
        try {
            // create XML reader
            XMLReader xmlReader = SAXParsers.newXMLReader();
            // set content handler
            xmlReader.setContentHandler(requestParser);
            // try to parse the data
            xmlReader.parse(new InputSource(inputStream));
            // get parsed object
            Object response;
            if(requestParser.isSuccess()) {
                response = requestParser.getResult();
            } else {
                response = FrpcResponseUtils.response(requestParser.getErrorCode(), requestParser.getErrorMessage());
            }
            // return the parsed object
            return response;
        } catch (XmlRpcException | SAXException | IOException e) {
            throw new FrpcDataException("Error while trying to parse request data", e);
        }
    }
}
