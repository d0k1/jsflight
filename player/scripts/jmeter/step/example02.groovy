if(!request.getUrl().toString().contains('sd40.naumen.ru'))
{
	System.out.println("Skipped " + request.getUrl()+" Response code " + response.getResponseCode());
	return false;
}

System.out.println("Proceed " + request.getUrl()+" Response code " + response.getResponseCode());

// check if request is post and countains desired patterms
if(request.getMethod().toLowerCase().equals('post')){
	// requestt processing
	def body = request.getPropertyAsString('HTTPsampler.Arguments');
	
	String pattern = '(\\w+)\\$(\\d+)';
	def r = java.util.regex.Pattern.compile(pattern);
	def m = r.matcher(body);
	
	while (m.find())
	{
		def template = m.group(1) + '$' + m.group(2);
		def src = m.group(1) + "." + m.group(2);
		if(ctx.getTemplate(src)==null){
			// if request has a link to an object. jmeter recorder should add an user-defined-variable with partOne.partTwo equals to partOne$partTwo
	 		ctx.addTemplate(src, template);
		}
		body = body.replace(template, '${'+src+'}');
		m = r.matcher(body);
	}
	request.getArguments().getArgument(0).setValue(body);
	System.out.println('-----------------request-'+body);

	// response processing
	// probably it might be better to parse response only in some conditions
	def res = new String(response.getResponseData(), "UTF-8");
	if(res.contains('AddObjectAction')) {
		m = r.matcher(res);
		
		while (m.find())
		{
			def template = m.group(1) + '$' + m.group(2);
			def src = m.group(1) + "." + m.group(2);
			if(ctx.getTemplate(src)==null){
				// if response has a link to an object jmeter recorder should add an user-defined-variable with partOne.partTwo equals current request
		 		ctx.addTemplate(src, request);
			}
			res = res.replace(template, '${'+src+'}');
			m = r.matcher(body);
		}
	}	
}
return true;