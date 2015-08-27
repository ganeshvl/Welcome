package com.entradahealth.entrada.android.app.personal.activities.job_display;

public class StopWatch{  

private long startTime = 0;
private long stopTime = 0;
private boolean running = false;

public StopWatch() {
	// TODO Auto-generated constructor stub
	this.running = false;
	this.startTime = 0;
	this.stopTime = 0;
}

public void start() {
    this.startTime = System.currentTimeMillis();
    this.running = true;
}


public void stop() {
    this.running = false;
    this.stopTime = 0;
    this.startTime = 0;
}


//elaspsed time in milliseconds
public long getElapsedTime() {
    long elapsed;
    if (running) {
         elapsed = (System.currentTimeMillis() - startTime);
    	
    }
    else {
        elapsed = (stopTime - startTime);
    }
    return elapsed;
}


//elaspsed time in seconds
public long getElapsedTimeSecs() {
    long elapsed;
    if (running) {
        elapsed = ((System.currentTimeMillis() - startTime) / 1000);
    }else {
    	elapsed = 0;
    }
    return elapsed;
}




//sample usage
public static void main(String[] args) {
    StopWatch s = new StopWatch();
    s.start();
    //code you want to time goes here
    s.stop();
    System.out.println("elapsed time in milliseconds: " + s.getElapsedTime());
}
}
