package rendering.engine.shader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import core.resources.Destroyable;
import core.result.VulkanException;

public class Descriptor implements Destroyable{
    
    private int valuesCount;
    private DescriptorValue[] values;
    private Map<String, Integer> indexMap = Collections.synchronizedMap(new HashMap<String, Integer>());
    
    public Descriptor(DescriptorValue... values) {
        this.values = values;
        this.valuesCount = values.length;
        makeMap();
    }
    
    public Descriptor(DescriptorBlueprint blueprint) {
        this.values = blueprint.getDescriptorValues();
        this.valuesCount = values.length;
        makeMap();
    }
    
    private void makeMap() {
        
        for (int i = 0; i < values.length; i++)
            indexMap.put(values[i].name(), i);
    }
    
    public DescriptorValue getValue(int index) {
        return values[index];
    }
    
    public Integer getIndex(String name) {
        return indexMap.get(name);
    } 
    
    public DescriptorValue getValue(String name) {
        return getValue(getIndex(name));
    }
    
    public void update() throws VulkanException {
        for (int i = 0; i < valuesCount; i++)
            values[i].update();
    }
    
    public boolean isUpToDate() {
        for (int i = 0; i < valuesCount; i++)
            if (!values[i].isUpToDate())
                return false;
        return true;
    }

    @Override
    public void destroy() {
        for (int i = 0; i < valuesCount; i++)
            values[i].destroy();
    }
}
