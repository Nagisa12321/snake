package com.struct;

/**
 * @author jtchen
 * @version 1.0
 * @date 2021/1/9 1:36
 */
public class SleepTime {
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        if (time > 0)
            this.time = time;
    }

    Integer time;

    public SleepTime(int time) {
        this.time = time;
    }


}
