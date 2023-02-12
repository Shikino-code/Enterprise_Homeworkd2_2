/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmsprimeserver;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

/**
 *
 * @author sarun
 */
public class TextListener implements MessageListener {

    private MessageProducer replyProducer;
    private Session session;

    public TextListener(Session session) {

        this.session = session;
        try {
            replyProducer = session.createProducer(null);
        } catch (JMSException ex) {
            Logger.getLogger(TextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onMessage(Message message) {
        TextMessage msg = null;
        try {

            if (message instanceof TextMessage) {
                msg = (TextMessage) message;
                System.out.println("Reading message is: " + msg.getText());
            } else {
                System.err.println("Message is not a TextMessage");
            }

            TextMessage response = session.createTextMessage();
            String[] convertToString = msg.getText().split(",");

            int start = Integer.valueOf(convertToString[0]);
            int end = Integer.valueOf(convertToString[1]);
            int amount = 0;
            boolean iPrime;

            for (int i = start; i <= end; i++) {
                iPrime = PrimeFilter.isPrime(i);
                if (iPrime) {
                    amount++;
                }
            }
            
            response.setText(String.valueOf(amount));

            System.out.println("sending message The number of prices between "
                    + start + " and " + end + " : " + response.getText());
            response.setJMSCorrelationID(message.getJMSCorrelationID());
            replyProducer.send(message.getJMSReplyTo(), response);
        } catch (JMSException e) {
            System.err.println("JMSException in onMessage(): " + e.toString());
        } catch (Throwable t) {
            System.err.println("Exception in onMessage():" + t.getMessage());
        }
    }
}
