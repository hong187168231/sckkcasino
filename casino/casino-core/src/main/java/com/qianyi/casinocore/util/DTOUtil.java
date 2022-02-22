package com.qianyi.casinocore.util;

import org.springframework.beans.BeanUtils;

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
     * To dto 函数释义.
     *
     * @param <T>   the type parameter
     * @param <D>   the type parameter
     * @param d     入参释义
     * @param clazz 入参释义
     * @return {@link T} 出参释义
     * @author lance
     * @since 2022 -02-21 13:44:24
     */
    public static <T, D> T toDTO(D d, Class<T> clazz) {
        return toDTO(d, clazz, null);
    }

    /**
     * To dto 函数释义.
     *
     * @param <T>      the type parameter
     * @param <D>      the type parameter
     * @param d        入参释义
     * @param clazz    入参释义
     * @param consumer 入参释义
     * @return {@link T} 出参释义
     * @author lance
     * @since 2022 -02-21 13:44:24
     */
    public static <T, D> T toDTO(D d, Class<T> clazz, Consumer<T> consumer) {
        T t = null;
        try {
            t = clazz.getConstructor().newInstance();
            BeanUtils.copyProperties(d, t);
            if (consumer != null) {
                consumer.accept(t);
            }

        } catch (Exception e) {
        }
        return t;
    }

    /**
     * To dto 函数释义.
     *
     * @param <T>      the type parameter
     * @param <D>      the type parameter
     * @param dataList 入参释义
     * @param clazz    入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-21 13:44:24
     */
    public static <T, D> List<T> toDTO(List<D> dataList, Class<T> clazz) {
        return toDTO(dataList, clazz, null);
    }

    /**
     * To dto 函数释义.
     *
     * @param <T>      the type parameter
     * @param <D>      the type parameter
     * @param dataList 入参释义
     * @param clazz    入参释义
     * @param consumer 入参释义
     * @return {@link List} 出参释义
     * @author lance
     * @since 2022 -02-21 13:44:25
     */
    public static <T, D> List<T> toDTO(List<D> dataList, Class<T> clazz, Consumer<T> consumer) {
        List<T> list = new ArrayList<>();
        for (D d: dataList) {
            T t = null;
            try {
                t = clazz.getConstructor().newInstance();
                BeanUtils.copyProperties(d, t);
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
     * To dto 函数释义.
     *
     * @param <T>        the type parameter
     * @param <D>        the type parameter
     * @param dataList   入参释义
     * @param clazz      入参释义
     * @param predicate  入参释义
     * @param comparator 入参释义
     * @return {@link List} 出参释义
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
                        BeanUtils.copyProperties(cls, t);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    }
                    return t;
                }).collect(Collectors.toList());
    }

}
