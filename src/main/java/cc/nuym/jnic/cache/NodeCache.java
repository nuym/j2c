package cc.nuym.jnic.cache;

import java.util.HashMap;
import java.util.Map;

public class NodeCache<T>
{
    private final Map<T, Integer> cache;
    
    public NodeCache() {
        this.cache = new HashMap<>();
    }
    
    public void getPointer(final T key) {
        this.getId(key);
    }
    
    public int getId(final T key) {
        if (!this.cache.containsKey(key)) {
            this.cache.put(key, this.cache.size());
        }
        return this.cache.get(key);
    }
    
    public int size() {
        return this.cache.size();
    }
    
    public boolean isEmpty() {
        return this.cache.isEmpty();
    }
    
    public Map<T, Integer> getCache() {
        return this.cache;
    }
    
    public void clear() {
        this.cache.clear();
    }
}
