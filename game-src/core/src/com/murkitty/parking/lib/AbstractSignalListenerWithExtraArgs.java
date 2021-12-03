package com.murkitty.parking.lib;

public abstract class AbstractSignalListenerWithExtraArgs<T,A> implements SignalListener<T> {
    private final A arg;

    public AbstractSignalListenerWithExtraArgs(A arg) {
        this.arg = arg;
    }

    @Override
    public void onSignal(T dispatched) {
        onSignalWithArg(dispatched, arg);
    }

    public abstract void onSignalWithArg(T dispatched, A arg);
}
