package com.qianyi.casinocore.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
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
     * Copy 函数释义.
     *
     * @param <T>    the type parameter
     * @param target 入参释义
     * @return {@link T} 出参释义
     * @author lance
     * @since 2022 -03-02 17:31:45
     */
    public static <T> T copy(T target) {
        T t = null;
        try {
            t = (T) target.getClass().getConstructor().newInstance();
            BeanUtil.copyProperties(target, t);
        } catch (Exception e) {
        }
        return t;
    }

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
        return toNodeTree(list, clazz, null, config().build());
    }

    public static <T, D> List<T> toNodeTree(List<D> list, Class<T> clazz, Consumer<T> consumer) {
        return toNodeTree(list, clazz, consumer, config().build());
    }

    public static <T, D> List<T> toNodeTree(List<D> list, Class<T> clazz, NodeConfig config) {
        return toNodeTree(list, clazz, null, config);
    }

    @Data
    private static class NodeConfig {
        private static final String DEFAULT_ID_KEY = "id";
        private static final String DEFAULT_PID_KEY = "pid";
        private static final String DEFAULT_CHILDREN_KEY = "children";
        private static final Long DEFAULT_EQUAL_CONDITION = 0L;

        private String idKey = DEFAULT_ID_KEY;

        private String pidKey = DEFAULT_PID_KEY;

        private String childrenKey = DEFAULT_CHILDREN_KEY;

        private Object rootCondition = DEFAULT_EQUAL_CONDITION;

        public Builder builder () {
            return new Builder(this);
        }

        public static class Builder {
            private NodeConfig config;
            public Builder(NodeConfig config) {
                this.config = config;
            }

            public Builder setIdKey(String idKey) {
                this.config.setIdKey(idKey);
                return this;
            }

            public Builder setPidKey(String pidKey) {
                this.config.setPidKey(pidKey);
                return this;
            }

            public Builder setChildrenKey(String childrenKey) {
                this.config.setChildrenKey(childrenKey);
                return this;
            }

            public Builder setRootCondition(Object rootCondition) {
                this.config.setRootCondition(rootCondition);
                return this;
            }

            public NodeConfig build(){
                return this.config;
            }
        }

    }

    public static NodeConfig.Builder config(){
        return new NodeConfig().builder();
    }

    public static <T, D> List<T> toNodeTree(List<D> list, Class<T> clazz, Consumer<T> consumer, NodeConfig config) {
        List<T> nodeList = toDTO(list, clazz, consumer);
        List<T> root = new ArrayList<>();
        //BeanUtil.getFieldValue()
        for (T dto: nodeList) {
            Object pid = BeanUtil.getFieldValue(dto, config.pidKey);
            if ((null == config.rootCondition && null == pid) || (null != config.rootCondition && config.rootCondition.equals(pid))) {
                root.add(dto);
            }
        }

        // 根据pid分组
        Map<Object, List<T>> hash = nodeList.stream().collect(
                Collectors.groupingBy(dto -> BeanUtil.getFieldValue(dto, config.pidKey))
        );

        deepTree(root, hash, config);

        return root;
    }

    private static <T> void deepTree(List<T> root, Map<Object, List<T>> hash, NodeConfig config) {
        for (T dto: root) {
            Object id = BeanUtil.getFieldValue(dto, config.idKey);
            List<T> children = hash.get(id);
            if (children != null && CollUtil.isNotEmpty(children)) {
                BeanUtil.setFieldValue(dto, config.childrenKey, children);
                deepTree(children, hash, config);
            }
        }
    }

    /**
     * 展开根路径
     *
     * @param <T>  the type parameter
     * @param tree 入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -03-02 17:36:55
     */
    public static <T> List<T> unwindRoot(List<T> tree) {
        List<T> list = new ArrayList<>();
        deepUnwind(tree, list);
        return list;
    }

    /**
     * 展开根路径
     *
     * @param <T>  the type parameter
     * @param <B>  the type parameter
     * @param tree 入参释义
     * @param fn   入参释义
     * @param refs 入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -03-02 17:31:45
     */
    public static <T, B> List<T> unwindRoot(List<T> tree, Function<T, B> fn, Map<B, B> refs) {
        List<T> list = new ArrayList<>();
        deepUnwind(tree, list, refs, fn);
        return list;
    }

    private static <T> void deepUnwind(List<T> tree, List<T> list) {
        for (T item: tree) {
            Object fieldValue = BeanUtil.getFieldValue(item, "children");
            T copy = copy(item);
            BeanUtil.setFieldValue(copy, "children", null);
            list.add(copy);

            if (fieldValue != null) {
                List<T> children = (List<T>) fieldValue;
                if (CollUtil.isEmpty(children)) {
                    continue;
                }
                deepUnwind(children, list);
            }
        }
    }

    private static <T, B> void deepUnwind(List<T> tree, List<T> list, Map<B, B> refs, Function<T, B> fn) {
        for (T item: tree) {
            Object fieldValue = BeanUtil.getFieldValue(item, "children");
            T copy = copy(item);
            BeanUtil.setFieldValue(copy, "children", null);
            list.add(copy);

            if (fieldValue != null) {
                List<T> children = (List<T>) fieldValue;
                if (CollUtil.isEmpty(children)) {
                    continue;
                }
                for (T c : children) {
                    B key = fn.apply(c);
                    B value = fn.apply(item);
                    refs.put(key, value);
                }
                deepUnwind(children, list, refs, fn);
            }
        }
    }

    public static <T, B> List<T> toNodeTree(List<T> list, Map<B, B> refs, Function<T, B> fn) {
        // 最顶级的
        List<T> roots = new ArrayList<>();
        List<T> items = new ArrayList<>();
        for (T node: list) {
            B key = fn.apply(node);
            if (null == refs.get(key)) {
                roots.add(node);
            } else {
                items.add(node);
            }
        }

        deepTree(roots, items, refs, fn);

        return roots;
    }

    private static  <T, B> void deepTree(List<T> roots, List<T> items, Map<B, B> refs, Function<T, B> fn){
        for (T root: roots) {
            List<T> children = new ArrayList<>();
            B rootKey = fn.apply(root);
            for (T item: items) {
                B ref = fn.apply(item);
                B key = refs.get(ref);
                if (rootKey.equals(key)) {
                    children.add(item);
                }
            }
            // 设置 children
            BeanUtil.setFieldValue(root, "children", children);
            if (CollUtil.isNotEmpty(children)) {
                deepTree(children, items, refs, fn);
            }
        }
    }

}
