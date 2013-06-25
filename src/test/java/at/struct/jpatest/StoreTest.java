/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package at.struct.jpatest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StoreTest extends ContainerTest {

    @Inject
    private CustomerService custSvc;

    @Test
    public void testCustomerLifecycle() throws Exception {
        Assert.assertNotNull(custSvc);

        Customer cust = custSvc.createCustomer("Hans", "Huber");
        Assert.assertNotNull(cust);

        Long customerId = cust.getId();

        assertSurName(customerId);

        // emulate new request
        cdiContainer.getContextControl().stopContext(RequestScoped.class);
        cdiContainer.getContextControl().startContext(RequestScoped.class);

        // actually since we do not bind this to the transaction, this change must NOT get written to the DB!
        cust.setSurName("Meier");
        custSvc.mergeCustomer(cust);

        // emulate new request
        cdiContainer.getContextControl().stopContext(RequestScoped.class);
        cdiContainer.getContextControl().startContext(RequestScoped.class);

        assertSurName(customerId);

        custSvc.storeCustomer(cust);

        // emulate new request
        cdiContainer.getContextControl().stopContext(RequestScoped.class);
        cdiContainer.getContextControl().startContext(RequestScoped.class);

        assertSurName(customerId);
    }

    private void assertSurName(Long customerId) {
        Customer c = custSvc.loadCustomer(customerId);
        Assert.assertNotNull(c);
        Assert.assertEquals(c.getSurName(), "Huber");
    }
}
