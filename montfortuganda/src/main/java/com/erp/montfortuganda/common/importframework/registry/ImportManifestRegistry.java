package com.erp.montfortuganda.common.importframework.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImportManifestRegistry {
    
    private final Map<String, ModuleManifest> registry = new ConcurrentHashMap<>();

    public void registerManifest(ModuleManifest manifest) {
        registry.put(manifest.getModuleName().toUpperCase(), manifest);
    }

    public ModuleManifest getManifest(String moduleName) {
        return registry.get(moduleName.toUpperCase());
    }
    
    public Map<String, ModuleManifest> getAllManifests() {
        return Map.copyOf(registry);
    }
}
