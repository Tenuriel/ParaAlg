/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Tim Pontzen
 */
public class Pair<K,V> {
    /**
     * key of this pair.
     */
    private K key;
    /**
     * value of htis pair
     */
    private V value;
    
    public Pair(K key,V value){
        this.key=key;
        this.value=value;
    }

    /**
     * key of this pair.
     * @return the key
     */
    public K getKey() {
        return key;
    }

    /**
     * key of this pair.
     * @param key the key to set
     */
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * value of htis pair
     * @return the value
     */
    public V getValue() {
        return value;
    }

    /**
     * value of htis pair
     * @param value the value to set
     */
    public void setValue(V value) {
        this.value = value;
    }
}
