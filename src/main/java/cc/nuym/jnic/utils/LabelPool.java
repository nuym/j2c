// rebuild
package cc.nuym.jnic.utils;

import org.objectweb.asm.Label;

import java.util.WeakHashMap;

public class LabelPool {
    private final WeakHashMap<Label, Long> labels = new WeakHashMap<>();
    private long currentIndex = 0L;

    public String getName(Label label) {
        return "L" + this.labels.computeIfAbsent(label, addedLabel -> ++this.currentIndex);
    }
}
