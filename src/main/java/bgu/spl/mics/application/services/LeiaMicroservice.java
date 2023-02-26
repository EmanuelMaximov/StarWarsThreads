package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.Future;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link AttackEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvent}.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
    private Attack[] attacks;
    private MessageBusImpl msgB;

    public LeiaMicroservice(Attack[] attacks) {
        super("Leia");
        this.attacks = attacks;
        msgB = MessageBusImpl.getInstance();
    }

    @Override
    protected void initialize() {
        Diary d = Diary.getInstance();
        for (int i = 0; i < attacks.length; i++) {
            AttackEvent AT = new AttackEvent(attacks[i]);
            sendEvent(AT);
        }
        subscribeBroadcast(TerminateBrod.class, term -> {//ensures terminating "at once"
            terminate();
            d.setLeiaTerminate(System.currentTimeMillis());
        });
    }
}
