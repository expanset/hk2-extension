package com.expanset.hk2.jms;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.InstanceLifecycleEvent;
import org.glassfish.hk2.api.InstanceLifecycleEventType;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.common.errors.ExceptionAdapter;

/**
 * Message driven service support.
 */
@Service
public class MessageDrivenService implements InstanceLifecycleListener, PreDestroy {

    private final static Filter FILTER = new Filter() {

        @Override
        public boolean matches(Descriptor d) {
            return d.getQualifiers().contains(MessageDriven.class.getName());
        }
    };
    
    protected final ConcurrentMap<Object, MessageSubscription> connectionFactoryHolders = new ConcurrentHashMap<>();
    
    private final static Logger log = LoggerFactory.getLogger(MessageDrivenService.class);
    
    @Inject
    protected ServiceLocator serviceLocator;
    
	@Override
	public Filter getFilter() {
		return FILTER;
	}

	@Override
	public void lifecycleEvent(InstanceLifecycleEvent lifecycleEvent) {
		if(lifecycleEvent.getEventType() == InstanceLifecycleEventType.POST_PRODUCTION) {			
			startListening(lifecycleEvent.getLifecycleObject());
		} else if(lifecycleEvent.getEventType() == InstanceLifecycleEventType.PRE_DESTRUCTION) {
			stopListening(lifecycleEvent.getLifecycleObject());
		}
	}

	@Override
	public void preDestroy() {
		final Object[] messageDrivenObjects = connectionFactoryHolders.keySet().toArray();
		for(Object messageDrivenObject : messageDrivenObjects) {
			stopListening(messageDrivenObject);
		}
	}

	protected void startListening(Object messageDrivenObject) {
		connectionFactoryHolders.computeIfAbsent(
				messageDrivenObject, 
				(key) -> ExceptionAdapter.get(() -> new MessageSubscription(key)));
	}
	
	protected void stopListening(Object messageDrivenObject) {
		log.debug("Unregister message driven object {}:{}", 
				messageDrivenObject.getClass().getName(), 
				messageDrivenObject.hashCode());
		
		final MessageSubscription subscription = 
				connectionFactoryHolders.remove(messageDrivenObject);
		if(subscription != null) {
			subscription.close();
		}
	}

	protected class MessageSubscription implements ExceptionListener {
		
		protected final Connection connection;
		
		public MessageSubscription(Object messageDrivenObject) 
				throws JMSException {
			final MessageDriven ann = messageDrivenObject.getClass().getAnnotation(MessageDriven.class);

			log.debug("Register message driven object {}:{}, destination {}", 
					messageDrivenObject.getClass().getName(), 
					messageDrivenObject.hashCode(),
					ann.destination());
			
			final ConnectionFactory connectionFactory = StringUtils.isEmpty(ann.connectionFactory()) ? 
					serviceLocator.getService(ConnectionFactory.class) : 
					serviceLocator.getService(ConnectionFactory.class, ann.connectionFactory());		
			connection = connectionFactory.createConnection();
			if(messageDrivenObject instanceof ExceptionListener) {
				connection.setExceptionListener((ExceptionListener)messageDrivenObject);
			} else {
				connection.setExceptionListener(this);
			}
			if(StringUtils.isNotEmpty(ann.clientId())) {
				connection.setClientID(ann.clientId());
			}
			
			final Session session = connection.createSession(ann.transacted(), ann.acknowledgeMode());
			
			final Destination destination;
			switch(ann.destinationType()) {
			case QUEUE:
				destination = session.createQueue(ann.destination());
				break;
			case TOPIC:
				destination = session.createTopic(ann.destination());
				break;
			default:
				throw new IllegalStateException();
			}			
			
			final MessageConsumer consumer;
			if(ann.subscriptionDurability()) {
				consumer = session.createDurableSubscriber(
						(Topic) destination, 
						ann.subscriptionName(), 
						ann.messageSelector(), 
						ann.subscriptionNoLocal());
			} else {
				consumer = session.createConsumer(
						destination, 
						ann.messageSelector());
			}				
			consumer.setMessageListener((MessageListener)messageDrivenObject);
			
			connection.start();
		}
		
		public void close() {
			try {
				connection.close();
			} catch (Throwable e) {
				log.error("Connection closing error", e);
			}
		}

		@Override
		public void onException(JMSException exception) {
			log.warn("JMS error", exception);
		}
	}
}
