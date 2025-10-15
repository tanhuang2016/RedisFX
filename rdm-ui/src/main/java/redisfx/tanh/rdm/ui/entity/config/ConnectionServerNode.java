package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.redis.RedisConfig;
import redisfx.tanh.rdm.ui.common.Applications;

import java.util.Objects;

/**
 * 连接实体类,分组和连接共用1个实体
 * type,dataId,parentDataId,timestampSort这几个字段在编辑是不可修改的
 * @author th
 * @version 1.0.0
 * @since 2023/7/20 16:21
 */
public class ConnectionServerNode extends RedisConfig {
    public static final int SERVER = 2;
    public static final int GROUP = 1;
    /**
     * 类型,分组为1,连接为2
     */
    private int type;
    /**
     * 定位id
     */
    private String dataId;
    /**
     * 父节点id
     */
    private String parentDataId;
    /**
     * 时间戳排序
     */
    private long timestampSort;
    /**
     * 版本号
     */
    private int version;


    public ConnectionServerNode() {
    }

    public long getTimestampSort() {
        return timestampSort;
    }

    public void setTimestampSort(long timestampSort) {
        this.timestampSort = timestampSort;
    }

    public ConnectionServerNode(int type) {
        this.type=type;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getParentDataId() {
        return parentDataId;
    }

    public void setParentDataId(String parentDataId) {
        this.parentDataId = parentDataId;
    }


    /**
     * 是否是连接
     * @return true:连接,false:分组
     */
    public boolean isConnection() {
        return type==2;
    }

    /**
     * 是否是跟
     * @return true:跟,false:非跟
     */
    public boolean isRoot() {
        return dataId.equals(Applications.ROOT_ID) ;
    }

    @Override
    public String toString() {
        return getName();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConnectionServerNode that = (ConnectionServerNode) o;
        return type == that.type && timestampSort == that.timestampSort && version == that.version && Objects.equals(dataId, that.dataId) && Objects.equals(parentDataId, that.parentDataId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, dataId, parentDataId, timestampSort, version);
    }

}
