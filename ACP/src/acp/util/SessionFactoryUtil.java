package acp.util;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import acp.store.DataStore;

public class SessionFactoryUtil {
	final static Logger logger = LoggerFactory.getLogger(SessionFactoryUtil.class);
	
	private static SessionFactory factory;

	private SessionFactoryUtil() {
	}
	
	public static void initialise() throws Throwable{
		if(factory == null){
			Configuration configuration = new Configuration();
			configuration.configure();
			ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
			factory = configuration.buildSessionFactory(serviceRegistry);
		}
	}
	
	public static void close() {
		if (factory != null) {
			try {
				factory.close();
			} catch (HibernateException ignored) {
				System.out.println("Couldn't close SessionFactory");
			} finally {
				factory = null;
			}
		}
	}
	
	public static SessionFactory getFactory(){
		return factory;
	}
}
