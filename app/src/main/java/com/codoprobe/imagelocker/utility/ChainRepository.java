package com.codoprobe.imagelocker.utility;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChainRepository {

    private static ChainRepository INSTANCE;
    private ArrayList<BlockChain.Chain> chains;

    private List<Listener> listeners = new ArrayList<>();

    private ChainRepository(Context ctx) throws IOException {
        chains = BlockChain.Chain.Loader
                .load(KeyStore.getEncryptionKey(ctx));
    }

    public static ChainRepository getInstance(Context ctx) {
        if(INSTANCE == null) {
            try {
                INSTANCE = new ChainRepository(ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return INSTANCE;
    }

    // getters and setters

    public ArrayList<BlockChain.Chain> getChains() {
        return chains;
    }

    /**
     * deletes chain in the same way
     * @param i index of the chain
     */
    public void deleteChain(int i) {
        chains.get(i).delete();
        chains.remove(i);
        for (Listener listener : listeners) {
            listener.onChange("REMOVE", i);
        }
    }

    public void addChain(BlockChain.Chain chain) {
        chains.add(chain);
        for (Listener listener : listeners) {
            listener.onChange("ADD", chains.size()-1);
        }
    }

    public interface Listener{
        /**
         * runs when changed
         * @param type ADD or REMOVE
         * @param index data index
         */
        void onChange(String type, int index);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

}