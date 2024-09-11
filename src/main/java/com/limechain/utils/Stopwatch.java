package com.limechain.utils;

public class Stopwatch {

    private long startTime;

    public Stopwatch() {
        reset();
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}
