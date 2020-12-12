package bgu.spl.mics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private static class SingletonHolder { // a class for holding the singleton instance
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> messageQueues;
	private ConcurrentHashMap<Class<? extends Message>, HashSet<MicroService>> subscriptionMap;
	private ConcurrentHashMap<MicroService, HashSet<Class<? extends Message>>> reverseSubscriptionMap;
	private HashMap<Event, Future> futures;
	private final ReadWriteLock readWriteLock;
	private final Lock readLock;
	private final Lock writeLock;


	private MessageBusImpl() {
		messageQueues = new ConcurrentHashMap<>();
		subscriptionMap = new ConcurrentHashMap<>();
		reverseSubscriptionMap = new ConcurrentHashMap<>();
		futures = new HashMap<>();
		readWriteLock = new ReentrantReadWriteLock();
		readLock = readWriteLock.readLock();
		writeLock = readWriteLock.writeLock();
	}

	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		readLock.lock();
		try {
			if (isRegistered(m)) { // check if mics already registered
				if (!isSubscribedTo(m, type)) { // the mics is not subscribed to the event type
					readLock.unlock(); // attempt writing
					writeLock.lock();
					try {
						if (!subscriptionMap.containsKey(type)) { // check if event does not exist in submap - never been used
							subscriptionMap.put(type, new HashSet<>()); // if so then create an entry for it
							subscriptionMap.get(type).add(m); // add the microservice to the submap at the specific event entry
							reverseSubscriptionMap.get(m).add(type); // also map the event in the reverse submap
						} else if (!subscriptionMap.get(type).contains(m)) { // check if the mics is registered to event already
							subscriptionMap.get(type).add(m); // add the mics to the event type map
							reverseSubscriptionMap.get(m).add(type);
						}
					} finally {
						writeLock.unlock();
					}
				}
			}
		} finally {
			readLock.unlock();
		}
	}


	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		readLock.lock();
		try {
			if (isRegistered(m)) {
				if (!isSubscribedTo(m, type)) { // the mics is not subscribed to the broadcast type
					readLock.unlock(); // attempt writing
					writeLock.lock();
					try {
						if (!subscriptionMap.containsKey(type)) { // double check - if broadcast does not exist in submap
							subscriptionMap.put(type, new HashSet<>());
							subscriptionMap.get(type).add(m); // add the microservice to the submap
							reverseSubscriptionMap.get(m).add(type);
						} else if (!subscriptionMap.get(type).contains(m)) { //check if mics registered to broadcast already
							subscriptionMap.get(type).add(m);
							reverseSubscriptionMap.get(m).add(type);
						}
					} finally {
						writeLock.unlock();
					}
				}
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		// resolve the associated future with result
		futures.get(e).resolve(result);
		futures.get(e).notifyAll();
		futures.remove(e);

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		readLock.lock();
		try {
			if (subscriptionMap.containsKey(b.getClass())) { // if any mics is subscribed to the broadcast type
				readLock.unlock();
				writeLock.lock(); //attempt to write
				try {
					if (subscriptionMap.containsKey(b.getClass())) { // double check if anything has changed
						for (MicroService mics : subscriptionMap.get(b.getClass())) //for each microservice subscribed to the broadcast
							messageQueues.get(mics).add(b); // insert the message in the correct mics queue and notify it if it is waiting
					}
				} finally {
					writeLock.unlock();
				}
			}
		} finally {
			readLock.unlock();
		}
	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		readLock.lock();
		Future<T> future = null;
		try {
			if (subscriptionMap.containsKey(e.getClass())) { // check if any service is subscribed to the event
				future = new Future<>(); // create a new future and associate the future with the event
				futures.put(e, future);
				roundRobinSend(e); // insert the message in the correct mics queue and notify it if it is waiting
			}
		} finally {
			readLock.unlock();
		}
		return future;
	}

	@Override
	public void register(MicroService m) {
		readLock.lock();
		try {
			if (!isRegistered(m)) { // check if m not in queues
				readLock.unlock();
				writeLock.lock();
				try {
					if (!isRegistered(m)) {
						// make a queue for the mics m
						messageQueues.put(m, new LinkedBlockingQueue<Message>());
						reverseSubscriptionMap.put(m, new HashSet<>());
					}
				} finally {
					writeLock.unlock();
				}
			}
		} finally {
			readLock.unlock();
		}
	}


	@Override
	public void unregister(MicroService m) {
		readLock.lock();
		try {
			if (isRegistered(m)) { // check if m in queues
				readLock.unlock();
				writeLock.lock();
				try {
					if (isRegistered(m)) {
						// get all of m's subscriptions
						HashSet<Class<? extends Message>> subscriptions = reverseSubscriptionMap.get(m);
						for (Class type : subscriptions)  // remove m from the as a subscriber for each subscription
							subscriptionMap.get(type).remove(m);
						messageQueues.remove(m); // remove the mics queue from the message bus
						reverseSubscriptionMap.remove(m); // remove the mics  reverse record from the message bus
					}
				} finally {
					writeLock.unlock();
				}
			}
		} finally {
			readLock.unlock();
		}
	}

	private boolean isSubscribedTo(MicroService m, Class<? extends Message> type) {
		if (subscriptionMap.containsKey(type)) {
			return subscriptionMap.get(type).contains(m);
		}
		return false;
	}

	private boolean isRegistered(MicroService m) {
		return messageQueues.containsKey(m);
	}

	private <T> void roundRobinSend(Event<T> e) {
		//TODO implement RR
	}


	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (!messageQueues.containsKey(m))
			throw new IllegalStateException();
		// attempt to retrieve a message from m's queue - blocking queue will put thread in waiting if no message is available
		Message message = messageQueues.get(m).take();
		// use the callback
		return message;
	}
}
