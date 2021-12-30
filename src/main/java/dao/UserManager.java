package main.java.dao;

import main.java.model.User;
import main.java.utility.HibernateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;

public class UserManager<T> extends BaseManager<T> {
	
	private static final Log log = LogFactory.getLog(UserManager.class);

	private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	
	public void persist(User transientInstance) {
		log.debug("persisting User instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(User instance) {
		log.debug("attaching dirty User instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(User instance) {
		log.debug("attaching clean User instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(User persistentInstance) {
		log.debug("deleting User instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public User merge(User detachedInstance) {
		log.debug("merging User instance");
		try {
			User result = (User) sessionFactory.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public User findById(int id) {
		log.debug("getting User instance with id: " + id);
		try {
			User instance = (User) sessionFactory.getCurrentSession().get("User", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public User findByUsernameOrEmail(String identity) {
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			session.beginTransaction();
			String searchEmailHQL = "FROM User WHERE email = :identity OR username = :identity";
			@SuppressWarnings("unchecked")
			Query<User> query = session.createQuery(searchEmailHQL);//.setParameter("email", email);
			query.setParameter("identity", identity);
			List<User> users = query.getResultList();
			if (users == null || users.size() == 0) {
				log.debug("get successful, no instance found");
				return null;
			} else {
				log.debug("get successful, instance found");
			}
			return users.get(0);
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		} finally {
			if(session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public User findByUsernameOrEmail(String usernameOrEmail, String password) {
		// Pre-condition check
		if (usernameOrEmail == null) {
			System.err.println("Username invalid!");
		}
		log.debug("getting User instance with username or email: " + usernameOrEmail);
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			session.beginTransaction();
			String searchUserHQL = "";
			// If given both username, email and password
			if (password != null) {
				searchUserHQL = "FROM User user WHERE user.username = :identification AND user.password = :password OR user.email = :identification AND user.password = :password";
			} else { // Search by username or email only
				searchUserHQL = "FROM User user WHERE user.username = :identification OR user.email = :identification";
			}
			        
			@SuppressWarnings("unchecked")
			Query<User> query = session.createQuery(searchUserHQL);//.setParameter("email", email);
			query.setParameter("identification", usernameOrEmail);
			if (password != null) {
				query.setParameter("password", password);
			}
			List<User> users = query.getResultList();
			if (users == null || users.size() == 0) {
				log.debug("get successful, no instance found");
				return null;
			} else {
				log.debug("get successful, instance found");
			}
			return users.get(0);
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		} finally {
			if(session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public User verifyResetPassword(String email, Date dob) {
		log.debug("getting User instance with email: " + email + " and dob: " + dob);
		Session session = null;
		try {
			session = sessionFactory.getCurrentSession();
			session.beginTransaction();
			String searchUserHQL = "FROM User user WHERE user.email = :email AND user.birthday = :dob";
			@SuppressWarnings("unchecked")
			Query<User> query = session.createQuery(searchUserHQL);//.setParameter("email", email);
			query.setParameter("email", email);
			query.setParameter("dob", dob);
			
			List<User> users = query.getResultList();
			if (users == null || users.size() == 0) {
				log.debug("get successful, no instance found");
				return null;
			} else {
				log.debug("get successful, instance found");
			}
			return users.get(0);
		} catch (RuntimeException re) {
			log.error("get failed", re);
			return null;
		} finally {
			if(session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}
