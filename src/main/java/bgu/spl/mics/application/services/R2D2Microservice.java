package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminateBrod;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.concurrent.CountDownLatch;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {
    private long duration;
    private CountDownLatch countDownLatch;

    public R2D2Microservice(CountDownLatch count, long duration) {
        super("R2D2");
        this.duration = duration;
        countDownLatch = count;
    }

    @Override
    protected void initialize() {
        MessageBusImpl msgB = MessageBusImpl.getInstance();
        msgB.subscribeEvent(DeactivationEvent.class, this);
        Diary d = Diary.getInstance();
        Callback<DeactivationEvent> c = (DeactivationEvent e) -> {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            d.setR2D2Deactivate(System.currentTimeMillis());
            Future<Boolean> f = sendEvent(new BombDestroyerEvent());//"Notifies" Lando to start
            f.get();
            complete(e, true);
        };
        subscribeEvent(DeactivationEvent.class, c);
        subscribeBroadcast(TerminateBrod.class, term -> {//ensures all this type MS terminate at the end "at once"
            terminate();
            d.setR2D2Terminate(System.currentTimeMillis());
        });
        countDownLatch.countDown();

    }
}

