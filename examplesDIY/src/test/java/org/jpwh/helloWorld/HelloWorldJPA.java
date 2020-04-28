package org.jpwh.helloWorld;

import org.jpwh.env.TransactionManagerTest;
import org.jpwh.model.helloworld.Message;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class HelloWorldJPA extends TransactionManagerTest {

    @Test
    public void storeLoadMessage() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("HelloWorldPU");
        try {

            {
                UserTransaction tx = TM.getUserTransaction();
                tx.begin();
                EntityManager em = emf.createEntityManager();
                Message message = new Message();
                message.setText("Hello World");
                em.persist(message);
                tx.commit();
                em.close();
            }
            {
                UserTransaction tx2 = TM.getUserTransaction();
                tx2.begin();
                EntityManager em2 = emf.createEntityManager();
                List<Message> messages = em2.createQuery("select m from Message m").getResultList();
                assertEquals(1, messages.size());
                assertEquals("Hello World", messages.get(0).getText());

                messages.get(0).setText("Take me to your leader");

                tx2.commit();
                em2.close();
            }
        }
        finally{
            TM.rollback();
            emf.close();

        }

    }


}
