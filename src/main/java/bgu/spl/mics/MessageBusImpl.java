package bgu.spl.mics;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
    //ConcurrentHashMap class is thread-safe
    private ConcurrentHashMap<Class<? extends Event<?>>, Vector<MicroService>> EventMap;//For each event we have a list of MS
    private ConcurrentHashMap<Class<? extends Broadcast>, Vector<MicroService>> BroadcastMap;//For each broadcast we have a list of MS
    private ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceQueueHashMap; //For each MS we have a Queue
    private ConcurrentHashMap<Event<?>, Future> events;//For each Event we have a Future


    private MessageBusImpl() { //constructor
        EventMap = new ConcurrentHashMap<>();
        BroadcastMap = new ConcurrentHashMap<>();
        microServiceQueueHashMap = new ConcurrentHashMap<>();
        events = new ConcurrentHashMap<>();
    }

    //singleton:
    private static class MessageBusImplHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }

    public static MessageBusImpl getInstance() {
        return MessageBusImplHolder.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        synchronized (EventMap) {
            if (!EventMap.containsKey(type)) { //if the event is not found
                Vector<MicroService> l = new Vector<>();
                l.add(m);
                EventMap.put(type, l);
            } else {
                Vector<MicroService> l = EventMap.get(type);
                l.add(m); //adding the MS to the right list
            }
        }
    }


    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        synchronized (BroadcastMap) {
            if (!BroadcastMap.containsKey(type)) { //the broadcast is not found
                Vector<MicroService> l = new Vector<>();
                l.add(m);
                BroadcastMap.put(type, l);
            } else {
                BroadcastMap.get(type).add(m); //adding the MS to the right list
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void complete(Event<T> e, T result) {
        Future<T> future = null;
        synchronized (events) {//for the case when number of MS completing at the same time
            if (events.containsKey(e)) {
                future = (Future<T>) events.get((Message) e);
                events.remove((Message) e);
            }
        }
        if (future != null) {
            future.resolve(result);
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        if (BroadcastMap.get(b.getClass()) != null) {
            for (MicroService m : BroadcastMap.get(b.getClass())) {
                try {
                    microServiceQueueHashMap.get(m).put(b);//add the broadcast to the queue of all the MS which subscribed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    @Override
    public synchronized <T> Future<T> sendEvent(Event<T> e) {
        if (EventMap.get(e.getClass()) == null) {
            return null;
        }
        Future<T> future = new Future<>();
        events.put(e, future);
        Vector<MicroService> l = EventMap.get(e.getClass());//list of MS relevent to this event
        MicroService m;
        synchronized (l) {
            m = l.remove(0);
            l.add(m);//round-robin manner
        }
        microServiceQueueHashMap.get(m).add(e);
        return future;
    }

    @Override
    public void register(MicroService m) {
        microServiceQueueHashMap.put(m, new LinkedBlockingQueue<>());
    }


    @Override
    public synchronized void unregister(MicroService m) {
        for (Message m1 : microServiceQueueHashMap.get(m)) {
            if (m1 instanceof Event)
                EventMap.get(m1).remove(m);
            else
                BroadcastMap.get(m1).remove(m);
        }
        microServiceQueueHashMap.remove(m);

    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        BlockingQueue<Message> q = microServiceQueueHashMap.get(m);
        Message message = null;
        if (q == null) // if the microService doesnt have a queue
            return null;
        synchronized (q) {
            message = q.take();//pops the head of the queue when available
        }
        return message;
    }
}
