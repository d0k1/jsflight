boolean accesKeyFound = false;

def getRPCRequestClassName(String request){
    def rpcRequest = com.google.gwt.user.server.rpc.RPC.decodeRequest(request);
    def test = rpcRequest.getParameters()[0];

    def actions = [];

    if(test.getClass().getName().contains("BatchAction")){
        test.getActions().each({
            actions.add(it.getClass().getSimpleName());
        })
    } else if(test.getClass().getName().contains("Action")){
        actions.add(test.getClass().getSimpleName());
    }
    if(actions.size()==0)
        return null;

    return actions.join(" ").trim();
}

java.lang.Thread.currentThread().setContextClassLoader(classloader);

if(sample.getMethod().toLowerCase().equals('post')) {
    def raw = sample.getPropertyAsString('HTTPsampler.Arguments');

    if(raw!=null && raw.length()>0) {
        String name = getRPCRequestClassName(raw as String);
        if(name!=null) {
            String counter = sample.getName().split(" ")[0].trim();
            sample.setName("" + counter + " " + name);
            System.err.println(Thread.currentThread().getName()+":"+'Request ' + sample.getName() + ' renamed to ' + name + ' hash ' + System.identityHashCode(sample));
        } else {
            System.err.println(Thread.currentThread().getName()+":"+'Request ' + sample.getName() + ' is not gwt-prc ' + ' hash ' + System.identityHashCode(sample)+'.'+raw);
        }
    }
}



for(String key : sample.getArguments().getArgumentsAsMap().keySet()) {
    if(key.equalsIgnoreCase("accessKey")){
        accesKeyFound = true;
        break;
    }
}

if(!accesKeyFound) {
    if (com.focusit.jmeter.JMeterJSFlightBridge.getInstace().getSourceEvent((org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase)sample) != null)
    {
        def cookies = new org.apache.jmeter.protocol.http.control.CookieManager();
        cookies.setName("HTTP Cookie Manager");
        cookies.setEnabled(true);
        cookies.setProperty(org.apache.jmeter.testelement.TestElement.GUI_CLASS, "CookiePanel");
        cookies.setProperty(org.apache.jmeter.testelement.TestElement.TEST_CLASS, "CookieManager");
        tree.add(cookies);

        String cooks = "employee=" + com.focusit.jmeter.JMeterJSFlightBridge.getInstace()
                .getSourceEvent((org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase)sample).getString(com.focusit.jmeter.JMeterJSFlightBridge.TAG_FIELD);

        String pattern = 'employee=(\\w+)\\$(\\d+)';
        def r = java.util.regex.Pattern.compile(pattern);
        def m = r.matcher(cooks);
        if (m.find())
        {
            String name = "jsid_" + m.group(1) + "_" + m.group(2);
            cookies.add(new org.apache.jmeter.protocol.http.control.Cookie("JSESSIONID", '${' + name + '}',
                    sample.getPropertyAsString(org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.DOMAIN), "/", false, 0L));

            if (!vars.getArgumentsAsMap().containsKey(name))
            {
                vars.addArgument(new org.apache.jmeter.config.Argument(name, "empty_session"));
            }
        }
    }
    else
    {
        System.err.println("No tag found for sampler " + sample.getName());
    }
}

def vars_key = 'variables';

if(ctx.getProperty(vars_key)==null){
    def items = new HashSet();
    items.addAll(ctx.getSources())
    ctx.addProperty(vars_key, items);
}

Set srcs = []
srcs.addAll(ctx.getProperty(vars_key));

srcs.each({
    def template = ctx.getTemplate(it);

    System.out.println('................................. '+it+' : '+template.getClass().getName());

    // should add regex post processor here
    if(template instanceof String) {
        // add just an user defined variable
        vars.addArgument(new org.apache.jmeter.config.Argument(it, template));

        System.err.println('???????????? Added variable '+it+' for '+sample.getName());
        ctx.getProperty(vars_key).remove(it);
    } else if(template instanceof org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase){
        System.err.println('++++++++++++++ template '+template.getName()+' sample '+sample.getName());
        if(template.equals(sample)) {
            def ree = new org.apache.jmeter.extractor.RegexExtractor();
            ree.setProperty(org.apache.jmeter.testelement.TestElement.GUI_CLASS, "RegexExtractorGui");
            ree.setProperty(org.apache.jmeter.testelement.TestElement.TEST_CLASS, "RegexExtractor");
            ree.setEnabled(true);
            ree.setRefName(it);
            ree.setName('REE.' + it);

            String pattern = '(\\w+)\\.(\\d+)';
            def r = java.util.regex.Pattern.compile(pattern);
            def m = r.matcher(it);
            m.find();
            ree.setRegex(m.group(1) + '\\$' + '\\d+');
            ree.setTemplate('$0$');
            ree.setDefaultValue('ererrer-1212$233232');
            ree.setMatchNumber(0);
            ree.useHeaders();
            tree.add(ree);

            ctx.getProperty(vars_key).remove(it);

            System.err.println('???????????? Added regex extractor to ' + sample.getName())
        }
    } else {
            System.err.println('Source '+ it+' template '+template.getName()+' sample '+sample.getName());
    }
})

