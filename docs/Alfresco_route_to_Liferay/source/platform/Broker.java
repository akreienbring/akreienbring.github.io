package de.binformed.platform.activemq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Broker {
    // default broker URL is : tcp://localhost:61616"
	private final static String URL = ActiveMQConnection.DEFAULT_BROKER_URL;
	private final static String QUEUE = "liferay";

	private static Log logger = LogFactory.getLog(Broker.class);
	
	private static Connection connection = null;
	private static Session session  = null;
	private static Destination destination = null;
	private static MessageProducer producer = null;

	 
	protected static MessageProducer getMessageProducer() {
		if (!(producer instanceof MessageProducer)) {
			createMessageProducer();
		}else {
			logger.info("Reusing the MessageProducer");
			
		}
		
		return producer;
	}
	
	protected static Session getSession() {
		if (!(session instanceof Session)) {
			createSession();
		}else {
			logger.info("Reusing the Session to '" + URL + "'" + "' with destination '" + QUEUE + "'");
		}
		
		return session;
	}
	
	private static void createMessageProducer() {
		if (!(session instanceof Session)) {
			createSession();
		}else {
			logger.info("Reusing the Session to '" + URL + "'" + "' with destination '" + QUEUE + "'");
		}
		
		try {
			// MessageProducer is used for sending messages to the queue.
			producer = session.createProducer(destination);
			logger.info("Created a new MessageProducer");
		} catch (JMSException e) {
			logger.error("Couldn't create a MessageProducer");
			e.printStackTrace();
		}  
	}
	
	private static void createSession() {
		if (!(connection instanceof Connection)) {
			createConnection();
		}else {
			logger.info("Reusing the connection to '" + URL + "'");
		}
		
		try {
			//Creating a non transactional session to send/receive JMS message.
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			//Destination represents here our queue 'liferay' on the JMS server. 
			//The queue will be created automatically on the server or used if it exists
			destination = session.createQueue(QUEUE); 
			
			logger.info("Created a new Session to '" + URL + "' with destination '" + QUEUE + "'");

		} catch (JMSException e) {
			logger.error("Couldn't create a session with the message server");
			e.printStackTrace();
		}  
	}
	
	private static void createConnection() {
		
		try {
			// Getting JMS connection from the server and starting it
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Broker.URL);
			connection = connectionFactory.createConnection();
			connection.start();
			logger.info("Created a new connection to '" + URL + "'");
			
		} catch (JMSException e) {
			logger.error("Couldn't create a connection to the message server");
			e.printStackTrace();
		}
	}
	

}
