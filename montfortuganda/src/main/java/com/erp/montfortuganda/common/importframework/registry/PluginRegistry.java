package com.erp.montfortuganda.common.importframework.registry;

import com.erp.montfortuganda.common.importframework.plugin.ImportPlugin;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PluginRegistry {

    private final List<ImportPlugin<?>> plugins;
    private final ImportManifestRegistry manifestRegistry;
    
    private final Map<String, ImportPlugin<?>> pluginMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void validateAndRegister() {
        log.info("Starting Enterprise Bulk Import Plugin Validation...");
        
        for (ImportPlugin<?> plugin : plugins) {
            validatePlugin(plugin);
            
            String moduleName = plugin.getManifest().getModuleName().toUpperCase();
            if (pluginMap.containsKey(moduleName)) {
                throw new IllegalStateException("Duplicate Import Plugin found for module: " + moduleName);
            }
            
            pluginMap.put(moduleName, plugin);
            manifestRegistry.registerManifest(plugin.getManifest());
            
            log.info("Successfully validated and registered Import Plugin for module: {}", moduleName);
        }
        
        log.info("Import Plugin Validation completed. {} plugins active.", pluginMap.size());
    }

    private void validatePlugin(ImportPlugin<?> plugin) {
        if (plugin.getManifest() == null) {
            throw new IllegalStateException("Plugin " + plugin.getClass().getSimpleName() + " is missing a Manifest");
        }
        if (plugin.getCapabilities() == null) {
            throw new IllegalStateException("Plugin " + plugin.getClass().getSimpleName() + " is missing Capabilities");
        }
        if (plugin.getStrategies() == null) {
            throw new IllegalStateException("Plugin " + plugin.getClass().getSimpleName() + " is missing Strategies");
        }
        if (plugin.getRowMapper() == null) {
            throw new IllegalStateException("Plugin " + plugin.getClass().getSimpleName() + " is missing RowMapper");
        }
        if (plugin.getValidator() == null) {
            throw new IllegalStateException("Plugin " + plugin.getClass().getSimpleName() + " is missing Validator");
        }
        if (plugin.getProcessor() == null) {
            throw new IllegalStateException("Plugin " + plugin.getClass().getSimpleName() + " is missing Processor");
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> ImportPlugin<T> getPlugin(String moduleName) {
        ImportPlugin<?> plugin = pluginMap.get(moduleName.toUpperCase());
        if (plugin == null) {
            throw new IllegalArgumentException("No Import Plugin registered for module: " + moduleName);
        }
        return (ImportPlugin<T>) plugin;
    }
}
