package xyz.hashdog.rdm.ui.common;

import java.util.List;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/13 20:53
 */
public interface Constant {

    /*国际化配置*/
    String CLOSE = "common.tab.close";
    String CLOSE_OTHER = "common.tab.closeOther";
    String CLOSE_LEFT ="common.tab.closeLeft";
    String CLOSE_RIGHT ="common.tab.closeRight";
    String CLOSE_ALL = "common.tab.closeAll";
    String OK = "common.ok";
    String CANCEL = "common.cancel";
    String ALERT_MESSAGE_DEL = "alert.message.del";
    String ALERT_MESSAGE_SAVE_SUCCESS = "alert.message.save.success";
    String ALERT_MESSAGE_SET_SUCCESS = "alert.message.set.success";
    String ALERT_MESSAGE_RENAME_SUCCESS = "alert.message.rename.success";
    String ALERT_MESSAGE_CONNECT_SUCCESS = "alert.message.connect.success";
    String ALERT_MESSAGE_RESTART_SUCCESS = "alert.message.restart.success";
    String ALERT_MESSAGE_SET_TTL = "alert.message.set.ttl";
    String ALERT_MESSAGE_DELCONNECTION = "alert.message.delConnection";
    String ALERT_MESSAGE_DELGROUP ="alert.message.delGroup" ;
    String ALERT_MESSAGE_DELFLUSH = "alert.message.delFlush";
    String ALERT_MESSAGE_DELALL = "alert.message.delAll";
    String TITLE_CONNECTION = "title.connection";
    String TITLE_NEW_KEY ="title.newKey";

    String MAIN_FILE_CONNECT = "main.file.connect";
    String MAIN_FILE_SETTINGS = "main.file.settings";

    /*默认key标签颜色*/
    String COLOR_HASH="#364CFF";
    String COLOR_STREAM="#6A741B";
    String COLOR_LIST="#008556";
    String COLOR_SET="#9C5C2B";
    String COLOR_STRING="#6A1DC3";
    String COLOR_ZSET="#A00A6B";
    String COLOR_JSON="#b8c5db";
    String COLOR_UNKNOWN ="#000000";

    /*redis info重要的key*/
    String REDIS_INFO_REDIS_VERSION = "redis_version";
    String REDIS_INFO_OS = "os";
    String REDIS_INFO_PROCESS_ID = "process_id";
    String REDIS_INFO_USED_MEMORY = "used_memory";
    String REDIS_INFO_USED_MEMORY_PEAK = "used_memory_peak";
    String REDIS_INFO_USED_MEMORY_LUA = "used_memory_lua";
    String REDIS_INFO_CONNECTED_CLIENTS = "connected_clients";
    String REDIS_INFO_TOTAL_CONNECTIONS_RECEIVED = "total_connections_received";
    String REDIS_INFO_TOTAL_COMMANDS_PROCESSED = "total_commands_processed";
    String REDIS_INFO_USED_CPU_USER = "used_cpu_user";
    String REDIS_INFO_USED_CPU_SYS = "used_cpu_sys";
    String REDIS_INFO_INSTANTANEOUS_OPS_PER_SEC = "instantaneous_ops_per_sec";

    List<String> REDIS_INFO_KEYS=List.of(
            REDIS_INFO_REDIS_VERSION,
            REDIS_INFO_OS,
            REDIS_INFO_PROCESS_ID,
            REDIS_INFO_USED_MEMORY,
            REDIS_INFO_USED_MEMORY_PEAK,
            REDIS_INFO_USED_MEMORY_LUA,
            REDIS_INFO_CONNECTED_CLIENTS,
            REDIS_INFO_TOTAL_CONNECTIONS_RECEIVED,
            REDIS_INFO_TOTAL_COMMANDS_PROCESSED,
            REDIS_INFO_USED_CPU_USER,
            REDIS_INFO_USED_CPU_SYS,
            REDIS_INFO_INSTANTANEOUS_OPS_PER_SEC
    );


    String INFO_KEYSPACE = "Keyspace";
}
