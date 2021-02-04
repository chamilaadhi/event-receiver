package com.chamila.sampleapp;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.wso2.andes.client.message.AMQPEncodedMapMessage;
import java.util.Properties;

public class EventReceiver implements MessageListener {
    String topicName = "notification";
    TopicConnection topicConnection;
    TopicSession topicSession;
    
    String brokerUrl = "tcp://0.0.0.0:5672";
    String username = "admin";
    String password = "admin";

    public static void main(String[] args) throws NamingException, JMSException {
        EventReceiver eventReceiver = new EventReceiver();
        eventReceiver.start();
    }

    public void start() throws NamingException, JMSException {
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
        properties.put("transport.jms.DestinationType", "topic");
        properties.put("connectionfactory.TopicConnectionFactory",
                "amqp://" + username + ":" + password + "@clientid/carbon?brokerlist='" + brokerUrl + "'");
        properties.put("transport.jms.ConnectionFactoryJNDIName", "TopicConnectionFactory");

        InitialContext ctx = new InitialContext(properties);
        TopicConnectionFactory connFactory = (TopicConnectionFactory) ctx.lookup("TopicConnectionFactory");

        topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);

        Topic topic = topicSession.createTopic(topicName);
        MessageConsumer consumer = topicSession.createConsumer(topic);
        consumer.setMessageListener(this);
        System.out.println("\n\nEvent receiver started and Listening to " + brokerUrl);
    }

    @Override
    public void onMessage(Message message) {

        AMQPEncodedMapMessage msg = (AMQPEncodedMapMessage) message;
        try {
            System.out.println("\nEvent type: " + msg.getString("eventType"));
            System.out.println("Event     : " + new String(Base64.decodeBase64(msg.getString("event").getBytes())));

        } catch (JMSException e) {
            System.out.print("Error whline reading the jms message " + e.getMessage());
        }
    }

}
