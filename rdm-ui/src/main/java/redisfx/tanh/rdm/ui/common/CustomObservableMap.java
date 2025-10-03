package redisfx.tanh.rdm.ui.common;

import com.sun.javafx.collections.ObservableMapWrapper;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.Map;

public class CustomObservableMap<K, V> extends ObservableMapWrapper<K, V> implements ObservableMap<K, V> {
    private final Map<K, V> backingMap0;


    public static <K, V> CustomObservableMap<K, V> simpleMap() {
       return new CustomObservableMap<K, V>(new HashMap<>());
    }

    public CustomObservableMap(Map<K, V> map) {
        super(map);
        this.backingMap0 = map;
    }


    public V put(K var1, V var2) {
        V var3;
        if (this.backingMap0.containsKey(var1)) {
            var3 = this.backingMap0.put(var1, var2);
            if (var3 == null && var2 != null || var3 != null && !var3.equals(var2)) {
                this.callObservers(new SimpleChange(var1, var3, var2, true, true));
            }else {
                this.callObservers(new SimpleChange(var1, var3, var2, true, true));
            }
        } else {
            var3 = this.backingMap0.put(var1, var2);
            this.callObservers(new SimpleChange(var1, var3, var2, true, false));
        }

        return var3;
    }




    private class SimpleChange extends MapChangeListener.Change<K, V> {
        private final K key;
        private final V old;
        private final V added;
        private final boolean wasAdded;
        private final boolean wasRemoved;

        public SimpleChange(K var2, V var3, V var4, boolean var5, boolean var6) {
            super(CustomObservableMap.this);

            assert var5 || var6;

            this.key = var2;
            this.old = var3;
            this.added = var4;
            this.wasAdded = var5;
            this.wasRemoved = var6;
        }

        public boolean wasAdded() {
            return this.wasAdded;
        }

        public boolean wasRemoved() {
            return this.wasRemoved;
        }

        public K getKey() {
            return this.key;
        }

        public V getValueAdded() {
            return this.added;
        }

        public V getValueRemoved() {
            return this.old;
        }

        public String toString() {
            StringBuilder var1 = new StringBuilder();
            if (this.wasAdded) {
                if (this.wasRemoved) {
                    var1.append(this.old).append(" replaced by ").append(this.added);
                } else {
                    var1.append(this.added).append(" added");
                }
            } else {
                var1.append(this.old).append(" removed");
            }

            var1.append(" at key ").append(this.key);
            return var1.toString();
        }
    }
}
