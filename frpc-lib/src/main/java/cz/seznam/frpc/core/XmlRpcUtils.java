package cz.seznam.frpc.core;

import org.apache.ws.commons.serialize.XMLWriter;
import org.apache.ws.commons.serialize.XMLWriterImpl;
import org.apache.xmlrpc.XmlRpcRequestConfig;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.serializer.XmlRpcWriter;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public class XmlRpcUtils {

    private static class XmlRpcConfigImpl implements XmlRpcStreamRequestConfig {

        @Override
        public String getEncoding() {
            return StandardCharsets.UTF_8.name();
        }

        @Override
        public boolean isEnabledForExtensions() {
            return true;
        }

        @Override
        public TimeZone getTimeZone() {
            return TimeZone.getTimeZone("Europe/Prague");
        }

        @Override
        public boolean isGzipCompressing() {
            return false;
        }

        @Override
        public boolean isGzipRequesting() {
            return false;
        }

        @Override
        public boolean isEnabledForExceptions() {
            return false;
        }
    }

    private static final XmlRpcConfigImpl XML_RPC_CONFIG = new XmlRpcConfigImpl();

    public static XmlRpcRequestConfig defaultRequestConfig() {
        return XML_RPC_CONFIG;
    }

    public static XmlRpcStreamConfig defaultStreamConfig() {
        return XML_RPC_CONFIG;
    }

    public static XmlRpcStreamRequestConfig defaultStreamRequestConfig() {
        return XML_RPC_CONFIG;
    }

    public static XmlRpcWriter newXmlRpcWriter(OutputStream outputStream) {
        return new XmlRpcWriter(defaultStreamConfig(), newXmlWriter(outputStream),
                new TypeFactoryImpl(null));
    }

    public static XMLWriter newXmlWriter(OutputStream outputStream) {
        // create XMLWriter
        XMLWriter xmlWriter = new XMLWriterImpl();
        // set encoding
        xmlWriter.setEncoding(StandardCharsets.UTF_8.name());
        // let it generate the XML header
        xmlWriter.setDeclarating(true);
        // no indenting
        xmlWriter.setIndenting(false);
        // enable flushing
        xmlWriter.setFlushing(true);
        // create writer which will write to given output stream
        xmlWriter.setWriter(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)));
        // return the handler
        return xmlWriter;
    }


}
