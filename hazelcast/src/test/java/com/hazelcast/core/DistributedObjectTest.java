/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.core;

import com.hazelcast.config.Config;
import com.hazelcast.config.ServiceConfig;
import com.hazelcast.instance.Node;
import com.hazelcast.instance.TestUtil;
import com.hazelcast.spi.InitializingObject;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.RemoteService;
import com.hazelcast.spi.impl.ProxyServiceImpl;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.TestHazelcastInstanceFactory;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author mdogan 8/25/13
 */
@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class DistributedObjectTest extends HazelcastTestSupport {

    @Test
    public void testMap() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getMap("test");
        test(instance, object);
    }

    @Test
    public void testQueue() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getQueue("test");
        test(instance, object);
    }

    @Test
    public void testTopic() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getTopic("test");
        test(instance, object);
    }

    @Test
    public void testMultiMap() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getMultiMap("test");
        test(instance, object);
    }

    @Test
    public void testSet() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getSet("test");
        test(instance, object);
    }

    @Test
    public void testList() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getList("test");
        test(instance, object);
    }

    @Test
    public void testExecutorService() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getExecutorService("test");
        test(instance, object);
    }

    @Test
    public void testLock() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getLock("test");
        test(instance, object);
    }

    @Test
    public void testLock2() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getLock(System.currentTimeMillis());
        test(instance, object);
    }

    @Test
    public void testAtomicLong() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getAtomicLong("test");
        test(instance, object);
    }

    @Test
    public void testSemaphore() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getSemaphore("test");
        test(instance, object);
    }

    @Test
    public void testCountdownLatch() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getCountDownLatch("test");
        test(instance, object);
    }

    @Test
    public void testIdGenerator() {
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance();
        DistributedObject object = instance.getIdGenerator("test");
        test(instance, object);
    }

    private void test(HazelcastInstance instance, DistributedObject object) {
        DistributedObject object2 = instance.getDistributedObject(object.getServiceName(), object.getName());
        Assert.assertEquals(object.getServiceName(), object2.getServiceName());
        Assert.assertEquals(object.getName(), object2.getName());
        Assert.assertEquals(object.getId(), object2.getId());
        Assert.assertEquals(object, object2);
        Assert.assertTrue(instance.getDistributedObjects().contains(object));
    }

    @Test
    public void testCustomObject() {
        Config config = new Config();
        config.getServicesConfig().addServiceConfig(
                new ServiceConfig().setServiceImpl(new TestInitializingObjectService())
                                   .setEnabled(true).setName(TestInitializingObjectService.NAME)
        );
        HazelcastInstance instance = createHazelcastInstanceFactory(1).newHazelcastInstance(config);
        TestInitializingObject object = instance
                .getDistributedObject(TestInitializingObjectService.NAME, "test-object");
        test(instance, object);
    }

    @Test
    public void testInitialization() {
        int nodeCount = 4;
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(nodeCount);
        Config config = new Config();
        config.getServicesConfig().addServiceConfig(
                new ServiceConfig().setServiceImpl(new TestInitializingObjectService())
                                   .setEnabled(true).setName(TestInitializingObjectService.NAME)
        );


        String serviceName = TestInitializingObjectService.NAME;
        String objectName = "test-object";

        HazelcastInstance[] instances = new HazelcastInstance[nodeCount];
        for (int i = 0; i < instances.length; i++) {
            instances[i] = factory.newHazelcastInstance(config);
            TestInitializingObject obj2 = instances[i].getDistributedObject(serviceName, objectName);
            Assert.assertTrue(obj2.init.get());
            Assert.assertFalse(obj2.error);
        }
    }

    @Test
    public void testInitialization2() {
        int nodeCount = 4;
        TestHazelcastInstanceFactory factory = createHazelcastInstanceFactory(nodeCount);
        Config config = new Config();
        config.getServicesConfig().addServiceConfig(
                new ServiceConfig().setServiceImpl(new TestInitializingObjectService())
                                   .setEnabled(true).setName(TestInitializingObjectService.NAME)
        );

        String serviceName = TestInitializingObjectService.NAME;
        String objectName = "test-object";

        HazelcastInstance[] instances = new HazelcastInstance[nodeCount];
        for (int i = 0; i < instances.length; i++) {
            instances[i] = factory.newHazelcastInstance(config);
            instances[i].getDistributedObject(serviceName, objectName);
        }

        for (int i = 0; i < nodeCount; i++) {
            Node node = TestUtil.getNode(instances[i]);
            ProxyServiceImpl proxyService = (ProxyServiceImpl) node.nodeEngine.getProxyService();
            Operation postJoinOperation = proxyService.getPostJoinOperation();

            for (int j = 0; j < nodeCount; j++) {
                if (i == j) continue;

                Node node2 = TestUtil.getNode(instances[j]);
                node.nodeEngine.getOperationService().send(postJoinOperation, node2.address);
            }
        }

        for (int i = 0; i < instances.length; i++) {
            TestInitializingObject obj = instances[i].getDistributedObject(serviceName, objectName);
            Assert.assertTrue(obj.init.get());
            Assert.assertFalse(obj.error);
        }
    }

    private static class TestInitializingObjectService implements RemoteService {
        static final String NAME = "TestInitializingObjectService";
        public DistributedObject createDistributedObject(final String objectName) {
            return new TestInitializingObject(objectName);
        }
        public void destroyDistributedObject(final String objectName) {
        }
    }

    private static class TestInitializingObject implements DistributedObject, InitializingObject {
        private final String name;
        private final AtomicBoolean init = new AtomicBoolean(false);
        private volatile boolean error = false;

        protected TestInitializingObject(final String name) {
            this.name = name;
        }

        @Override
        public void initialize() {
            if (!init.compareAndSet(false, true)) {
                error = true;
                throw new IllegalStateException("InitializingObject must be initialized only once!");
            }
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public String getServiceName() {
            return TestInitializingObjectService.NAME;
        }
        @Override
        public Object getId() {
            return getName();
        }
        @Override
        public String getPartitionKey() {
            return getName();
        }
        @Override
        public void destroy() {
        }
    }
}
