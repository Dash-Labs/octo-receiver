package com.dashlabs.octoreceiver.model;

/**
 * Created by mpuri on 6/24/14.
 */
public class ProcessResult {

    private final StringBuilder logs;

    private final int result;

    public ProcessResult(StringBuilder logs, int result) {
        this.logs = logs;
        this.result = result;
    }

    public StringBuilder getLogs() {
        return logs;
    }

    public int getResult() {
        return result;
    }
}
