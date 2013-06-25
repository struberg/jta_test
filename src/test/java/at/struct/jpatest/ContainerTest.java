package at.struct.jpatest;

import java.util.Properties;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Qualifier;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;


/**
 * A base class which shows simply starts a CDI unit test
 * utilizing Apache DeltaSpike CdiControl.
 *
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public abstract class ContainerTest {

    protected static CdiContainer cdiContainer;
    // nice to know, since testng executes tests in parallel.
    protected static int containerRefCount = 0;

    protected ProjectStage runInProjectStage() {
        return ProjectStage.UnitTest;
    }

    /**
     * Starts container
     * @throws Exception in case of severe problem
     */
    @BeforeMethod
    public final void setUp() throws Exception {
        containerRefCount++;

        if (cdiContainer == null) {
            ProjectStageProducer.setProjectStage(runInProjectStage());

            cdiContainer = CdiContainerLoader.getCdiContainer();

            Properties dbProperties = new Properties();
            dbProperties.put("MYDS", "new://Resource?type=DataSource");
            dbProperties.put("MYDS.JdbcDriver", "org.h2.Driver");
            dbProperties.put("MYDS.JdbcUrl", "jdbc:h2:mem:testdb");
            dbProperties.put("MYDS.JtaManaged", "true");
            dbProperties.put("MYDS.UserName", "sa");
            dbProperties.put("MYDS.Password", "");
                    
            cdiContainer.boot(dbProperties);

            cdiContainer.getContextControl().startContexts();
        }
        else {
            cleanInstances();
        }
    }


    public static CdiContainer getCdiContainer() {
        return cdiContainer;
    }

    @BeforeClass
    public final void beforeClass() throws Exception {
        setUp();
        cleanInstances();

        // perform injection into the very own test class
        BeanManager beanManager = cdiContainer.getBeanManager();

        CreationalContext creationalContext = beanManager.createCreationalContext(null);

        AnnotatedType annotatedType = beanManager.createAnnotatedType(this.getClass());
        InjectionTarget injectionTarget = beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(this, creationalContext);
    }

    /**
     * Shuts down container.
     * @throws Exception in case of severe problem
     */
    @AfterMethod
    public final void tearDown() throws Exception {
        if (cdiContainer != null) {
            cleanInstances();
            containerRefCount--;
        }
    }

    public final void cleanInstances() throws Exception {
        cdiContainer.getContextControl().stopContext(RequestScoped.class);
        cdiContainer.getContextControl().startContext(RequestScoped.class);
        cdiContainer.getContextControl().stopContext(SessionScoped.class);
        cdiContainer.getContextControl().startContext(SessionScoped.class);
    }

    @AfterSuite
    public synchronized void shutdownContainer() throws Exception {
        if (cdiContainer != null) {
            cdiContainer.shutdown();
            cdiContainer = null;
        }
    }

    public void finalize() throws Throwable {
        shutdownContainer();
        super.finalize();
    }


    /**
     * Override this method for database clean up.
     *
     * @throws Exception in case of severe problem
     */
    protected void cleanUpDb() throws Exception {
        //Override in subclasses when needed
    }

    protected <T> T getInstance(Class<T> type, Qualifier... qualifiers) {
        Set<Bean<?>> beans = cdiContainer.getBeanManager().getBeans(type, qualifiers);
        Bean<T> bean = (Bean<T>) cdiContainer.getBeanManager().resolve(beans);
        Assert.assertNotNull(bean, "Bean must not be null for type " + type.getName());

        CreationalContext<T> cc = cdiContainer.getBeanManager().createCreationalContext(bean);
        return (T) cdiContainer.getBeanManager().getReference(bean, type, cc);
    }

}
