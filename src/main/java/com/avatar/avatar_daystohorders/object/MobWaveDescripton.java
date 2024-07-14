package com.avatar.avatar_daystohorders.object;

public class MobWaveDescripton {
    private final String mobName;
    private final int quantity;
    private final int startwave;
    private final int endwave;

    public MobWaveDescripton(String mobName, int quantity, int startwave, int endwave) {
        this.mobName = mobName;
        this.quantity = quantity;
        this.startwave = startwave;
        this.endwave = endwave;
    }

    public String getMobName() {
        return mobName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getStartwave() {
        return startwave;
    }

    public int getEndwave() {
        return endwave;
    }
}
