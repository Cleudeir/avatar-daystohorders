package com.avatar.avatar_7dayshorders.function;

public class MobWeave {
    private final String mobName;
    private final int quantity;
    private final int startWeave;
    private final int endWeave;

    public MobWeave(String mobName, int quantity, int startWeave, int endWeave) {
        this.mobName = mobName;
        this.quantity = quantity;
        this.startWeave = startWeave;
        this.endWeave = endWeave;
    }

    public String getMobName() {
        return mobName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getStartWeave() {
        return startWeave;
    }

    public int getEndWeave() {
        return endWeave;
    }
}
