package de.binformed.platform.activemq;

import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.MessageProducer;
import javax.jms.JMSException;
 
public class Sender {
     
	private static Log logger = LogFactory.getLog(Sender.class);

    public static void send(String messageToSend) throws JMSException {        
        try {
			 
        	Session session = Broker.getSession();
        	MessageProducer producer = Broker.getMessageProducer();  
			 
			TextMessage message = session.createTextMessage(messageToSend);
			 
			// Here we are sending our message!
			producer.send(message);
			 
			logger.info("Sent '" + message.getText() + "'");
		} catch (Exception e) {
			logger.error("Couln't send a message to ActiveMQ");
			e.printStackTrace();
		}
    }

}
