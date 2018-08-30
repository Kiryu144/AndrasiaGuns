package net.andrasia.kiryu144.andrasiaguns.guns;

public class GunPlayerData {
    private long lastShot = 0;
    private long timeoutStart = 0;
    private long timeout = 0;

    private int ammoLeft = 0;
    private int totalAmmoLeft = 0;

    public GunPlayerData() {
        lastShot = 0;
        timeoutStart = 0;
        timeout = 0;
        ammoLeft = 0;
    }

    public long getLastShot() {
        return lastShot;
    }

    public void setLastShot(long lastShot) {
        this.lastShot = lastShot;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getAmmoLeft() {
        return ammoLeft;
    }

    public void setAmmoLeft(int ammoLeft) {
        this.ammoLeft = ammoLeft;
    }

    public long getTimeoutStart() {
        return timeoutStart;
    }

    public void setTimeoutStart(long timeoutStart) {
        this.timeoutStart = timeoutStart;
    }

    public void startTimeout(){
        this.timeoutStart = System.currentTimeMillis();
    }

    public boolean isInTimeout(){
        return (System.currentTimeMillis() - timeoutStart) < timeout;
    }

    public int getTotalAmmoLeft() {
        return totalAmmoLeft;
    }

    public void setTotalAmmoLeft(int totalAmmoLeft) {
        this.totalAmmoLeft = totalAmmoLeft;
    }
}
