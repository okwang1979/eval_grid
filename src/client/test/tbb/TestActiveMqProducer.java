package test.tbb;

import com.ufida.iufo.pub.tools.AppDebug;

import nc.bs.framework.test.AbstractTestCase;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class TestActiveMqProducer extends AbstractTestCase{
    //����ActivMQ�����ӵ�ַ
    private static final String ACTIVEMQ_URL = "tcp://127.0.0.1:61616";
    //���巢����Ϣ�Ķ�������
    private static final String QUEUE_NAME = "MyMessage";
 
	
	public void testStart() throws JMSException{
		AppDebug.info("message");
		
		
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
        //����һ��������
        javax.jms.MessageProducer producer = session.createProducer(destination);
    
        //����ģ��100����Ϣ
        for (int i = 1 ; i <= 100 ; i++){
            TextMessage message = session.createTextMessage("�ҷ���message: is " + i);
            //������Ϣ
            producer.send(message);
            //�ڱ��ش�ӡ��Ϣ
            System.out.println("�����ڷ�����Ϣ�ǣ�" + message.getText());
        }
        //�ر�����
        connection.close();
 
		
		
		
	}

}
