package pers.wu.gobang.msg;

import java.io.Serializable;

public class Message implements Serializable {
    private boolean reply;

    public Message() {
    }

    public Message(boolean reply) {
        this.reply = reply;
    }

    public Message(String 悔棋) {
    }

    public boolean getMes() {
        return reply;
    }

    public void setMes(boolean reply) {
        this.reply = reply;
    }
}