package com.example.springbatchexample.part6;

import java.util.Objects;

public enum Level {
    VIP(500_000,null),
    GOLD(500_000,VIP),
    SILVER(300_000,GOLD),
    NORMAL(200_000,SILVER);

    private final int nextAmount;

    private final Level nextLevel;

    private
    Level(int nextAmount, Level nextLevel){
        this.nextLevel = nextLevel;
        this.nextAmount = nextAmount;
    }

    public static boolean availableLevelUp(Level level, int totalAmount) {

            if(Objects.isNull(level)){
                return false;
            }
            if(Objects.isNull(level.nextLevel)){
                //nextLEvel이 null인건 VIP
                return false;
            }

            return totalAmount >= level.nextAmount;
            //사용자가 가진 금액이 level도달 금액보다
            // 많으면 true 아니면 false
    }

    public static Level getNextLevel(int totalAmount) {
        if( totalAmount >= Level.VIP.nextAmount){
            return VIP;
        }

        if(totalAmount >= Level.GOLD.nextAmount){
            return GOLD.nextLevel;
        }

        if(totalAmount >= Level.SILVER.nextAmount){
            return SILVER.nextLevel;
        }
        if(totalAmount >= Level.NORMAL.nextAmount){
            return NORMAL.nextLevel;
        }



        return NORMAL;
    }
}
