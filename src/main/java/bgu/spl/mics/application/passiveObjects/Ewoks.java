package bgu.spl.mics.application.passiveObjects;


import java.util.ArrayList;
import java.util.Arrays;


/**
 * Passive object representing the resource manager.
 * <p>
 * This class must be implemented as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
//"You can add to this class members and methods as you see right."
public class Ewoks {
    ArrayList<Ewok> list;

    private Ewoks() {}//empty constructor

    private static class EwoksHolder {//singleton
        private static Ewoks instance = new Ewoks();
    }

    public static Ewoks getInstance() {//singleton
        return EwoksHolder.instance;
    }

    /**
     * initialize the Ewoks list
     */
    public void init(int count) {
        list = new ArrayList<Ewok>();
        for (int i = 1; i <= count; i++) {
            list.add(new Ewok(i, true));
        }
    }
    /**
     * acquires the relevant Ewoks
     */
    public void acquire(int[] serials) {
        Arrays.sort(serials);//Determines the order of acquiring the shared resource - Ewok
        for (int serial : serials)
        {
            synchronized (list.get(serial))
            {
                if (!list.get(serial).getAvailable())
                {
                    try {
                        list.get(serial).wait();

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                list.get(serial).acquire();

            }
        }

    }
    /**
     * releases the relevant Ewoks when MS finishes
     */
    public void release(int[] serials) {
        for (int serial : serials)
        {
            synchronized (list.get(serial))
            {
                list.get(serial).release();
                list.get(serial).notify();
            }
        }

    }

}






