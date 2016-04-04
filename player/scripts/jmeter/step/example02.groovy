import com.google.gwt.user.server.rpc.RPC;

def isItAddAction(String body) {
	return body.contains('AddObjectAction');
}

def isItEditAction(String body) {
	return body.contains('EditObjectAction') || body.contains('EditObjectsAction');
}

def isItDeleteAction(String body) {
	return body.contains('DeleteObjectAction') || body.contains('DeleteObjectsAction');
}

def processBatch(def batch, def clazz){
	batch.getActions().each({
		if(it.getClass().getName().contains(clazz)){
			return it;
		}
	})

	return null;
}

def getAddObjectActionResult(String response){
	def rpcRequest = RPC.decodeRequest(response);
	def test = rpcRequest.rpcRequest.getParameters()[0];
	if(test.getClass().getName().contains("BatchAction")){
		test = processBatch(test, "AddObjectAction");
	}

	if(test.getClass().getName().contains("AddObjectAction")){

	}
}

def getEditObjectActionResult(String request){
	def result = [];

	def rpcRequest = com.google.gwt.user.server.rpc.RPC.decodeRequest(request);

	def test = rpcRequest.getParameters()[0];
	if(test.getClass().getName().contains("BatchAction")){
		test = processBatch(test, "EditObjectAction");
		if(test==null) {
			test = processBatch(test, "EditObjectsAction");
		}
	}

	if(test.getClass().getName().contains("EditObjectAction")){
		result << test.getUuid();
	}

	if(test.getClass().getName().contains("EditObjectsAction")){
		result.addAll(test.getUuids() as Set);
	}

	return result;
}

def getDeleteObjectActionResult(String request){
	def result = [];

	def rpcRequest = com.google.gwt.user.server.rpc.RPC.decodeRequest(request);
	def test = rpcRequest.getParameters()[0];
	if(test.getClass().getName().contains("BatchAction")){
		test = processBatch(test, "DeleteObjectAction");
		if(test==null){
			test = processBatch(test, "DeleteObjectsAction");
		}
	}

	if(test.getClass().getName().contains("DeleteObjectAction")){
		result << test.getUUID();
	}

	if(test.getClass().getName().contains("DeleteObjectsAction")){
		result.addAll(test.getUuids() as Set);
	}

	return result;
}

java.lang.Thread.currentThread().setContextClassLoader(classloader);

baseurl = System.getProperty("baseurl");

if( baseurl!=null && baseurl.trim().length()>0 && !request.getUrl().toString().contains(baseurl))
{
	System.out.println("Skipped " + request.getUrl()+" Response code " + response.getResponseCode());
	return false;
}

if( request.getUrl().toString().contains('/remote_logging')  || request.getUrl().toString().contains('/comet'))
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
		def templates = [];
		if(isItEditAction(body)){
			templates.addAll(getEditObjectActionResult(body))
		} else {
			templates.addAll(getDeleteObjectActionResult(body));
		}

		templates.each({ template->
			def src = template.replace('$', '.');

			if(ctx.getTemplate(src) instanceof String || ctx.getTemplate(src)==null) {
				def tt = new String(template);
				tt = 'clone.'+tt;
				if(ctx.getTemplate(src)==null||!ctx.getTemplate(src).equals(tt)) {
					System.err.println('^^^^^^^^^^^^^^^^^^^^^^^^^ 2. ' + src + ' - ' + tt);
					ctx.addTemplate(src, tt);
				}
			}
		})
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

		body = body.replace(template, '${'+src+'}');
		m = r.matcher(body);
	}
	def arg = request.getArguments().getArgument(0);
	if(arg!=null){
		arg.setValue(body);
	}

	def res = new String(response.getResponseData(), "UTF-8");
	System.out.println('-----------------request-'+body);
	System.err.println('-----------------response:'+res);

	// response processing
	// probably it might be better to parse response only in some conditions
	def rr = java.util.regex.Pattern.compile('AddObjectAction\\/\\d+\\|.*Fqn\\/\\d+\\|(\\w+)(\\$\\w+)?\\|');
	def mm = rr.matcher(body);

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