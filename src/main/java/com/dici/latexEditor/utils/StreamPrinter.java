package com.dici.latexEditor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class StreamPrinter implements Runnable {
	private final InputStream		inputStream;
	private final Consumer<String>	consumer;

	public StreamPrinter(InputStream inputStream) {
        this(inputStream,System.out::println);
    }
	
    public StreamPrinter(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer    = consumer;
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = br.readLine()) != null) 
            	consumer.accept(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}