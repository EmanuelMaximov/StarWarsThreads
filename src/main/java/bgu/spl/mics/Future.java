package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * <p>
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
    private boolean isDone;
    private T result;
    private final Object result_lock = new Object();

    /**
     * This should be the the only public constructor in this class.
     */
    public Future() {
        result = null;
    }

    /**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     *
     * @return return the result of type T if it is available, if not wait until it is available.
     */
    public T get() {
        if (!isDone()) {
            try {
                synchronized (result_lock) {
                    result_lock.wait();
                }
            } catch (InterruptedException excepion) {
                excepion.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Resolves the result of this Future object.
     */
    public void resolve(T result) {
        synchronized (result_lock) {
            this.result = result;
            result_lock.notify();
        }
    }

    /**
     * @return true if this object has been resolved, false otherwise
     */
    public boolean isDone() {
        isDone = (result != null);
        return isDone;
    }

    /**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     *
     * @param timeout the maximal amount of time units to wait for the result.
     * @param unit    the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not,
     * wait for {@code timeout} TimeUnits {@code unit}. If time has
     * elapsed, return null.
     */
    public T get(long timeout, TimeUnit unit) {
        if (isDone()) return result;
        try {
            synchronized (result_lock) {
                result_lock.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
            }
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        return result;
    }

}
