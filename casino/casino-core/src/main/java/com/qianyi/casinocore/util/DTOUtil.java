package com.qianyi.casinocore.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * DTO转换工具
 *
 * @author lance
 * @since 2022 -02-21 13:44:24
 */
public class DTOUtil {

    /**
     * 将目标转换为对应的实体
     *
     * @param <T>   目标泛型
     * @param <D>   当前泛型
     * @param d     待转换对象
     * @param clazz 目标类型class
     * @return {@link T} 目标对象
     * @author lance
     * @since 2022 -02-21 13:44:24
     */
    public static <T, D> T toDTO(D d, Class<T> clazz) {
        return toDTO(d, clazz, null);
    }

    /**
     * 将目标转换为对应的实体
     *
     * @param <T>      目标泛型
     * @param <D>      当前泛型
     * @param d        待转换对象
     * @param clazz    目标类型class
     * @param consumer 对得到的目标对象进行进一步的处理
     * @return {@link T} 目标对象
     * @author lance
     * @since 2022 -02-21 13:44:24
     */
    public static <T, D> T toDTO(D d, Class<T> clazz, Consumer<T> consumer) {
        T t = null;
        try {
            t = clazz.getConstructor().newInstance();
            BeanUtil.copyProperties(d, t);
            if (consumer != null) {
                consumer.accept(t);
            }

        } catch (Exception e) {
        }
        return t;
    }

    /**
     * map转dto
     *
     * @param <T>   目标泛型
     * @param d     map
     * @param clazz 需要转换的class
     * @return {@link T} 目标对象
     * @author lance
     * @since 2022 -02-25 14:27:48
     */
    public static <T> T toDTO(Map<String, Object> d, Class<T> clazz) {
        return toDTO(d, clazz, null);
    }

    /**
     * map转dto
     *
     * @param <T>      目标泛型
     * @param d        map
     * @param clazz    需要转换的class
     * @param consumer 进一步处理的方法
     * @return {@link T} 目标
     * @author lance
     * @since 2022 -02-25 14:27:48
     */
    public static <T> T toDTO(Map<String, Object> d, Class<T> clazz, Consumer<T> consumer) {
        T t = BeanUtil.mapToBean(d, clazz, true);
        if (consumer != null) {
            consumer.accept(t);
        }
        return t;
    }

    /**
     * map列表转dto
     *
     * @param <T>      目标泛型
     * @param dataList map列表
     * @param clazz    需要转换的class
     * @return {@link List} 目标列表
     * @author lance
     * @since 2022 -02-25 14:27:48
     */
    public static <T> List<T> map2DTO(List<Map<String,Object>> dataList, Class<T> clazz) {
        return map2DTO(dataList, clazz, null);
    }

    /**
     * map列表转dto
     *
     * @param <T>      目标泛型
     * @param dataList map列表
     * @param clazz    需要转换的class
     * @param consumer 进一步处理的方法
     * @return {@link List} 目标列表
     * @author lance
     * @since 2022 -02-25 14:27:48
     */
    public static <T> List<T> map2DTO(List<Map<String,Object>> dataList, Class<T> clazz, Consumer<T> consumer) {
        List<T> list = new ArrayList<>();
        for (Map<String,Object> d: dataList) {
            T t = toDTO(d, clazz, consumer);
            list.add(t);
        }
        return list;
    }

    /**
     * 将目标列表转换为对应类型的列表
     *
     * @param <T>      目标泛型
     * @param <D>      当前泛型
     * @param dataList 待转换列表
     * @param clazz    目标类型class
     * @return {@link List} 目标列表
     * @author lance
     * @since 2022 -02-21 13:44:24
     */
    public static <T, D> List<T> toDTO(List<D> dataList, Class<T> clazz) {
        return toDTO(dataList, clazz, null);
    }

    /**
     * 将目标列表转换为对应类型的列表
     *
     * @param <T>      目标泛型
     * @param <D>      当前泛型
     * @param dataList 待转换列表
     * @param clazz    目标类型class
     * @param consumer 对得到的目标对象进行进一步的处理
     * @return {@link List} 目标列表
     * @author lance
     * @since 2022 -02-21 13:44:25
     */
    public static <T, D> List<T> toDTO(List<D> dataList, Class<T> clazz, Consumer<T> consumer) {
        List<T> list = new ArrayList<>();
        for (D d: dataList) {
            T t = null;
            try {
                t = clazz.getConstructor().newInstance();
                BeanUtil.copyProperties(d, t);
                if (consumer != null) {
                    consumer.accept(t);
                }
                list.add(t);
            } catch (Exception e) {
            }
        }
        return list;
    }

    /**
     * 将目标列表转换为对应类型的列表
     *
     * @param <T>        目标泛型
     * @param <D>        当前泛型
     * @param dataList   待转换列表
     * @param clazz      目标类型class
     * @param predicate  过滤条件
     * @param comparator 对得到的目标对象进行进一步的处理
     * @return {@link List} 目标列表
     * @author lance
     * @since 2022 -02-21 13:44:25
     */
    public static <T, D> List<T> toDTO(List<D> dataList, Class<T> clazz, Predicate<D> predicate, Comparator<D> comparator){
        return dataList.stream()
                .filter(predicate)
                .sorted(comparator)
                .map(cls -> {
                    T t = null;
                    try {
                        t = clazz.getConstructor().newInstance();
                        BeanUtil.copyProperties(cls, t);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    }
                    return t;
                }).collect(Collectors.toList());
    }

    /**
     * 列表转列表树
     *
     * @param <T>   目标泛型
     * @param <D>   当前泛型
     * @param list  待转换列表
     * @param clazz 目标类型class
     * @return {@link List} 目标列表
     * @author lance
     * @since 2022 -02-24 13:06:04
     */
    public static <T, D> List<T> toNodeTree(List<D> list, Class<T> clazz){
        return toNodeTree(list, clazz, null, "pid");
    }

    /**
     * 列表转列表树
     *
     * @param <T>    目标泛型
     * @param <D>    当前泛型
     * @param list   待转换列表
     * @param clazz  目标类型class
     * @param pidKey pid 对应的属性字段名称
     * @return {@link List} 目标列表
     * @author lance
     * @since 2022 -02-24 13:06:04
     */
    public static <T, D> List<T> toNodeTree(List<D> list, Class<T> clazz, String pidKey){
        return toNodeTree(list, clazz, null, pidKey);
    }


    /**
     * 列表转列表树
     *
     * @param <T>      目标泛型
     * @param <D>      当前泛型
     * @param list     待转换列表
     * @param clazz    目标类型class
     * @param consumer 对结果做进一步处理
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-24 13:06:04
     */
    public static <T, D> List<T> toNodeTree(List<D> list, Class<T> clazz, Consumer<T> consumer) {
        return toNodeTree(list, clazz, consumer, "pid");
    }

    /**
     * 列表转列表树
     *
     * @param <T>      目标泛型
     * @param <D>      当前泛型
     * @param list     待转换列表
     * @param clazz    目标类型class
     * @param consumer 对结果做进一步处理
     * @param pidKey   pid 对应的属性字段名称
     * @return {@link List} 目标列表
     * @author lance
     * @since 2022 -02-24 13:06:04
     */
    public static <T, D> List<T> toNodeTree(List<D> list, Class<T> clazz, Consumer<T> consumer, String pidKey) {
        List<T> nodeList = toDTO(list, clazz, consumer);
        List<T> root = new ArrayList<>();
        //BeanUtil.getFieldValue()
        for (T dto: nodeList) {
            Long pid = (Long) BeanUtil.getFieldValue(dto, pidKey);
            if (pid == -1L) {
                root.add(dto);
            }
        }
        Map<Long, List<T>> hash = new HashMap<>();
        for (T dto: nodeList) {
            Long pid = (Long) BeanUtil.getFieldValue(dto, pidKey);
            hash.putIfAbsent(pid, new ArrayList<>());
            hash.get(pid).add(dto);
        }

        deepTree(root, hash);

        return root;
    }

    private static <T> void deepTree(List<T> root, Map<Long, List<T>> hash) {
        for (T dto: root) {
            Long id = (Long) BeanUtil.getFieldValue(dto, "id");
            List<T> children = hash.get(id);
            if (children != null && CollUtil.isNotEmpty(children)) {
                BeanUtil.setFieldValue(dto, "children", children);
                deepTree(children, hash);
            }
        }
    }

}
