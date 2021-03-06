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

import java.nio.ByteBuffer;
import javax.jms.TextMessage;
import net.sfr.tv.messaging.client.impl.FileOutConsumerImpl;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.MessageHandler;

/**
 *
 * @author matthieu.chaplin@sfr.com
 */
public class FileOutMessageHandler extends FileOutConsumerImpl implements MessageHandler {

    public FileOutMessageHandler() throws Exception {
        super();
    }
    
    @Override
    public void release() {
        super.release();
    }
    
    @Override
    public void onMessage(ClientMessage msg) {
        
        ByteBuffer bb;
        String body;
        
        try {

            body = "hq-message-id=" + msg.getStringProperty("hq-message-id") + ";";
            
            if (TextMessage.class.isAssignableFrom(msg.getClass())) {
                body += ((TextMessage) msg).getText();
                //body += System.lineSeparator();
                body += "\n";
                bb = ByteBuffer.wrap(body.getBytes());
                
                out.writeDirect(bb, false);
            }

            // ACK the message to remove it from the queue.
            msg.acknowledge();

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}