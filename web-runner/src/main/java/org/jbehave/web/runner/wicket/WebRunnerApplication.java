package org.jbehave.web.runner.wicket;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.protocol.http.WebApplication;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryRunner;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.rest.RESTClient.Type;
import org.jbehave.core.io.rest.ResourceIndexer;
import org.jbehave.core.io.rest.redmine.IndexFromRedmine;
import org.jbehave.core.io.rest.redmine.LoadFromRedmine;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.Steps;
import org.jbehave.web.io.ArchivingFileManager;
import org.jbehave.web.io.FileManager;
import org.jbehave.web.io.FileMonitor;
import org.jbehave.web.io.SilentFileMonitor;
import org.jbehave.web.io.ZipFileArchiver;
import org.jbehave.web.runner.wicket.pages.DataFiles;
import org.jbehave.web.runner.wicket.pages.FindSteps;
import org.jbehave.web.runner.wicket.pages.Home;
import org.jbehave.web.runner.wicket.pages.RunStory;
import org.jbehave.web.runner.wicket.pages.SubmitStory;
import org.jbehave.web.runner.wicket.pages.ViewStory;
import org.jbehave.web.runner.wicket.pages.WikiTree;

import com.google.inject.AbstractModule;
import com.google.inject.Module;

public class WebRunnerApplication extends WebApplication {

    @Override
    protected void init() {
        super.init();
        getComponentInstantiationListeners().add(new GuiceComponentInjector(this, modules()));
        mountPage("/home", Home.class);
        mountPage("/data/files", DataFiles.class);
        mountPage("/steps/find", FindSteps.class);
        mountPage("/story/run", RunStory.class);
        mountPage("/story/submit", SubmitStory.class);
        mountPage("/story/view", ViewStory.class);
        mountPage("/wiki/tree", WikiTree.class);
    }

    private Module[] modules() {
        return new Module[] { new ApplicationModule() };
    }

    protected class ApplicationModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Embedder.class).toInstance(embedder());
            bind(FileManager.class).toInstance(fileManager());
            bind(ResourceIndexer.class).toInstance(resourceIndexer());
            bind(ResourceLoader.class).toInstance(resourceLoader());
            bind(WikiConfigurer.class).toInstance(wikiConfiguration());
        }

    }

    protected Embedder embedder() {
        Embedder embedder = new Embedder();
        embedder.useConfiguration(configuration());
        embedder.useStepsFactory(stepsFactory());
        return embedder;
    }


	protected WikiConfiguration wikiConfiguration() {
		return new WikiConfiguration("http://demo.redmine.org/projects/jbehave-rest/wiki");
	}

	protected ResourceIndexer resourceIndexer() {
		WikiConfiguration configuration = wikiConfiguration();
		return new IndexFromRedmine(configuration.getUsername(), configuration.getPassword());
	}

    protected ResourceLoader resourceLoader() {
		WikiConfiguration configuration = wikiConfiguration();
		return new LoadFromRedmine(Type.JSON, configuration.getUsername(), configuration.getPassword());
	}
    
	protected StoryRunner storyRunner() {
        return embedder().storyRunner();
    }

    protected Configuration configuration() {
        return new MostUsefulConfiguration();
    }

    protected InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), candidateSteps().toArray());
    }

    protected List<CandidateSteps> candidateSteps() {
        return Arrays.<CandidateSteps> asList(new Steps());
    }

    protected FileMonitor fileMonitor() {
        return new SilentFileMonitor();
    }

    protected File uploadDirectory() {
        return new File("/tmp", "upload");
    }

    protected ZipFileArchiver fileArchiver() {
        return new ZipFileArchiver();
    }

    protected FileManager fileManager() {
        return new ArchivingFileManager(fileArchiver(), fileMonitor(), uploadDirectory());
    }

    public Class<Home> getHomePage() {
        return Home.class;
    }
}
