import com.esotericsoftware.kryo.Kryo
@Grapes([
        @Grab(group = 'org.apache.httpcomponents', module = 'httpclient', version = '4.5.2'),
        @Grab(group = 'com.github.d0k1.jsflight', module = 'recorder', version = '3.8'),
        @Grab(group = 'com.esotericsoftware', module = 'kryo', version = '4.0.0')
])
import com.esotericsoftware.kryo.io.FastInput
import com.focusit.jsflight.recorder.internalevent.httprequest.HttpRecordInformation

class RestoredRequest {
    Map<String, String[]> parameters;
    Long contentLength;
    String contentType;
    String uri;
    String method;
}

RestoredRequest requests = [];

FastInput input = new FastInput(new FileInputStream(args[0]))

Kryo kryo = new Kryo();

while (input.available() > 0) {
    HttpRecordInformation information = kryo.readClassAndObject(input);
    FastInput paramsInput = new FastInput(new ByteArrayInputStream(information.params));

    RestoredRequest request = new RestoredRequest();
    request.parameters = kryo.readObject(paramsInput, HashMap.class);
    request.contentLength = kryo.readObject(paramsInput, Long.class);
    request.contentType = kryo.readObject(paramsInput, String.class);
    request.uri = kryo.readObject(paramsInput, String.class);
    request.method = kryo.readObject(paramsInput, String.class);

    requests << request;
}
