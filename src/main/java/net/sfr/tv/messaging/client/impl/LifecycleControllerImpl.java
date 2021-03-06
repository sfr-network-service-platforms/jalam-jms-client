/**
 * Copyright 2012-2014 - SFR (http://www.sfr.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.sfr.tv.messaging.client.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.sfr.tv.exceptions.ResourceInitializerException;
import net.sfr.tv.messaging.client.api.LifecycleController;
import net.sfr.tv.messaging.api.MessageConsumer;
import net.sfr.tv.jms.client.impl.listener.LoggerMessageListener;
import org.apache.log4j.Logger;

/**
 * Base LifecycleController implementation. 
 * Feel free to create your own to do more sophisticated things !
 * Like external resources allocation & release.
 *
 * @see net.sfr.tv.messaging.client.api.LifecycleController
 *
 * @author matthieu.chaplin@sfr.com
 * @author pierre.cheynier@sfr.com
 * @author scott.messner.prestataire@sfr.com
 */
public class LifecycleControllerImpl implements LifecycleController {

    private final Logger logger = Logger.getLogger(LifecycleControllerImpl.class);

    private final Map<Integer, MessageConsumer> listeners = new HashMap<>();

    private int insertIdx = 0;
    private int retrievalIdx = 0;

    @Override
    public void initListener(Class listener) throws ResourceInitializerException {
        if (listener == null) {
            listener = LoggerMessageListener.class;
        }
        listeners.put(insertIdx++, createListener(listener));
        logger.info("Listener registered : ".concat(listener.getName()));        
    }
    
    

    @Override
    public MessageConsumer getListener(Class listenerClass) throws ResourceInitializerException {
        /*if (insertIdx <= retrievalIdx) {
            listeners.put(insertIdx++, createListener(listenerClass));
        }*/
        // RETRIEVE A LISTENER AND INCREMENT POSITION IDX.
        MessageConsumer ret = listeners.get(retrievalIdx++);
        logger.info("Will be using listener instance : " + ret.toString());
        return ret;
    }

    /**
     * Register a new listener of the class specified and add it to the list of listeners
     *
     * @param listenerClass class of new listener to be instantiated and added.
     * @throws ResourceInitializerException
     */
    private MessageConsumer createListener(final Class listenerClass) throws ResourceInitializerException {
        MessageConsumer ret = null;
        try {
            Constructor ct = listenerClass.getConstructor();
            ret = (MessageConsumer) ct.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new ResourceInitializerException(ex);
        }
        return ret;
    }

    @Override
    public Collection<MessageConsumer> getListeners() {
        return listeners.values();
    }

    @Override
    public void run() {
    }

    @Override
    public void release() {
        for (MessageConsumer listener : listeners.values()) {
            listener.release();
        }
    }
}
