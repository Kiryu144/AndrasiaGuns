package net.andrasia.kiryu144.andrasiaguns.guns;

import java.util.HashMap;
import java.util.SplittableRandom;

public class BulletDatabase {
    private static HashMap<Integer, BulletData> bulletDatabase = new HashMap<>();
    private static SplittableRandom random = new SplittableRandom(System.currentTimeMillis());

    public static int addBullet(BulletData bullet){
        int id = random.nextInt();
        bulletDatabase.put(id, bullet);
        return id;
    }

    public static BulletData getBullet(int id){
        return bulletDatabase.get(id);
    }

    public static void removeBullet(int id){
        bulletDatabase.remove(id);
    }
}
