boolean accesKeyFound = false;

for(String key : sample.getArguments().getArgumentsAsMap().keySet()) {
    if(key.equalsIgnoreCase("accessKey")){
        accesKeyFound = true;
        break;
    }
}

if(!accesKeyFound) {
    def cookies = new org.apache.jmeter.protocol.http.control.CookieManager();
    cookies.setName("HTTP Cookie Manager");
    cookies.setEnabled(true);
    cookies.setProperty(org.apache.jmeter.testelement.TestElement.GUI_CLASS, "CookiePanel");
    cookies.setProperty(org.apache.jmeter.testelement.TestElement.TEST_CLASS, "CookieManager");
    tree.add(cookies);

    // TODO groovy script should decide what cookies might be added to manager
    if (com.focusit.jmeter.JMeterJSFlightBridge.getInstace().getSourceEvent((org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase)sample) != null)
    {
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

    // should add regex post processor here
    if(template instanceof String) {
        // add just an user defined variable
        vars.addArgument(new org.apache.jmeter.config.Argument(it, template));

        ctx.getProperty(vars_key).remove(it);
    } else if(template.equals(sample)){
        def ree = new org.apache.jmeter.extractor.RegexExtractor();
        ree.setProperty(org.apache.jmeter.testelement.TestElement.GUI_CLASS, "RegexExtractorGui");
        ree.setProperty(org.apache.jmeter.testelement.TestElement.TEST_CLASS, "RegexExtractor");
        ree.setEnabled(true);
        ree.setRefName(it);
        ree.setName('REE.'+it);

        String pattern = '(\\w+)\\.(\\d+)';
        def r = java.util.regex.Pattern.compile(pattern);
        def m = r.matcher(it);
        m.find();
        ree.setRegex(m.group(1)+'\\$'+'\\d+');
        ree.setTemplate('$0$');
        ree.setDefaultValue('ererrer-1212$233232');
        ree.setMatchNumber(0);
        ree.useHeaders();
        tree.add(ree);

        ctx.getProperty(vars_key).remove(it);
    } else {
        System.err.println('Source '+ it+' template '+template.getName()+' sample '+sample.getName());
    }
})

