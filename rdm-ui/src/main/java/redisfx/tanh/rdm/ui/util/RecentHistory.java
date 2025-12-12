package redisfx.tanh.rdm.ui.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedCollection;

/**
 * 最近记录保存，用于搜索记录，打开最近连接等功能
 *
 * @author th
 * @version 2.0.0
 * @since 2025/7/20 13:10
 */
public class RecentHistory<T> {

    private final int size;
    /**
     * 是否去重
     */
    private final boolean distinct;
    /**
     * 最近记录
     */
    private final SequencedCollection<T> historys;
    /**
     * 最近记录通知者
     */
    private final Noticer<T> noticer;

    public RecentHistory(int size, Noticer<T> noticer) {
        this(size, noticer,true);
    }
    public RecentHistory(int size, Noticer<T> noticer,boolean distinct) {
        this.size = size;
        this.noticer = noticer;
        historys =  new ArrayList<>(size);
        this.distinct = distinct;
    }

    /**
     * 添加
     *
     * @param add 添加一条最近记录
     */
    public void add(T add) {
        if (distinct) {
            historys.remove(add);
        }
        if (historys.size() >= size) {
            historys.removeLast();
        }
        historys.addFirst(add);
        //添加之后通知
        notice();
    }

    /**
     * 通知最近记录，全量返回
     */
    private void notice() {
        noticer.notice(get());
    }

    /**
     * 清空最近记录
     */
    public void clear() {
        historys.clear();
        notice();
    }


    /**
     * 获取最近记录
     *
     * @return 最近记录
     */
    public List<T> get() {
        List<T> list = new ArrayList<>(historys);
        if (list.size() > size) {
            return list.subList(0, size);
        }
        return list;
    }


    /**
     * 最近记录通知者
     *
     * @param <T>
     */
    public interface Noticer<T> {
        void notice(List<T> list);

    }
}
