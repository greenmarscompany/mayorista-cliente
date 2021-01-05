package com.greenmarscompany.mayoristacliente.utils;

public class Temporizador extends Thread {

    public static int nuMin = 0;
    public static int numSeg = 0;
    public static int numHor = 0;

    public Temporizador() {
        super();
    }

    public void run() {
        try {

            for (; ; ) {
                if (numSeg != 0) {
                    numSeg--;
                } else {
                    if (nuMin != 0) {
                        numSeg = 59;
                        nuMin--;
                    } else {
                        if (numHor != 0) {
                            numHor--;
                            nuMin = 59;
                            numSeg = 59;
                        } else {
                            break;
                        }
                    }
                }
            }
            sleep(998);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
