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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

/**
 *
 */
@ApplicationScoped
public class CustomerService {

    private @Inject EntityManager em;

    private @Inject UserTransaction tx;

    public Customer createCustomer(String firstName, String surName) throws Exception {
        Customer cust = new Customer();
        cust.setFirstName(firstName);
        cust.setSurName(surName);

        tx.begin();
        try {
            em.persist(cust);
            em.joinTransaction();
            tx.commit();

            return cust;
        }
        catch(Exception e) {
            tx.rollback();
            throw e;
        }
    }

    public Customer loadCustomer(Long customerId) {
        return em.find(Customer.class, customerId);
    }

    public Customer mergeCustomer(Customer cust) {
        return em.merge(cust);
    }

    public void storeCustomer(Customer cust) throws Exception {
        tx.begin();
        em.joinTransaction();
        tx.commit();
    }

}
