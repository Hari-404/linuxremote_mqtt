package com.example.linuxremote;

import java.util.ArrayList;

public interface SendData {
    void send(ArrayList<String> s);
    void connectToService(String ip);
    void removeItem(int position);
}
