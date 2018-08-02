package org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.scattered.cache.store.file;

import java.io.IOException;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.page.configuration.ScatteredCachePage;
import org.jboss.hal.testsuite.test.configuration.infinispan.InfinispanFixtures;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.testsuite.test.configuration.infinispan.InfinispanFixtures.cacheContainerAddress;
import static org.jboss.hal.testsuite.test.configuration.infinispan.InfinispanFixtures.fileStoreAddress;
import static org.jboss.hal.testsuite.test.configuration.infinispan.InfinispanFixtures.scatteredCacheAddress;

@RunWith(Arquillian.class)
public class WriteBehaviourTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    private static final String CACHE_CONTAINER = "cache-container-" + Random.name();
    private static final String SCATTERED_CACHE_WRITE_BEHIND_EDIT =
        "scattered-cache-with-write-behind-to-be-edited-" + Random.name();
    private static final String SCATTERED_CACHE_WRITE_THROUGH = "scattered-cache-write-through-" + Random.name();
    private static final String SCATTERED_CACHE_WRITE_BEHIND = "scattered-cache-write-behind-" + Random.name();

    @BeforeClass
    public static void init() throws IOException {
        operations.add(cacheContainerAddress(CACHE_CONTAINER));
        operations.add(cacheContainerAddress(CACHE_CONTAINER).and("transport", "jgroups"));
        operations.add(scatteredCacheAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND));
        operations.add(scatteredCacheAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND_EDIT));
        operations.add(scatteredCacheAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_THROUGH));
        operations.headers(Values.of("allow-resource-service-restart", true))
            .add(fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND));
        operations.headers(Values.of("allow-resource-service-restart", true))
            .add(fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_THROUGH));
        operations.headers(Values.of("allow-resource-service-restart", true))
            .add(fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND_EDIT));
        operations.add(fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND).and(InfinispanFixtures.WRITE,
            InfinispanFixtures.BEHIND));
        operations.add(fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND_EDIT).and(InfinispanFixtures.WRITE,
            InfinispanFixtures.BEHIND));
    }

    @AfterClass
    public static void tearDown() throws IOException, OperationException {
        try {
            operations.removeIfExists(cacheContainerAddress(CACHE_CONTAINER));
        } finally {
            client.close();
        }
    }

    @Drone
    private WebDriver browser;

    @Inject
    private Console console;

    @Inject
    private CrudOperations crudOperations;

    @Page
    private ScatteredCachePage page;

    private void navigateToWriteBehaviour(String cacheContainer, String scatteredCache) {
        page.navigate(cacheContainer, scatteredCache);
        console.verticalNavigation().selectPrimary("scattered-cache-store-item");
    }

    @Test
    public void switchToWriteBehind() throws Exception {
        navigateToWriteBehaviour(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_THROUGH);
        page.getFileStoreTab().select(ScatteredCachePage.FILE_STORE_WRITE_BEHAVIOUR_TAB);
        page.switchBehaviour();
        new ResourceVerifier(
            fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_THROUGH).and(InfinispanFixtures.WRITE,
                InfinispanFixtures.BEHIND),
            client).verifyExists();
    }

    @Test
    public void switchToWriteThrough() throws Exception {
        navigateToWriteBehaviour(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND);
        page.getFileStoreTab().select(ScatteredCachePage.FILE_STORE_WRITE_BEHAVIOUR_TAB);
        page.switchBehaviour();
        new ResourceVerifier(
            fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND).and(InfinispanFixtures.WRITE, InfinispanFixtures.THROUGH),
            client).verifyExists();
    }

    @Test
    public void editModificationQueueSize() throws Exception {
        navigateToWriteBehaviour(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND_EDIT);
        crudOperations.update(
            fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND_EDIT).and(InfinispanFixtures.WRITE,
                InfinispanFixtures.BEHIND),
            page.getFileStoreWriteBehindForm(), "modification-queue-size", Random.number());
    }

    @Test
    public void editThreadPoolSize() throws Exception {
        navigateToWriteBehaviour(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND_EDIT);
        crudOperations.update(
            fileStoreAddress(CACHE_CONTAINER, SCATTERED_CACHE_WRITE_BEHIND_EDIT).and(InfinispanFixtures.WRITE,
                InfinispanFixtures.BEHIND),
            page.getFileStoreWriteBehindForm(), "thread-pool-size", Random.number());
    }
}
