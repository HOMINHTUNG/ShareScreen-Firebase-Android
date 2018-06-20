package com.hominhtung.seenscreen.object;

/**
 * Created by HOMINHTUNG-PC on 3/29/2018.
 */

public class driverItem {
    private String token;
    private String nameDevice;

    public driverItem(String token, String nameDevice){
        this.token = token;
        this.nameDevice = nameDevice;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNameDevice() {
        return nameDevice;
    }

    public void setNameDevice(String nameDevice) {
        this.nameDevice = nameDevice;
    }
}

