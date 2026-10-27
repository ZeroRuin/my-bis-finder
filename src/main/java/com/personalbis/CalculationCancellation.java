package com.personalbis;

import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;

/** Cooperative cancellation for the existing single planning worker. No game actions. */
final class CalculationCancellation
{
    private static final ThreadLocal<BooleanSupplier> CURRENT=new ThreadLocal<>();
    private CalculationCancellation() {}
    static void check()
    {
        BooleanSupplier current=CURRENT.get();
        if(Thread.currentThread().isInterrupted()||(current!=null&&!current.getAsBoolean()))
            throw new CancellationException("Obsolete calculation");
    }
    static void runWhile(BooleanSupplier current,Runnable work)
    {
        CURRENT.set(current);
        try {check();work.run();}
        catch(CancellationException cancelled) { /* Superseded by player input. */ }
        finally {CURRENT.remove();}
    }
}
