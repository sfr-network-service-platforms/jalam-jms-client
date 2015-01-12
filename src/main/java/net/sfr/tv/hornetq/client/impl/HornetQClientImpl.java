/*
 * Copyright 2015 matthieu.chaplin@sfr.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sfr.tv.hornetq.client.impl;

import java.util.TreeMap;
import net.sfr.tv.exceptions.ResourceInitializerException;
import net.sfr.tv.hornetq.HqCoreConnectionManager;
import net.sfr.tv.messaging.api.ConsumerConnectionManager;
import net.sfr.tv.messaging.api.MessageConsumer;
import net.sfr.tv.messaging.api.SubscriptionDescriptor;
import net.sfr.tv.messaging.client.impl.AbstractMessagingClient;
import net.sfr.tv.messaging.impl.MessagingProvidersConfiguration;
import org.apache.log4j.Logger;
import org.hornetq.api.core.client.MessageHandler;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class HornetQClientImpl extends AbstractMessagingClient {

    private static final Logger logger = Logger.getLogger(HornetQClientImpl.class);
    
    public HornetQClientImpl(
            MessagingProvidersConfiguration msgingProviderConfig, 
            String preferredServer, 
            String subscriptionBaseName, 
            String selector, 
            Class lifecycleControllerClass, 
            Class listenerClass, 
            String[] destinations) throws ResourceInitializerException {
        
        super(msgingProviderConfig, preferredServer, subscriptionBaseName, selector, lifecycleControllerClass, listenerClass, destinations);
        
        // Connect and Subscribe listeners to destinations
        cnxManagers = new TreeMap<>();
        int idxListener = 0;
        for (String group : msgingProviderConfig.getGroups()) {
            try {
                for (MessageConsumer listener : lifecycleController.getListeners()) {
                    /*if (lifecycleController.getListeners().size() > 1) {
                        clientId = clientId.concat("/" + idxListener++);
                    }*/
                    ConsumerConnectionManager cnxManager = new HqCoreConnectionManager(group, msgingProviderConfig.getCredentials(), msgingProviderConfig.getServersGroup(group), preferredServer, (MessageHandler) listener);
                    cnxManager.connect(2);
                    logger.info("Connection created for ".concat(listener.getName()));

                    String subscriptionName;
                    int subscriptionIdx = 0;
                    for (String dest : listener.getDestinations()) {
                        subscriptionName = subscriptionBaseName.concat("@").concat(dest).concat(listener.getDestinations().length > 1 ? "-" + subscriptionIdx++ : "");
                        // FIXME : Handle topic/durable subscription booleans
                        cnxManager.subscribe(new SubscriptionDescriptor(dest, true, true, subscriptionName, selector), 2);
                        if (logger.isInfoEnabled() || logger.isDebugEnabled()) {
                            logger.info("Destination : ".concat(dest));
                            logger.info("Subscription base name : ".concat(subscriptionBaseName));
                            logger.info("Filter : ".concat(selector != null ? selector : ""));
                            logger.info("Servers groups : ".concat(String.valueOf(msgingProviderConfig.getGroups().size())));
                        }
                    }
                    cnxManagers.put("clientId-unset", cnxManager);
                }
            } catch (Exception ex) {
                logger.error("Unable to start a listener/context binded to : ".concat(group), ex);
            }
        }
        
    }
    
    @Override
    public void shutdown() {
        logger.warn("shutdown() : Not implemented yet !");
    }
}
