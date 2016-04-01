import com.google.gwt.user.server.rpc.*;

def addPath(String s) throws Exception {
	/*
	def f = new File(s);
	def u = f.toURL();
	def urlClassLoader = java.lang.ClassLoader.getSystemClassLoader();
	def urlClass = java.net.URLClassLoader.class;
	java.lang.Class clsz[] = [java.net.URL.class];
	Method method = urlClass.getDeclaredMethod("addURL", new Class[]{ });
	method.setAccessible(true);
	method.invoke(urlClassLoader, new Object[]{u});
	*/
	this.getClass().classLoader.rootLoader.addURL(new File(s).toURL());
	System.err.println("Classpath modified " + Class.forName("com.google.gwt.user.server.rpc.RPC"));
}

def modifyClassPath(){
	def cp = System.getProperty("cp");
	if(ctx.getProperty('classpath')==null && cp!=null){
		addPath(System.getProperty("cp"))
		//ctx.addProperty('policy', SerializationPolicyLoader.loadFromStream(new FileInputStream(System.getProperty("sp")), null));
		ctx.addProperty('classpath', new Object());
	}
}

def isItAddAction(String body) {
	modifyClassPath();

	return body.contains('AddObjectAction');
}

def isItEditAction(String body) {
	modifyClassPath();

	return body.contains('EditObjectAction');
}

def isItDeleteAction(String body) {
	modifyClassPath();

	return body.contains('DeleteObjectAction');
}

def processBatch(def batch, clazz){
	batch.getActions().each({
		if(it.getClass().getName().equals(clazz)){
			return it;
		}
	})

	return null;
}

def getAddObjectActionResult(String response){
	modifyClassPath();

	def rpcRequest = RPC.decodeRequest(payload);
	def test = rpcRequest.rpcRequest.getParameters()[0];
	if(test.getClass().getName("net.customware.gwt.dispatch.shared.BatchAction")){
		test = processBatch(test, "ru.naumen.core.shared.dispatch.AddObjectAction");
	}

	if(test.getClass().getName().equals("ru.naumen.core.shared.dispatch.AddObjectAction")){

	}
}

def getEditObjectActionResult(String request){
	modifyClassPath();

	def rpcRequest = com.google.gwt.user.server.rpc.RPC.decodeRequest(payload);

	def test = rpcRequest.rpcRequest.getParameters()[0];
	if(test.getClass().getName("net.customware.gwt.dispatch.shared.BatchAction")){
		test = processBatch(test, "ru.naumen.core.shared.dispatch.EditObjectAction");
	}

	if(!(test.getClass().getName().equals("ru.naumen.core.shared.dispatch.EditObjectAction"))){
		return null;
	}

	return test.getUuid();
}

def getDeleteObjectActionResult(String request){
	modifyClassPath();

	def rpcRequest = com.google.gwt.user.server.rpc.RPC.decodeRequest(payload);
	def test = rpcRequest.rpcRequest.getParameters()[0];
	if(test.getClass().getName("net.customware.gwt.dispatch.shared.BatchAction")){
		test = processBatch(test, "ru.naumen.core.shared.dispatch.DeleteObjectAction");
	}

	if(!(test.getClass().getName().equals("ru.naumen.core.shared.dispatch.DeleteObjectAction"))){
		return null;
	}

	return test.getUUID()
}

if(!request.getUrl().toString().contains('sd40.naumen.ru'))
{
	System.out.println("Skipped " + request.getUrl()+" Response code " + response.getResponseCode());
	return false;
}

def counter = com.focusit.jmeter.JMeterProxyCounter.getInstance().counter.incrementAndGet();


System.out.println("Proceed " + request.getUrl()+" Response code " + response.getResponseCode());

if(!request.getMethod().toLowerCase().equals('post')) {
	request.setName(request.getName());
}

// check if request is post and countains desired patterms
if(request.getMethod().toLowerCase().equals('post')){
	def raw = request.getPropertyAsString('HTTPsampler.Arguments');
	def rraw = java.util.regex.Pattern.compile('(ru\\.naumen\\..*?)[/|\\s]');
	def ract = java.util.regex.Pattern.compile('.*\\.(.*Action)');

	def mraw = rraw.matcher(raw);

	def actions = "";

	while(mraw.find()){
		def item = mraw.group(1);
		def mact = ract.matcher(item);
		if(mact.find()) {
			item = mact.group(1);
			actions += (item + " ");
		} else {
			System.err.println('@@@@@@@@@@@@@@ '+item+' regex '+ract.toString());
		}
	}
	actions = actions.trim();
	if(actions.length()>0) {
		request.setName("" + counter + '. ' + actions);
	}

	// request processing
	def body = request.getPropertyAsString('HTTPsampler.Arguments');

	// comet can't be used at the moment
	if(body.contains('WaitCometEventsAction')){
		return false;
	}

	if((isItEditAction(body) || isItDeleteAction(body)))
	{
		def template = '';

		if(isItEditAction(body)){
			template = getEditObjectActionResult(body)
		} else {
			template = getDeleteObjectActionResult(body);
		}

		if(ctx.getTemplate(src) instanceof String) {
			def tt = new String(template);
			tt = 'clone.'+tt;
			if(!ctx.getTemplate(src).equals(tt)) {
				System.err.println('^^^^^^^^^^^^^^^^^^^^^^^^^ 2. ' + src + ' - ' + tt);
				ctx.addTemplate(src, tt);
			}
		}
		return;
	}

	String pattern = '(\\w+)\\$(\\d+)';
	def r = java.util.regex.Pattern.compile(pattern);
	def m = r.matcher(body);

	while (m.find())
	{
		def template = m.group(1) + '$' + m.group(2);
		def src = m.group(1) + "." + m.group(2);

		if(ctx.getTemplate(src)==null){
			// if request has a link to an object. jmeter recorder should add an user-defined-variable with partOne.partTwo equals to partOne$partTwo
			def tt = new String(template);

			System.err.println('**************************** 1. '+src+' - '+tt);
			ctx.addTemplate(src, tt);
		}
		if((body.contains('EditObjectAction') || body.contains('DeleteObjectAction')) && (ctx.getTemplate(src) instanceof String)){
			def tt = new String(template);
			tt = 'clone.'+tt;
			if(!ctx.getTemplate(src).equals(tt)) {
				System.err.println('**************************** 2. ' + src + ' - ' + tt);
				ctx.addTemplate(src, tt);
			}
		}
		body = body.replace(template, '${'+src+'}');
		m = r.matcher(body);
	}
	if(request.getArguments().getArgument(0)!=null){
		request.getArguments().getArgument(0).setValue(body);
	}
	System.out.println('-----------------request-'+body);

	// response processing
	// probably it might be better to parse response only in some conditions
	def rr = java.util.regex.Pattern.compile('AddObjectAction\\/\\d+\\|.*Fqn\\/\\d+\\|(\\w+)(\\$\\w+)?\\|');
	def mm = rr.matcher(body);

	def res = new String(response.getResponseData(), "UTF-8");

	if(mm.find()) {
		r = java.util.regex.Pattern.compile('('+mm.group(1)+')\\$(\\d+)');
		System.err.println('!!!!!!!!!!!!!!!!!!!!!!!!! AddObjectAction');
		System.err.println('!!!!!!!!!!!!!!!!!!!!!!!!! regex:'+r.toString());
		System.err.println('!!!!!!!!!!!!!!!!!!!!!!!!! res:'+res);
		m = r.matcher(res);

		if (m.find())
		{
			def template = m.group(1) + '$' + m.group(2);
			def src = m.group(1) + "." + m.group(2);
			if(ctx.getTemplate(src)==null){
				// if response has a link to an object jmeter recorder should add an user-defined-variable with partOne.partTwo equals current request
				ctx.addTemplate(src, request);
				System.err.println('!!!!!!!!!!!!!!!!!!!!!!!!! register ree '+src+' - '+request.getName());
			} else {
				System.err.println('=============================== Already has '+src+' variable!!');
			}
			res = res.replace(template, '${'+src+'}');
		}
	}
}
return true;