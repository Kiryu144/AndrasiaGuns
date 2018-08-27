package net.andrasia.kiryu144.andrasiaguns.guns;

import java.util.HashMap;

public class GunContainer {
    private HashMap<Integer, Gun> guns = new HashMap<>();
    private HashMap<String, Gun> names = new HashMap<>();

    public GunContainer() {

    }

    public void put(int id, Gun gun){
        guns.put(id, gun);
        names.put(gun.getDisplayName(), gun);
    }

    public Gun get(int id){
        return guns.get(id);
    }

    public Gun get(String name){
        return names.get(name);
    }
}
