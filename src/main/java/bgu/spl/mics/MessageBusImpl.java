package bgu.spl.mics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
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

	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> messageQueues; // a structure that holds registered mics as keys and their message queues as values
	private ConcurrentHashMap<Class<? extends Message>, ArrayList<MicroService>> subscriptionMap; // a structure that maps event types to the mics that are subscribed to it
	private ConcurrentHashMap<MicroService, HashSet<Class<? extends Message>>> reverseSubscriptionMap; // a structure that maps mics to their subscriptions
	private HashMap<Event, Future> futures; // a structure mapping events to their respective future events
	private Map<Class<? extends Message>, AtomicInteger> indexList;
	private final Lock readLock;
	private final Lock writeLock;


	private MessageBusImpl() {
		messageQueues = new ConcurrentHashMap<>();
		subscriptionMap = new ConcurrentHashMap<>();
		reverseSubscriptionMap = new ConcurrentHashMap<>();
		futures = new HashMap<>();
		ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		readLock = readWriteLock.readLock();
		writeLock = readWriteLock.writeLock();
		indexList = new HashMap<>();
	}

	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		subscribeMessage(type, m, true);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribeMessage(type, m, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		// resolve the associated future with result
		Future currFuture = futures.get(e);
		synchronized (currFuture) {
			if (futures.containsKey(e)) {
				futures.get(e).resolve(result);
				futures.get(e).notifyAll();
				futures.remove(e);
			}
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		readLock.lock();
		try {
			if (subscriptionMap.containsKey(b.getClass())) { // if any mics is subscribed to the broadcast type
				readLock.unlock();
				writeLock.lock(); //attempt to write
				try {
					addBroadcastToQueues(b);
				} finally {
					readLock.lock();
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
			ArrayList<MicroService> subscriptions = subscriptionMap.getOrDefault(e.getClass(), null);
			if (subscriptions != null && subscriptions.size() > 0){
				future = new Future<>(); // create a new future and associate the future with the event
				futures.put(e, future);
				roundRobinSend(e, subscriptions);
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
					readLock.lock();
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
						for (Class type : subscriptions) {  // remove m from the as a subscriber for each subscription
							subscriptionMap.get(type).remove(m);
							removeEmptySubscriptions(type); // if the event type is empty - remove all traces of it
						}
						messageQueues.remove(m); // remove the mics queue from the message bus
						reverseSubscriptionMap.remove(m); // remove the mics  reverse record from the message bus
					}
				} finally {
					readLock.lock();
					writeLock.unlock();
				}
			}
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws IllegalStateException, InterruptedException {
		if (!messageQueues.containsKey(m))
			throw new IllegalStateException();
		// attempt to retrieve a message from m's queue - blocking queue will put thread in waiting if no message is available
		return messageQueues.get(m).take();
	}

	private void removeEmptySubscriptions(Class<? extends Message> type){
		if (subscriptionMap.get(type).size()==0) {
			subscriptionMap.remove(type);
			indexList.remove(type);
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

	private <T> void roundRobinSend(Event<T> e, ArrayList<MicroService> subscriptions) {
		// check the current index to decide which mics going to get the event
		AtomicInteger index = indexList.get(e.getClass());

		int currentIndex = 0;
		int newIndex;

		// increment the index
		boolean changed = false;
		while (!changed) {
			currentIndex = index.get();
			newIndex = currentIndex + 1;
			changed = index.compareAndSet(currentIndex , newIndex);

			// modulo on the index in case it's bigger than subscription size
			currentIndex %= subscriptions.size();
		}
		// send the event to the next microservice in line
		MicroService m = subscriptions.get(currentIndex);
		messageQueues.get(m).add(e);
	}

	private void subscribeMessage(Class<? extends Message> type, MicroService m, boolean roundRobinRequired){
		readLock.lock();
		try {
			if (isRegistered(m)) { // check if mics already registered
				if (!isSubscribedTo(m, type)) { // the mics is not subscribed to the event type
					readLock.unlock(); // attempt writing
					writeLock.lock();
					try {
						manageSubscriptions(type, m, roundRobinRequired);
					} finally {
						readLock.lock();
						writeLock.unlock();
					}
				}
			}
		} finally {
			readLock.unlock();
		}
	}

	private void addBroadcastToQueues(Broadcast b){
		if (subscriptionMap.containsKey(b.getClass())) { // double check if anything has changed
			for (MicroService mics : subscriptionMap.get(b.getClass())) //for each microservice subscribed to the broadcast
				messageQueues.get(mics).add(b); // insert the message in the correct mics queue and notify it if it is waiting
		}
	}

	private void manageSubscriptions(Class<? extends Message> type, MicroService m, boolean roundRobinRequired){
		if (!subscriptionMap.containsKey(type)) { // check if event does not exist in submap - never been used
			subscriptionMap.put(type, new ArrayList<>()); // if so then create an entry for it
			subscriptionMap.get(type).add(m); // add the microservice to the submap at the specific event entry
			reverseSubscriptionMap.get(m).add(type); // also map the event in the reverse submap
			if (roundRobinRequired)
				indexList.put(type, new AtomicInteger(0));
		} else if (!subscriptionMap.get(type).contains(m)) { // check if the mics is registered to event already
			subscriptionMap.get(type).add(m); // add the mics to the event type map
			reverseSubscriptionMap.get(m).add(type);
		}
	}
}
