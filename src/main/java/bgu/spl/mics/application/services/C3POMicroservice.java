package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminateBrod;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.concurrent.CountDownLatch;


/**
 * C3POMicroservices is in charge of the handling {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {
    private CountDownLatch countDownLatch;

    public C3POMicroservice(CountDownLatch count) {
        super("C3PO");
        countDownLatch = count;
    }

    @Override
    protected void initialize() {
        MessageBusImpl msgB = MessageBusImpl.getInstance();
        msgB.subscribeEvent(AttackEvent.class, this);
        Diary d = Diary.getInstance();
        Callback<AttackEvent> c = (AttackEvent e) -> {
            Ewoks ewoks = Ewoks.getInstance();
            int[] array = new int[e.getAttack().getSerials().size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = e.getAttack().getSerials().get(i);//taking the serials of the Ewoks for the attack
            }
            ewoks.acquire(array);
            try {
                Thread.sleep(e.getAttack().getDuration());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            ewoks.release(array);
            d.incrementTotalAttack();
            d.setC3POFinish(System.currentTimeMillis());
            if (d.getTotalAttack().get() == d.getExpectedAttacks()) {//happens in the last attack
                Future<Boolean> f = sendEvent(new DeactivationEvent());
                f.get();
            }
            complete(e, true);

        };
        subscribeEvent(AttackEvent.class, c);
        subscribeBroadcast(TerminateBrod.class, term -> {//ensures all this type MS terminate at the end "at once"
            d.setC3POTerminate(System.currentTimeMillis());
            terminate();
        });
        countDownLatch.countDown();
    }
}

