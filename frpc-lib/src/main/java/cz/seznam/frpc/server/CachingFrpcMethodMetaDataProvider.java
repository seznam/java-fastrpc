package cz.seznam.frpc.server;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Moidl david.moidl@firma.seznam.cz
 */
public abstract class CachingFrpcMethodMetaDataProvider implements FrpcMethodMetaDataProvider {

    protected Map<String, FrpcMethodMetaData> methodMetaDataMap = new HashMap<>();

    protected abstract FrpcMethodMetaData doGetMethodMetaData(String methodFrpcName) throws NoSuchMethodException;

    @Override
    public FrpcMethodMetaData getMethodMetaData(String methodFrpcName) throws NoSuchMethodException {
        // check if we already have the value
        FrpcMethodMetaData methodMetaData = methodMetaDataMap.get(methodFrpcName);
        // if we don't
        if(methodMetaData == null) {
            // delegate to subclass
            methodMetaData = doGetMethodMetaData(methodFrpcName);
            // and store it
            methodMetaDataMap.put(methodFrpcName, methodMetaData);
        }
        // return the value, either computed or cached
        return methodMetaData;
    }



}
