/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.testsuite.test.configuration.batch;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.CrudOperations;
import org.jboss.hal.testsuite.Random;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.BatchPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX_THREADS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.testsuite.test.configuration.batch.BatchFixtures.*;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class ThreadPoolTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void beforeClass() throws Exception {
        operations.add(threadPoolAddress(THREAD_POOL_READ), Values.empty().and(MAX_THREADS, MAX_THREADS_VALUE));
        operations.add(threadPoolAddress(THREAD_POOL_UPDATE), Values.empty().and(MAX_THREADS, MAX_THREADS_VALUE));
        operations.add(threadPoolAddress(THREAD_POOL_DELETE), Values.empty().and(MAX_THREADS, MAX_THREADS_VALUE));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        operations.removeIfExists(threadPoolAddress(THREAD_POOL_CREATE));
        operations.removeIfExists(threadPoolAddress(THREAD_POOL_READ));
        operations.removeIfExists(threadPoolAddress(THREAD_POOL_UPDATE));
        operations.removeIfExists(threadPoolAddress(THREAD_POOL_DELETE));
    }

    @Inject private Console console;
    @Inject private CrudOperations crud;
    @Page private BatchPage page;
    private TableFragment table;
    private FormFragment form;

    @Before
    public void setUp() throws Exception {
        page.navigate();
        console.verticalNavigation().selectPrimary("batch-thread-pool-item");

        form = page.getThreadPoolForm();
        table = page.getThreadPoolTable();
        table.bind(form);
    }

    @Test
    public void create() throws Exception {
        crud.create(threadPoolAddress(THREAD_POOL_CREATE), table, form -> {
            form.text(NAME, THREAD_POOL_CREATE);
            form.number(MAX_THREADS, MAX_THREADS_VALUE);
        });
    }

    @Test
    public void createNoMaxThreads() {
        crud.createWithError(table, THREAD_POOL_CREATE, MAX_THREADS);
    }

    @Test
    public void createInvalidMaxThreads() {
        crud.createWithError(table, form -> {
            form.text(NAME, THREAD_POOL_CREATE);
            form.number(MAX_THREADS, -1);
        }, MAX_THREADS);
    }

    @Test
    public void read() {
        table.select(THREAD_POOL_READ);
        assertEquals(THREAD_POOL_READ, form.value(NAME));
        assertEquals(MAX_THREADS_VALUE, form.intValue(MAX_THREADS));
    }

    @Test
    public void update() throws Exception {
        int maxThreads = Random.number();

        table.select(THREAD_POOL_UPDATE);
        crud.update(threadPoolAddress(THREAD_POOL_UPDATE), form, MAX_THREADS, maxThreads);
    }

    @Test
    public void updateNoMaxThreads() {
        table.select(THREAD_POOL_UPDATE);
        crud.updateWithError(form, form -> form.clear(MAX_THREADS), MAX_THREADS);
    }

    @Test
    public void reset() throws Exception {
        table.select(THREAD_POOL_UPDATE);
        crud.reset(threadPoolAddress(THREAD_POOL_UPDATE), form);
    }

    @Test
    public void updateInvalidMaxThreads() {
        table.select(THREAD_POOL_UPDATE);
        crud.updateWithError(form, MAX_THREADS, -1);
    }

    @Test
    public void delete() throws Exception {
        crud.delete(threadPoolAddress(THREAD_POOL_DELETE), table, THREAD_POOL_DELETE);
    }
}
