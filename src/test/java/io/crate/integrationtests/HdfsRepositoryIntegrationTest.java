/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.integrationtests;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakLingering;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import io.crate.action.sql.SQLBulkRequest;
import io.crate.action.sql.SQLBulkResponse;
import io.crate.action.sql.SQLRequest;
import io.crate.action.sql.SQLResponse;
import io.crate.client.CrateClient;
import io.crate.testing.CrateTestServer;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.junit.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.core.Is.is;

@ThreadLeakLingering(linger = 5000) // 5 sec lingering
@ThreadLeakScope(ThreadLeakScope.Scope.SUITE)
public class HdfsRepositoryIntegrationTest extends RandomizedTest {

    public static final ESLogger LOGGER = Loggers.getLogger(HdfsRepositoryIntegrationTest.class);

    private static final String CLUSTER_NAME = "hdfs-test";
    private static CrateClient crateClient;
    private String path;

    @ClassRule
    public static final CrateTestServer testServer = new CrateTestServer(CLUSTER_NAME, "0.53.0");

    @BeforeClass
    public static void beforeClass() throws Exception {
        crateClient = new CrateClient(String.format(Locale.ENGLISH, "%s:%d",
                testServer.crateHost,
                testServer.transportPort
        ));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        crateClient.close();
        crateClient = null;
    }

    @Before
    public final void before() throws Exception {
        path = "build/data/repo-" + randomInt();
    }

    @Test
    public void testSimpleWorkflow() {
        LOGGER.info("-->  creating hdfs repository with path [{}]", path);

        exec("create repository \"test-repo\" type hdfs with (uri = 'file://./', path = ?, chunk_size = ?, compress = ?)",
             $(path, randomIntBetween(10, 100), randomBoolean()));

        exec("create table t1 (id int primary key) with(number_of_replicas=0)");
        exec("create table t2 (id int primary key) with(number_of_replicas=0)");
        exec("create table t3 (id int primary key) with(number_of_replicas=0)");

        LOGGER.info("--> indexing some data");
        Object[][] bulkArgs = new Object[100][0];
        for (int i = 0; i < 100; i++) {
            bulkArgs[i] = $(i);
        }
        exec("insert into t1 (id) values (?)", bulkArgs);
        exec("insert into t2 (id) values (?)", bulkArgs);
        exec("insert into t3 (id) values (?)", bulkArgs);
        exec("refresh table t1, t2, t3");


        LOGGER.info("--> snapshot");
        exec("create snapshot \"test-repo\".\"test-snap\" TABLE t1, t2 with(wait_for_completion=true)");
        SQLResponse response = exec("SELECT state FROM sys.snapshots where name = 'test-snap'");
        assertThat((String)response.rows()[0][0], is("SUCCESS"));

        LOGGER.info("--> delete some data");
        for (int i = 0; i < 100; i += 2) {
            exec("delete from t3 where id = ?", $(i));
        }
        exec("refresh table t3");

        LOGGER.info("--> drop tables");
        exec("drop table t1");
        exec("drop table t2");

        LOGGER.info("--> restore all indices from the snapshot");
        exec("RESTORE SNAPSHOT \"test-repo\".\"test-snap\" ALL with (wait_for_completion = true)");

        assertThat(((long) exec("select count(*) from t1").rows()[0][0]), is(100L));
        assertThat(((long) exec("select count(*) from t2").rows()[0][0]), is(100L));
        assertThat(((long) exec("select count(*) from t3").rows()[0][0]), is(50L)); // not restored because still existed
    }

    private SQLResponse exec(String statement) {
        return exec(statement, new Object[0]);
    }
    private SQLResponse exec(String statement, Object[] args) {
        return crateClient.sql(new SQLRequest(statement, args)).actionGet(10, TimeUnit.SECONDS);
    }
    private SQLBulkResponse exec(String statement, Object[][] bulkArgs) {
        return crateClient.bulkSql(new SQLBulkRequest(statement, bulkArgs)).actionGet(10, TimeUnit.SECONDS);
    }
}
