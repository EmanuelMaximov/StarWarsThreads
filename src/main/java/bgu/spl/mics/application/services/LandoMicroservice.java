package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminateBrod;
import bgu.spl.mics.application.passiveObjects.Diary;

import java.util.concurrent.CountDownLatch;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice extends MicroService {
    private long duration;
    private CountDownLatch countDownLatch;

    public LandoMicroservice(CountDownLatch count, long duration) {
        super("Lando");
        this.duration = duration;
        countDownLatch = count;
    }

    @Override
    protected void initialize() {
        MessageBusImpl msgB = MessageBusImpl.getInstance();
        msgB.subscribeEvent(BombDestroyerEvent.class, this);
        Diary d = Diary.getInstance();
        Callback<BombDestroyerEvent> c = (BombDestroyerEvent e) -> {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            complete(e, true);
            sendBroadcast(new TerminateBrod());//"Notifies" all MS to terminate
        };
        subscribeEvent(BombDestroyerEvent.class, c);
        subscribeBroadcast(TerminateBrod.class, term -> {//ensures all this type MS terminate at the end "at once"
            terminate();
            d.setLandoTerminate(System.currentTimeMillis());
        });
        countDownLatch.countDown();
    }
}
