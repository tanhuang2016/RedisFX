package xyz.hashdog.rdm.common.util;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 泛型/反射相关操作工具
 *
 * @author th
 * @version 1.0.0
 * @since 2023/7/18 21:09
 */
public class TUtil {

    /**
     * 只有包装类型才能复制，注意不要使用基本类型
     * 同类复制属性(只复制是null的属性)
     * @param souce
     * @param target
     * @param <T>
     * @throws IllegalAccessException
     */
    public static <T> void copyProperties(T souce, T target) {
        Class clazz = souce.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //如果是null,从源对象复制到模板对象
            try {
                Object o = field.get(target);
                Object o2 = field.get(souce);
                if(o==null&&o2!=null){
                    field.set(target, field.get(souce));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }



    /**
     * 判断字符串是否为空
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        if(str==null){
            return false;
        }
        if(str.isEmpty()){
            return false;
        }
        return true;
    }

    public static  <T> T ifNull(T t1,T t2) {
        return t1==null?t2:t1;
    }


    /**
     * 递归策略接口
     * @param <L>
     * @param <T>
     */
    public static interface RecursiveTree2List<L, T> {

        /**
         * 递归
         *
         * @param h             结果集
         * @param t             需要递归迭代的目标
         * @param recursiveDeal
         * @param <L>
         * @param <T>
         */
        static <L, T> void recursive(L h, T t, RecursiveTree2List<L, T> recursiveDeal) {
            //获取子集
            List<T> ts = recursiveDeal.subset(t);
            if (ts == null || ts.isEmpty()) {
                recursiveDeal.noSubset(h, t);
            } else {
                L newh = recursiveDeal.hasSubset(h, t);
                for (T t1 : ts) {
                    recursive(newh, t1, recursiveDeal);
                }
            }
        }

        /**
         * 获取节点子集
         * @param t
         * @return
         */
        List<T> subset(T t) ;
        /**
         * 没有子集的情况怎么处理
         * @param h
         * @param t
         */
        void noSubset(L h, T t);
        /**
         * 有子集的情况怎么处理
         * @param h
         * @param t
         * @return
         */
        L hasSubset(L h, T t);
    }



    /**
     * 递归策略接口
     * 将list转为tree
     *
     * @param <T>
     * @param <L>
     */
    public static interface RecursiveList2Tree<T, L> {

        /**
         * @param tree               树结果
         * @param list               需要迭代的list
         * @param recursiveList2Tree 策略
         * @param <T>
         * @param <L>
         */
        static <T, L> void recursive(T tree, List<L> list, RecursiveList2Tree<T, L> recursiveList2Tree) {

            //获取子集
            List<L> subs = recursiveList2Tree.findSubs(tree, list);
            //整合到tree
            List<T> treeList = recursiveList2Tree.toTree(tree, subs);
            //过滤掉已经迭代到tree的数据
            List<L> newList = recursiveList2Tree.filterList(list, subs);
            for (T tree1 : treeList) {
                recursive(tree1, newList, recursiveList2Tree);
            }

        }
        /**
         * 获取子节
         * 一般根据父节点id,从list匹配子节点
         *
         * @param tree 父节点
         * @param list 需要迭代的集合
         * @return 找到的子节点
         */
        List<L> findSubs(T tree, List<L> list);
        /**
         * 转为树
         *
         * @param tree 父节点
         * @param subs 子节点
         * @return 转为树的子节点
         */
        List<T> toTree(T tree, List<L> subs);

        /**
         * 一般已经迭代过的list可以过滤掉,避免重复,具体由子类实现
         *
         * @param list 原始集合
         * @param subs 已经迭代处理过的集合
         * @return 过滤后的集合
         */
        List<L> filterList(List<L> list, List<L> subs);
    }


    /**
     * 反射获取对象的字段
     *
     * @param obj
     * @param fieldName 获取的字段
     * @param <T>
     * @return
     */
    public static <T> T getField(Object obj, String fieldName) {
        // 使用反射获取字段的值
        try {
            // 获取对象的 Class 对象
            Class<?> objClass = obj.getClass();
            // 获取字段对象
            Field field = objClass.getDeclaredField(fieldName);
            // 设置允许访问私有字段
            field.setAccessible(true);
            // 获取字段的值
            Object fieldValue = field.get(obj);
            return (T) fieldValue;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * spi获取此类服务
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T spi(Class<T> clazz) {
        ServiceLoader<T> load = ServiceLoader.load(clazz);
        Iterator<T> iterator = load.iterator();

        //循环获取所需的对象
        while (iterator.hasNext()) {
            T next = iterator.next();
            if (next != null) {
                return next;
            }
        }
        throw new RuntimeException("no such spi:" + clazz.getName());
    }


    /**
     * 执行方法
     * 目前用于统一处理jedis执行命令之后的close操作
     *
     * @param t           可以是jedis
     * @param execCommand 需要执行的具体逻辑
     * @param callback    执行逻辑之后的回调,比如关流
     * @param <T>         jedis
     * @param <R>         执行jedis命令之后的返回值
     * @return
     */
    public static <T, R> R execut(T t, Function<T, R> execCommand, Consumer<T> callback) {
        try {
            return execCommand.apply(t);
        } finally {
//            callback.accept(t);
        }
    }


    public static String ifEmpty(String str, String o) {
        if(str==null||str.isEmpty()){
            return o;
        }
        return str;
    }
}
