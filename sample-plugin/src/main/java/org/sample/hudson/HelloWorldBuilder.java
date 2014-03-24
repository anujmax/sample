package org.sample.hudson;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.PollingResult;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.scm.SCM;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.Builder;
import hudson.util.Digester2;
import hudson.util.FormValidation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.digester.Digester;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.ldap.filter.CompareFilter;
import org.xml.sax.SAXException;

import com.sun.xml.bind.v2.schemagen.xmlschema.List;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImplnewInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link perform(AbstractBuild, Launcher, BuildListener)} method
 * will be invoked. 
 *
 */
public class HelloWorldBuilder extends SCM  {

	    private boolean cleanCopy;
	    private String server;
	    private String project;
	    private String workspaceName;
	    private String username;
		private String password;
	    private String domain;
	    
	    public String getProject() {
			return project;
		}

		public String getWorkspaceName() {
			return workspaceName;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		public String getDomain() {
			return domain;
		}

	    public String getServer() {
	        return server;
	    }

	    public boolean isCleanCopy() {
	        return cleanCopy;
	    }
	    
	    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	    @DataBoundConstructor
	    public HelloWorldBuilder(String server, String project, boolean cleanCopy,
	              String username, String password, String domain, String workspaceName) {
	        // Copying arguments to fields
	    	this.server = server;
	    	this.project = project;
	    	this.cleanCopy = cleanCopy;
	    	this.domain  = domain;
	    	this.workspaceName = workspaceName;
	    }
	    
	    @Override
	    public SCMDescriptor<HelloWorldBuilder> getDescriptor() {
	        return PluginImpl.TFS_DESCRIPTOR;
	    }

   

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // this marker indicates Hudson that this is an implementation of an extension point.
    public static class DescriptorImpl extends SCMDescriptor {
        private String tfExecutable;

        public DescriptorImpl() {
            super(HelloWorldBuilder.class, null);
            load();
        }

        @Override
        public String getDisplayName() {
            return "My SCM";
        }
        
        @Override
        public boolean configure(StaplerRequest req) throws FormException {
            tfExecutable = Util.fixEmpty(req.getParameter("tfs.tfExecutable").trim());
            save();
            return true;
        }
        
        public String getTfExecutable() {
            if (tfExecutable == null) {
                return "tfs";
            } else {
                return tfExecutable;
            }
        }
        public FormValidation doExecutableCheck(@QueryParameter String value) {
            return FormValidation.validateExecutable(value);
        }
    }



	@Override
	public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> arg0,
			Launcher arg1, TaskListener arg2) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public class CheckoutTask implements FileCallable<Boolean> {
	    public Boolean invoke(File workspace, VirtualChannel channel) throws IOException {
	        // This here code is executed on the slave.
	        // Call the library method to check out the files
	    	
	    	FileUtils.copyDirectoryToDirectory(new File(server+""+project),new File(workspaceName));
	    	
	    	PrintWriter writer = new PrintWriter(new FileWriter(server+"changelog.xml"));
	    	writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	    	writer.println("<changelog>");
	    	
	    	    writer.println(String.format("\t<changeset version=\"%s\">", "1"));
	    	    writer.println(String.format("\t\t<date>%s</date>", Calendar.DATE));
	    	    writer.println(String.format("\t\t<user>%s</user>", "Anuj"));
	    	    writer.println(String.format("\t\t<comment>%s</comment>", "file changed"));
	    	    writer.println("\t\t<items>");
	    	  
	    	    writer.println("\t\t</items>");
	    	    writer.println("\t</changeset>");
	    	
	    	writer.println("</changelog>");
	    	writer.close();
	        return true;
	    }
	}
	@Override
	public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, 
	        BuildListener listener, File changelogFile) throws IOException,
			InterruptedException {
		listener.getLogger().println("Checkout is started with properties,CleanCopy= "+cleanCopy+" ,Domain= "+domain+",password= "+password+""
				+ ",project= "+project+",server= "+server+" ,username= "+username+" ,workspaceName= "+workspaceName+" ");
		CheckoutTask task = new CheckoutTask();
	    return workspace.act(task); // The CheckoutTask.invoke() method is now invoked
		
	}

	@Override
	protected PollingResult compareRemoteRevisionWith(
			AbstractProject<?, ?> arg0, Launcher launcher, FilePath workspace,
			TaskListener listener, SCMRevisionState scrmState) throws IOException,
			InterruptedException {
		listener.getLogger().println("Started polling");
		listener.getLogger().println("Files on remote DIR");
		String remoteDir = server+""+project;
		File f1 = new File(remoteDir);
		Collection files = FileUtils.listFiles(
				  f1, 
				  new RegexFileFilter("^(.*?)"), 
				  DirectoryFileFilter.DIRECTORY
				);
		ArrayList<String> dirList = new ArrayList<String>();
		Iterator itr = files.iterator();
		while(itr.hasNext()) {
	         Object element = itr.next();
	         listener.getLogger().println(element + "");
	         File f = (File) element;
	         String fname = f.getAbsolutePath();
	         String relativePath = fname.substring(remoteDir.length());
	         dirList.add(relativePath);
	        
	      }	
		
		listener.getLogger().println("Files on local DIR");
		String localDir = workspaceName+""+project;
		File f2 = new File(localDir);
		Collection files1 = FileUtils.listFiles(
				  f2, 
				  new RegexFileFilter("^(.*?)"), 
				  DirectoryFileFilter.DIRECTORY
				);
		
		Iterator itr1 = files1.iterator();
		while(itr1.hasNext()) {
	         Object element = itr1.next();
	         listener.getLogger().print(element + " ");
	         File f = (File) element;
	         String fname = f.getAbsolutePath();
	         String relativePath = fname.substring(localDir.length());
	        if(!dirList.contains(relativePath)) {
		         listener.getLogger().println("---- Changed ");
	         }
	        else {
	        	  listener.getLogger().println("");
	        }
	      }
		
		/*if (f1.isDirectory()) {
			listener.getLogger().println("Directory of " + remoteDir);
		      String s[] = f1.list();
		   
		      for (int i=0; i < s.length; i++) {
		        File f = new File(remoteDir + "/" + s[i]);
		        if (f.isDirectory()) {
		        	listener.getLogger().println(s[i] + " is a directory");
		        } else {
		        	listener.getLogger().println(s[i] + " is a file");
		        }
		      }
		    } else {
		    	listener.getLogger().println(remoteDir + " is not a directory");
		    }*/
		
		
		
			return PollingResult.BUILD_NOW;
		
	}

	@Override
	public ChangeLogParser createChangeLogParser() {
		ArrayList<HelloWorldBuilder> changesetList = new ArrayList<HelloWorldBuilder>();
		Digester digester = new Digester2();
		digester.push(changesetList);

		// When digester reads a {{<changeset>}} node it will create a {{TeamFoundationChangeSet}} object
		digester.addObjectCreate("*/changeset", HelloWorldBuilder.class);
		// Reads all attributes in the {{<changeset>}} node and uses setter method in class to set the values
		digester.addSetProperties("*/changeset");
		// Reads the child node {{<comment>}} and uses {{TeamFoundationChangeSet.setComment()}} to set the value
		digester.addBeanPropertySetter("*/changeset/comment");
		digester.addBeanPropertySetter("*/changeset/user");
		// Reading the {{<date<}} child node will use the {{TeamFoundationChangeSet.setDateStr()}} method
		// instead of the default {{TeamFoundationChangeSet.setDate()}}
		digester.addBeanPropertySetter("*/changeset/date", "dateStr");
		// The digested node/change set is added to the list through {{List.add()}}
		digester.addSetNext("*/changeset", "add");


		// When digester reads a {{<items>}} child node of {{<changeset}} it will create a {{TeamFoundationChangeSet.Item}} object
		digester.addObjectCreate("*/changeset/items/item", HelloWorldBuilder.class);
		digester.addSetProperties("*/changeset/items/item");
		digester.addBeanPropertySetter("*/changeset/items/item", "path");
		// The digested node/item is added to the change set through {{TeamFoundationChangeSet.add()}}
		digester.addSetNext("*/changeset/items/item", "add");

		// Do the actual parsing
		FileReader reader;
		try {
			reader = new FileReader(server+"changelog.xml");
			digester.parse(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ChangeLogParser clp = new ChangeLogParser() {
			
			@Override
			public ChangeLogSet<? extends Entry> parse(AbstractBuild arg0, File arg1)
					throws IOException, SAXException {
				//ChangeLogSet<Entry> cls = new ChangeLogSet<Entry>();
				
				return null;
			}
		};
		return clp;
	}
}

