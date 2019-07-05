package test.tbb;
import nc.bs.framework.test.AbstractTestCase;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
public class TestActiveConsumer extends AbstractTestCase{
	
	
    //����ActivMQ�����ӵ�ַ
    private static final String ACTIVEMQ_URL = "tcp://127.0.0.1:61616";
    //���巢����Ϣ�Ķ�������
    private static final String QUEUE_NAME = "MyMessage";
 
    
    public static void main(String[] args)throws JMSException{
    	  //�������ӹ���
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ACTIVEMQ_URL);
        //��������
        Connection connection = activeMQConnectionFactory.createConnection();
        //������
        connection.start();
        //�����Ự
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //��������Ŀ��
        Destination destination = session.createQueue(QUEUE_NAME);
        //����������
        javax.jms.MessageConsumer consumer = session.createConsumer(destination);
        //�������ѵļ���
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("��ȡ��Ϣ��" + textMessage.getText());
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
 
    }
}
