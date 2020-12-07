package bgu.spl.mics;

import javax.jws.Oneway;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	private static MessageBusImpl messageBusInstance = null;
	private ConcurrentHashMap<Class<? extends MicroService>, Queue<Message>> messageQueues;
	private ConcurrentHashMap<Class<? extends Message>, HashSet<Class<? extends MicroService>>> subscriptionMap;
	private HashMap<Event, Future> futures;
	private final Object regLock;
	private final Object subLock;
	private int regVersion;
	private int subVersion;

	private MessageBusImpl(){
		messageQueues =  new ConcurrentHashMap<>();
		subscriptionMap = new ConcurrentHashMap<>();
		futures = new HashMap<>();
		regLock = new Object();
		subLock = new Object();
		regVersion = 0;
		subVersion = 0;
	}

	public static MessageBusImpl getInstance(){
		if (messageBusInstance==null){
			synchronized (MessageBusImpl.class) { //synchronize while using Class object as monitor
				if(messageBusInstance==null)
					messageBusInstance = new MessageBusImpl();
			}
		}
		return messageBusInstance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// check if already registered
		int currVersion = subVersion;
		if(isRegistered(m)) {

			//TODO --Critical section-- m might unregister - sync messagequeues
			if (!subscriptionMap.containsKey(type)){
				// TODO --Critical section-- subscription cannot be empty
				subscriptionMap.get(type).add(m.getClass());
				// TODO --END--
			}
			else if(!isSubscribedTo(m, type)){ //check if mics registered to event already
				subscriptionMap.get(type).add(m.getClass());
			}
			//TODO --END--
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if(isRegistered(m)) {
			//TODO --Critical section-- m might unregister
			if (!eventTypeActive(type)){
				// TODO --Critical section-- subscription cannot be empty
				subscriptionMap.get(type).add(m.getClass());
				// TODO --END--
			}
			else if(!isSubscribedTo(m, type)){ //check if mics registered to event already
				subscriptionMap.get(type).add(m.getClass());
			}
			//TODO --END--
		}
	}

	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
		// resolve the associated future with result
		futures.get(e).resolve(result); // TODO maybe notify sender?

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// check if any service is subscribed to the event TODO permission - R level - subscriptions also manage map( no subscribers delete key)
		if(eventTypeActive(b.getClass())){
			// sync for entire message queue? TODO
			for(Class mics : subscriptionMap.get(b.getClass()))
				messageQueues.get(mics).add(b);
		}
		// send the event to any registered queue TODO permission - R/W level - specific queues

	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// check if any service is subscribed to the event TODO permission - R level - subscriptions
		// create a new future and associate the future with the event TODO permission - R/W level  futures (?)
		// send the event to any registered queue using the round robin TODO permission - R/W level - specific queues
		if (eventTypeActive(e.getClass())) {
			Future<T> future = new Future<>();
			futures.put(e, future);
			roundRobinSend(e);
		}
		else {
			//TODO wait for subscriber to come?
		}
        return null;
	}

	@Override
	public void register(MicroService m) {
		boolean changed = false;
		do {
			// check if change happened while reading
			int currVersion = regVersion;
			// check if m not in queues
			if (!isRegistered(m)) {
				synchronized (regLock) {
					if (currVersion == regVersion) {
						// make a queue for the mics m
						messageQueues.put(m.getClass(), new PriorityQueue<>());
						changed = true;
						regVersion++;
						subVersion++;
					}
				}
			}
		} while (!changed);
	}

	@Override
	public void unregister(MicroService m) {
		// check if m in queues
		// delete the queue for mics m and all references for it (subscriptions) TODO permission - R/W level - message queues
//		HashSet<Class<? extends Message>> subscriptions = subscriptionMap.get(m.getClass());
		messageQueues.remove(m.getClass());
		// for each subscription the mics had, go to the subscription map and delete the reference to the mics //TODO permission - R/W level - subscriptions


		
	}

	private boolean isSubscribedTo(MicroService m, Class<? extends Message>  type) {
		return subscriptionMap.get(type).contains(m.getClass());
	}

	private void unsubscribe(MicroService m, Class<? extends Message> type){
		// TODO --Critical Section-- must keep invariant
		subscriptionMap.get(type).remove(m.getClass());
		if(subscriptionMap.get(type).isEmpty())
			subscriptionMap.remove(type);
		// TODO --END--
	}


	private boolean isRegistered(MicroService m) {
		return messageQueues.containsKey(m.getClass());
	}

	private boolean eventTypeActive(Class<? extends Message> type){
		return subscriptionMap.containsKey(type); // invariant - if event type exists - it has a subscriber
	}

	private <T> void roundRobinSend(Event<T> e){
		//TODO implement RR
	}


	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO make blocking
		// attempt to retrieve a message from m's queue
		// use the callback
		return null;
	}
}
