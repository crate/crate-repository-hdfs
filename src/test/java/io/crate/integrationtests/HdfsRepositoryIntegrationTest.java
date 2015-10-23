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
import io.crate.action.sql.SQLRequest;
import io.crate.client.CrateClient;
import io.crate.testing.CrateTestServer;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryResponse;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.snapshots.SnapshotState;
import org.hamcrest.Matchers;
import org.junit.*;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class HdfsRepositoryIntegrationTest extends RandomizedTest {

    public static final ESLogger LOGGER = Loggers.getLogger(HdfsRepositoryIntegrationTest.class);

    private static final String CLUSTER_NAME = "hdfs-test";
    private static CrateClient crateClient;
    private static TransportClient transportClient;
    private String path;

    @ClassRule
    public static CrateTestServer testServer = new CrateTestServer(CLUSTER_NAME);

    @BeforeClass
    public static void beforeClass() throws Exception {
        crateClient = new CrateClient(String.format(Locale.ENGLISH, "%s:%d",
                testServer.crateHost,
                testServer.transportPort
        ));
        // ES transport client
        Settings settings = ImmutableSettings.builder()
                .put("plugins.load_classpath_plugins", false)
                .put("cluster.name", CLUSTER_NAME)
                .build();
        transportClient = new TransportClient(settings);
        transportClient.addTransportAddress(new InetSocketTransportAddress(testServer.crateHost, testServer.transportPort));
    }

    @AfterClass
    public static void afterClass() throws Exception {
        crateClient.close();
        crateClient = null;
        transportClient.close();
        transportClient = null;
    }

    @Before
    public final void before() throws Exception {
        path = "build/data/repo-" + randomInt();
    }

    @Test
    public void testSimpleWorkflowWithElasticSearchAPI() {
        LOGGER.info("-->  creating hdfs repository with path [{}]", path);

        PutRepositoryResponse putRepositoryResponse = transportClient.admin().cluster().preparePutRepository("test-repo")
                .setType("hdfs")
                .setSettings(ImmutableSettings.settingsBuilder()
                                .put("uri", "file://./")
                                .put("path", path)
                                .put("chunk_size", randomIntBetween(100, 1000))
                                .put("compress", randomBoolean())
                ).get();
        assertThat(putRepositoryResponse.isAcknowledged(), equalTo(true));

        crateClient.sql("create table test_idx_1 (id int primary key) with(number_of_replicas=0)").actionGet();
        crateClient.sql("create table test_idx_2 (id int primary key) with(number_of_replicas=0)").actionGet();
        crateClient.sql("create table test_idx_3 (id int primary key) with(number_of_replicas=0)").actionGet();

        ensureGreen();

        LOGGER.info("--> indexing some data");
        for (int i = 0; i < 100; i++) {
            crateClient.sql(new SQLRequest("insert into test_idx_1 (id) values (?)", new Object[]{i})).actionGet();
            crateClient.sql(new SQLRequest("insert into test_idx_2 (id) values (?)", new Object[]{i})).actionGet();
            crateClient.sql(new SQLRequest("insert into test_idx_3 (id) values (?)", new Object[]{i})).actionGet();
        }
        refresh();
        assertThat(transportClient.prepareCount("test_idx_1").get().getCount(), equalTo(100L));
        assertThat(transportClient.prepareCount("test_idx_2").get().getCount(), equalTo(100L));
        assertThat(transportClient.prepareCount("test_idx_3").get().getCount(), equalTo(100L));

        LOGGER.info("--> snapshot");
        CreateSnapshotResponse createSnapshotResponse = transportClient.admin().cluster().prepareCreateSnapshot("test-repo", "test-snap").setWaitForCompletion(true).setIndices("test_idx_*", "-test_idx_3").get();
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), greaterThan(0));
        assertThat(createSnapshotResponse.getSnapshotInfo().successfulShards(), equalTo(createSnapshotResponse.getSnapshotInfo().totalShards()));

        assertThat(transportClient.admin().cluster().prepareGetSnapshots("test-repo").setSnapshots("test-snap").get().getSnapshots().get(0).state(), equalTo(SnapshotState.SUCCESS));

        LOGGER.info("--> delete some data");
        for (int i = 0; i < 50; i++) {
            crateClient.sql(new SQLRequest("delete from test_idx_1 where id = ?", new Object[]{i})).actionGet();
        }
        for (int i = 50; i < 100; i++) {
            crateClient.sql(new SQLRequest("delete from test_idx_2 where id = ?", new Object[]{i})).actionGet();
        }
        for (int i = 0; i < 100; i += 2) {
            crateClient.sql(new SQLRequest("delete from test_idx_3 where id = ?", new Object[]{i})).actionGet();
        }
        refresh();
        assertThat(transportClient.prepareCount("test_idx_1").get().getCount(), equalTo(50L));
        assertThat(transportClient.prepareCount("test_idx_2").get().getCount(), equalTo(50L));
        assertThat(transportClient.prepareCount("test_idx_3").get().getCount(), equalTo(50L));

        LOGGER.info("--> close indices");
        transportClient.admin().indices().prepareClose("test_idx_1", "test_idx_2").get();

        LOGGER.info("--> restore all indices from the snapshot");
        RestoreSnapshotResponse restoreSnapshotResponse = transportClient.admin().cluster().prepareRestoreSnapshot("test-repo", "test-snap").setWaitForCompletion(true).execute().actionGet();
        assertThat(restoreSnapshotResponse.getRestoreInfo().totalShards(), greaterThan(0));

        ensureGreen();
        assertThat(transportClient.prepareCount("test_idx_1").get().getCount(), equalTo(100L));
        assertThat(transportClient.prepareCount("test_idx_2").get().getCount(), equalTo(100L));
        assertThat(transportClient.prepareCount("test_idx_3").get().getCount(), equalTo(50L));

        // Test restore after index deletion
        LOGGER.info("--> delete indices");
        crateClient.sql("drop table test_idx_1").actionGet();
        crateClient.sql("drop table test_idx_2").actionGet();
        crateClient.sql("drop table test_idx_3").actionGet();
        LOGGER.info("--> restore one index after deletion");
        restoreSnapshotResponse = transportClient.admin().cluster().prepareRestoreSnapshot("test-repo", "test-snap").setWaitForCompletion(true).setIndices("test_idx_*", "-test_idx_2").execute().actionGet();
        assertThat(restoreSnapshotResponse.getRestoreInfo().totalShards(), greaterThan(0));
        ensureGreen();
        assertThat(transportClient.prepareCount("test_idx_1").get().getCount(), equalTo(100L));
        ClusterState clusterState = transportClient.admin().cluster().prepareState().get().getState();
        assertThat(clusterState.getMetaData().hasIndex("test_idx_1"), equalTo(true));
        assertThat(clusterState.getMetaData().hasIndex("test_idx_2"), equalTo(false));
    }

    private void ensureGreen() {
        ClusterHealthResponse actionGet = transportClient.admin().cluster()
                .health(Requests.clusterHealthRequest().timeout(TimeValue.timeValueSeconds(30))
                        .waitForGreenStatus().waitForEvents(Priority.LANGUID).waitForRelocatingShards(0)).actionGet();
        if (actionGet.isTimedOut()) {
            LOGGER.info("ensureGreen timed out, cluster state:\n{}\n{}", transportClient.admin().cluster().prepareState().get().getState().prettyPrint(), transportClient.admin().cluster().preparePendingClusterTasks().get().prettyPrint());
            fail("timed out waiting for green state");
        }
        assertThat(actionGet.getStatus(), Matchers.equalTo(ClusterHealthStatus.GREEN));
    }

    private void refresh() {
        transportClient.admin().indices().prepareRefresh().execute().actionGet();
    }
}
