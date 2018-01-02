package com.ah.xiny3.Util;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Cache {
    private final Context c;

    public Cache(Context c) {
        this.c = c;
    }

    @SuppressWarnings("SameReturnValue")
    public void writeToCache(String location, byte[] bytes) {
        Runnable w = () -> {
            try{
                File file = new File(c.getCacheDir(), location);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();
            } catch (IOException e){
                // do nothing
            }
        };
        AsyncTask.execute(w);
    }

    public String readFromCache(String location) {
        StringBuilder buffer = new StringBuilder();
        try {
            File file = new File(c.getCacheDir(), location);
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = input.readLine()) != null) {
                buffer.append(line).append(System.lineSeparator());
            }
            return buffer.toString();
        } catch (IOException e) {
            return "fail"; // no language will be fail lol
        }
    }
}
