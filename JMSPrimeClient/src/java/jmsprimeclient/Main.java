/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmsprimeclient;

import java.util.Scanner;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

/**
 *
 * @author ADMIN
 */
public class Main {

    @Resource(mappedName = "jms/ConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/TempQueue")
    private static Queue queue;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws JMSException, InterruptedException {
        // TODO code application logic here
        Connection connection = null;
        TextListener listener = null;

        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(
                    false,
                    Session.AUTO_ACKNOWLEDGE);

            listener = new TextListener();

            Queue replyQueue = session.createTemporaryQueue();
            MessageConsumer responseConsumer = session.createConsumer(replyQueue);
            responseConsumer.setMessageListener(listener);

            MessageProducer producer = session.createProducer(queue);
            TextMessage message = session.createTextMessage();

            while (true) {
                String ch = "";
                Scanner inp = new Scanner(System.in);
                System.out.println("Enter two number. Use ',' seperate each number. "
                        + "To end the program press enter");
                ch = inp.nextLine();
                if (ch.isEmpty()) {
                    return;
                }
                message.setText(ch);
                message.setJMSReplyTo(replyQueue);

                String correlationId = "12345";
                message.setJMSCorrelationID(correlationId);
                connection.start();
                System.out.println("Sending message: " + message.getText());

                producer.send(message);
                Thread.sleep(5000);
            }

        } catch (JMSException e) {
            System.err.println("Exception occurred: " + e.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                }
            }
        }
    }

}
