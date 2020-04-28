package org.jpwh.helloWorld;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.resource.transaction.backend.jta.internal.JtaTransactionCoordinatorBuilderImpl;
import org.hibernate.service.ServiceRegistry;
import org.jpwh.env.TransactionManagerTest;
import org.jpwh.model.helloworld.Message;
import org.testng.annotations.Test;

import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class HelloWorldHibernate extends TransactionManagerTest {

    protected void unsuedSimpleFactory(){
        SessionFactory sf = new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .configure("hibernate.cfg.xml").build()
        ).buildMetadata().buildSessionFactory();
    }


    protected SessionFactory createSessionFactory() throws Exception {
        StandardServiceRegistryBuilder serviceRegistryBuilder =
                new StandardServiceRegistryBuilder();

        serviceRegistryBuilder
                .applySetting("hibernate.connection.datasource", "myDS")
                .applySetting("hibernate.format_sql", "true")
                .applySetting("hibernate.use_sql_comments", "true")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop");

        // Enable JTA (this is a bit crude because Hibernate devs still believe that JTA is
        // used only in monstrous application servers and you'll never see this code).
        serviceRegistryBuilder.applySetting(
                Environment.TRANSACTION_COORDINATOR_STRATEGY,
                JtaTransactionCoordinatorBuilderImpl.class
        );
        ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        metadataSources.addAnnotatedClass(
                org.jpwh.model.helloworld.Message.class
        );

        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();
        Metadata metadata = metadataBuilder.build();
        assertEquals(metadata.getEntityBindings().size(), 1);
        SessionFactory sessionFactory = metadata.buildSessionFactory();
        return sessionFactory;
    }


    @Test
    public void storeLoadMessage() throws Exception {

        SessionFactory sessionFactory = createSessionFactory();
        try{
            {
                UserTransaction tx = TM.getUserTransaction();
                tx.begin();

                Session session = sessionFactory.getCurrentSession();
                Message message = new Message();
                message.setText("Hello World");
                session.persist(message);
                tx.commit();
            }

            {
                UserTransaction tx = TM.getUserTransaction();
                tx.begin();
                List<Message> messages = sessionFactory.getCurrentSession().createCriteria(Message.class).list();

                assertEquals(messages.size(),1);
                assertEquals(messages.get(0).getText(),"Hello World");
                tx.commit();
            }
        }finally {
            TM.rollback();
        }

    }

}
