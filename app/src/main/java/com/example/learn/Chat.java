package com.example.learn;

public class Chat {
    Chat(String textc, int idc)
    {
        text=textc;
        side = idc;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }

    String text;
 int side;
}
