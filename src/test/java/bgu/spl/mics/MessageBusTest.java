package bgu.spl.mics;

import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminateBrod;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.services.C3POMicroservice;
import bgu.spl.mics.application.services.HanSoloMicroservice;
import org.junit.jupiter.api.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;


public class MessageBusTest {
    private TerminateBrod terminated;
    private MessageBus msgBus;
    private MicroService C3PO;
    private MicroService Hansolo;
    private AttackEvent AttackEvent;
    private AttackEvent otherAttackEvent;
    private CountDownLatch count;
    private Attack attack;


    @BeforeEach
    public void setUp() {
        msgBus = MessageBusImpl.getInstance();
        count = new CountDownLatch(0);
        C3PO = new C3POMicroservice(count);
        Hansolo = new HanSoloMicroservice(count);
        AttackEvent = new AttackEvent(attack);
        terminated = new TerminateBrod();
        List<Integer> list = new ArrayList<>();
        attack = new Attack(list, 0);

    }

    @Test
    public void testComplete() {
        //tests SubscribeEvemt & SendEvent & AwaitMessage
        msgBus.register(C3PO);
        msgBus.subscribeEvent(AttackEvent.getClass(), C3PO);
        Future<Boolean> f = msgBus.sendEvent(AttackEvent);
        assertFalse(f.isDone());
        assertNotNull(f);
        Message m;
        try {
            m = msgBus.awaitMessage(C3PO);
            msgBus.complete((AttackEvent) m, true);
            assertEquals(AttackEvent, m);
            assertTrue(f.isDone());
            assertTrue(f.get());
        } catch (Exception e) {
            assert false;
        }
        msgBus.unregister(C3PO);
    }

    @Test
    public void testSendBroadcast() {
        //tests the subscribeBroadcast too
        //tests AwaitMessage too
        msgBus.register(C3PO);
        msgBus.register(Hansolo);
        msgBus.subscribeBroadcast(terminated.getClass(), C3PO);
        msgBus.subscribeBroadcast(terminated.getClass(), Hansolo);
        msgBus.sendBroadcast(terminated);
        Message m;
        try {
            m = msgBus.awaitMessage(C3PO);
            assertEquals(m, terminated);
            m = msgBus.awaitMessage(Hansolo);
            assertEquals(m, terminated);
        } catch (Exception e) {
            assert false;
        }
        msgBus.unregister(C3PO);
        msgBus.unregister(Hansolo);
    }

    @Test
    public void testRegister() {
        //no need to test
    }


    @Test
    public void testSubscribeEvent() {
        //no need to test
    }

    @Test
    public void testSubscribeBroadcast() {
        //no need to test
    }

    @Test
    public void testSendEvent() {
        //no need to test
    }


    @Test
    public void testAwaitMessage() {
        //no need to test
    }


}
