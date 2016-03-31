if(!request.getUrl().toString().contains('192.168.225.142'))
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