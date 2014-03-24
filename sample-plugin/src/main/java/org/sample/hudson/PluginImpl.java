package org.sample.hudson;

import hudson.Plugin;
import hudson.scm.SCMS;

public class PluginImpl extends Plugin {

    public static final HelloWorldBuilder.DescriptorImpl TFS_DESCRIPTOR = new HelloWorldBuilder.DescriptorImpl();

    /**
     * Registers SCMDescriptors with Hudson.
     */
    @Override
    public void start() throws Exception {
        SCMS.SCMS.add(TFS_DESCRIPTOR);
        super.start();
    }
}
