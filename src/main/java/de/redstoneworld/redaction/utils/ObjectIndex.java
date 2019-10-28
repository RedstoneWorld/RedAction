package de.redstoneworld.redaction.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class ObjectIndex<T> {

    private final Map<Object, Object> map = new HashMap<>();
    private Map<Class<?>, BiFunction<Object, Object, Boolean>> matchers = new LinkedHashMap<>();

    public ObjectIndex(Collection<T> objects, Class<T> clazz) {
        List<Field> selectorFields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Index.class)) {
                field.setAccessible(true);
                selectorFields.add(field);
            }
        }
        if (selectorFields.isEmpty()) {
            throw new IllegalArgumentException("The class " + clazz.getSimpleName() + " does not have any field specified as an Index!");
        }
        for (T object : objects) {
            Collection<Map<Object, Object>> currentMaps = Collections.singleton(map);
            try {
                for (int i = 0; i < selectorFields.size(); i++) {
                    Field field = selectorFields.get(i);
                    Object value = field.get(object);
                    Collection<Map<Object, Object>> newCurrentMaps = new ArrayList<>();
                    if (value instanceof Collection) {
                        if (((Collection) value).isEmpty()) {
                            newCurrentMaps.addAll(addToMaps(currentMaps, null, object, selectorFields.size() == i + 1));
                        } else {
                            for (Object key : (Collection<Object>) value) {
                                newCurrentMaps.addAll(addToMaps(currentMaps, key, object, selectorFields.size() == i + 1));
                            }
                        }
                    } else if (value instanceof Map) {
                        if (((Map) value).isEmpty()) {
                            Collection<Map<Object, Object>> newMaps = new ArrayList<>();
                            for (Map<Object, Object> currentMap : currentMaps) {
                                newMaps.add((Map<Object, Object>) currentMap.computeIfAbsent(null, v -> new HashMap()));
                            }
                            newCurrentMaps.addAll(addToMaps(newMaps, null, object, selectorFields.size() == i + 1));
                        } else {
                            for (Map.Entry<Object, Object> e : ((Map<Object, Object>) value).entrySet()) {
                                Collection<Map<Object, Object>> newMaps = new ArrayList<>();
                                for (Map<Object, Object> currentMap : currentMaps) {
                                    newMaps.add((Map<Object, Object>) currentMap.computeIfAbsent(e.getKey(), v -> new HashMap()));
                                }
                                newCurrentMaps.addAll(addToMaps(newMaps, e.getValue(), object, selectorFields.size() == i + 1));
                            }
                        }
                    } else {
                        if (value instanceof Integer && (int) value == -1) {
                            value = null;
                        }
                        newCurrentMaps.addAll(addToMaps(currentMaps, value, object, selectorFields.size() == i + 1));
                    }
                    currentMaps = newCurrentMaps;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Collection<Map<Object, Object>> addToMaps(Collection<Map<Object, Object>> maps, Object key, T object, boolean last) {
        if (last) {
            for (Map<Object, Object> currentMap : maps) {
                List<T> col = (List<T>) currentMap.computeIfAbsent(key, v -> new ArrayList<T>());
                col.add(object);
            }
            return Collections.emptyList();
        }
        Collection<Map<Object, Object>> newCurrentMaps = new ArrayList<>();
        for (Map<Object, Object> currentMap : maps) {
            newCurrentMaps.add((Map<Object, Object>) currentMap.computeIfAbsent(key, v -> new HashMap()));
        }
        return newCurrentMaps;
    }

    /**
     * Register a custom matcher for a class type. This will be used for getting the matching step.
     * Please note that matchers need to loop through all keys of the layer instead of being able to take advantage of HashMap indexing!
     * @param clazz     The class
     * @param matcher   The matcher
     */
    public <M> void registerMatcher(Class<M> clazz, BiFunction<M, M, Boolean> matcher) {
        matchers.put(clazz, (BiFunction<Object, Object, Boolean>) matcher);
    }

    private BiFunction<Object, Object, Boolean> getMatcher(Class<?> paramClass) {
        if (paramClass == null) {
            return null;
        }
        BiFunction<Object, Object, Boolean> matcher = matchers.get(paramClass);
        if (matcher == null) {
            for (Class<?> classInterface : paramClass.getInterfaces()) {
                matcher = getMatcher(classInterface);
                if (matcher != null) {
                    return matcher;
                }
            }
            return getMatcher(paramClass.getSuperclass());
        }
        return matcher;
    }

    /**
     * Select all objects that match certain parameters.
     * The parameters need to be in the order of the fields in the class that this selector is for.
     * @param params The parameters
     * @return A collection of all matched objects
     */
    public List<T> select(Object... params) {
        Set<T> found = new LinkedHashSet<>();
        Collection<Map<Object, Object>> seekTips = Collections.singleton(map);
        for (int i = 0; i < params.length; i++) {
            if (seekTips.isEmpty()) {
                break;
            }
            List<Map<Object, Object>> newSeekTips = new ArrayList<>();
            if (params[i] != null) {
                BiFunction<Object, Object, Boolean> matcher = getMatcher(params[i].getClass());
                if (matcher != null) {
                    for (Map<Object, Object> seekTip : seekTips) {
                        for (Map.Entry<Object, Object> entry : seekTip.entrySet()) {
                            if (entry.getKey() != null && matcher.apply(params[i], entry.getKey())) {
                                if (params.length == i + 1) {
                                    found.addAll((Collection<T>) entry.getValue());
                                } else {
                                    newSeekTips.add((Map<Object, Object>) entry.getValue());
                                }
                            }
                        }
                    }
                } else {
                    newSeekTips.addAll(find(params[i], seekTips, found, params.length == i + 1));
                }
            }
            newSeekTips.addAll(find(null, seekTips, found, params.length == i + 1));
            seekTips = newSeekTips;
        }
        return new ArrayList<>(found);
    }

    private Collection<? extends Map<Object, Object>> find(Object param, Collection<Map<Object, Object>> seekTips, Collection<T> found, boolean last) {
        List<Map<Object, Object>> newSeekTips = new ArrayList<>();
        for (Map<Object, Object> seekTip : seekTips) {
            Object col = seekTip.get(param);
            if (col != null) {
                if (last) {
                    found.addAll((Collection<T>) col);
                } else {
                    newSeekTips.add((Map<Object, Object>) col);
                }
            }
        }
        return newSeekTips;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Index {

    }
}
