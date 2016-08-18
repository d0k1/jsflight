package com.focusit.jsflight.player.http2jmeter;

import java.io.*;
import java.util.*;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.FastInput;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.focusit.jsflight.recorder.internalevent.InternalEventRecorder;
import com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecordInformation;

/**
 * This example shows how to convert raw traffic recorded by HttpRecorder to JMeter scenario to reproduce a load
 * Created by dkirpichenkov on 17.08.16.
 */
public class Http2JMeter {
    private static final String GUEST_USER = "GUEST";

    static class RestoredRequest {
        Map<String, String[]> parameters;
        Integer contentLength;
        String contentType;
        String uri;
        String method;
        String contextPath;
        HashMap<String, String> cookies;
        HashMap<String, String> headers;
        String payload;
        HashMap additional;

        @Override
        public String toString() {
            return "RestoredRequest{" + "parameters=" + parameters + ", contentLength=" + contentLength
                    + ", contentType='" + contentType + '\'' + ", uri='" + uri + '\'' + ", method='" + method + '\''
                    + ", contextPath='" + contextPath + '\'' + ", cookies=" + cookies + ", payload=" + payload
                    + ", additional=" + additional + '}';
        }
    }

    public List<RestoredRequest> getRequests(String file) throws IOException {
        List<RestoredRequest> requests = new ArrayList<>();
        FastInput input = new FastInput(new FileInputStream(file));

        Kryo kryo = new Kryo();
        kryo.register(HashMap.class, new MapSerializer());
        while (input.available() > 0) {
            InternalEventRecorder.InternalEventRecord record = kryo.readObject(input,
                    InternalEventRecorder.InternalEventRecord.class);
            HttpRecordInformation information = (HttpRecordInformation) record.data;

            String tag = new String(record.tag).trim();

            if (tag.equalsIgnoreCase(
                    com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecorder.HTTP_RECORDER_TAG)) {

                RestoredRequest request = new RestoredRequest();

                if (information.params != null && information.params.length > 0) {
                    FastInput paramsInput = new FastInput(new ByteArrayInputStream(information.params));

                    request.parameters = kryo.readObject(paramsInput, HashMap.class);
                    request.headers = kryo.readObject(paramsInput, HashMap.class);
                    request.contentLength = kryo.readObjectOrNull(paramsInput, Integer.class);
                    request.contentType = kryo.readObjectOrNull(paramsInput, String.class);
                    request.uri = kryo.readObjectOrNull(paramsInput, String.class);
                    request.method = kryo.readObjectOrNull(paramsInput, String.class);
                    request.contextPath = kryo.readObjectOrNull(paramsInput, String.class);
                    request.cookies = kryo.readObject(paramsInput, HashMap.class);
                }
                if (information.payload != null && information.payload.length > 0) {
                    request.payload = new String(information.payload);
                } else {

                }
                request.additional = information.additional;
                requests.add(request);

            }
        }
        return requests;
    }

    public Map<String, List<RestoredRequest>> getRequestsByUsers(List<RestoredRequest> requests) {
        HashMap<String, List<RestoredRequest>> result = new HashMap<>();

        for (RestoredRequest request : requests) {
            String uuid = (String) request.additional.get("uuid");
            if (uuid == null) {
                uuid = GUEST_USER;
            }
            if (uuid != null && uuid.trim().length() > 0) {
                List<RestoredRequest> filtered = result.get(uuid);
                if (filtered == null) {
                    filtered = new ArrayList<>();
                    result.put(uuid, filtered);
                }

                filtered.add(request);
            }
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        Http2JMeter converter = new Http2JMeter();
        List<RestoredRequest> requests = converter.getRequests(args[0]);
        Map<String, List<RestoredRequest>> byUuid = converter.getRequestsByUsers(requests);

        System.out.println(byUuid.size());
        String pathToTemplate = args[1];

        HashTree hashTree;
        JMeterUtils.setJMeterHome(new File("").getAbsolutePath());
        JMeterUtils.loadJMeterProperties(new File("jmeter.properties").getAbsolutePath());
        JMeterUtils.setProperty("saveservice_properties", File.separator + "saveservice.properties");
        JMeterUtils.setProperty("user_properties", File.separator + "user.properties");
        JMeterUtils.setProperty("upgrade_properties", File.separator + "upgrade.properties");
        JMeterUtils.setProperty("system_properties", File.separator + "system.properties");
        JMeterUtils.setLocale(Locale.ENGLISH);

        JMeterUtils.setProperty("proxy.cert.directory", new File("").getAbsolutePath());
        hashTree = SaveService.loadTree(new File(pathToTemplate));

        final TestPlan[] plan = {null};
        final HashTree[] planTree = new HashTree[1];

        long sampleCounter = 0;

        hashTree.traverse(new HashTreeTraverser() {

            @Override
            public void addNode(Object node, HashTree subTree) {
                System.out.println("Node: " + node.toString());
                if (node instanceof TestPlan) {
                    plan[0] = (TestPlan) node;
                    planTree[0] = subTree;
                }
            }

            @Override
            public void processPath() {
            }

            @Override
            public void subtractNode() {
            }
        });

        for (String key : byUuid.keySet()) {
            ThreadGroup group = new ThreadGroup();
            group.setProperty(TestElement.GUI_CLASS, "ThreadGroupGui");
            group.setName("Group_" + key);

            LoopController ctrl = new LoopController();
            group.setSamplerController(ctrl);

            HashTree groupTree = hashTree.add(group);
            CookieManager mngr = null;
            if (!key.equals(GUEST_USER)) {
                mngr = new CookieManager();
                mngr.setName("Cookies_" + key);
                mngr.setProperty(TestElement.GUI_CLASS, "CookiePanel");
                groupTree.add(mngr);
            }

            TransactionController txCtrl = new TransactionController();
            txCtrl.setName("Tx_" + key);
            txCtrl.setProperty(TestElement.GUI_CLASS, "TransactionControllerGui");
            HashTree txTree = groupTree.add(txCtrl);

            for (RestoredRequest request : byUuid.get(key)) {
                HTTPSampler sample = new HTTPSampler();
                sample.setProperty(TestElement.GUI_CLASS, "HttpTestSampleGui");
                sample.setName(sampleCounter + " Sample");
                sample.setDomain("${host}");
                sample.setProperty(HTTPSampler.PORT, "${port}");
                //sample.setCookieManager(mngr);
                sample.setMethod(request.method);
                sample.setPath(request.contextPath);
                sample.setMethod(request.method);
                Arguments sampleArgs = new Arguments();

                if (request.payload != null && request.payload.trim().length() > 0) {
                    HTTPArgument arg = new HTTPArgument();
                    arg.setAlwaysEncoded(false);
                    arg.setValue(request.payload);
                    sampleArgs.addArgument(arg);
                    sample.setPostBodyRaw(true);
                }
                if (!key.equals(GUEST_USER)) {
                    HTTPArgument loginArg = new HTTPArgument();
                    loginArg.setAlwaysEncoded(true);
                    loginArg.setName("loginAs");
                    loginArg.setValue(key);
                    sampleArgs.addArgument(loginArg);
                }

                sample.setFollowRedirects(true);
                sample.setUseKeepAlive(true);
                sample.setArguments(sampleArgs);
                HashTree sampleTree = txTree.add(sample);

                if (request.headers.size() > 0) {
                    HeaderManager headerManager = new HeaderManager();
                    headerManager.setName(sampleCounter + " Headers");
                    headerManager.setProperty(TestElement.GUI_CLASS, "HeaderPanel");

                    for (Map.Entry<String, String> entry : request.headers.entrySet()) {
                        if (!entry.getKey().toLowerCase().startsWith("cookie")) {
                            headerManager.add(new Header(entry.getKey(), entry.getValue()));
                        }
                    }
                    sampleTree.add(headerManager);
                }

                sampleCounter++;
            }
        }

        SaveService.saveTree(hashTree, new FileOutputStream(new File("result.jmx")));
    }
}
