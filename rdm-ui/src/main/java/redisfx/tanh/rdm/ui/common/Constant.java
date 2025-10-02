package redisfx.tanh.rdm.ui.common;

import java.util.List;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/13 20:53
 */
public interface Constant {
    /**
     * 明亮主题标识
     */
    String THEME_LIGHT="Light";
    /**
     * 内容背景色
     */
    String THEME_COLOR_BG_SUBTLE="-color-bg-subtle";
    /**
     * 内容文字色
     */
    String THEME_COLOR_FG_DEFAULT="-color-fg-default";
    /**
     * 内容背景色
     */
    String THEME_COLOR_BG_DEFAULT="-color-bg-default";
    /**
     * 成功色
     */
    String THEME_COLOR_SUCCESS_FG="-color-success-fg";
    /**
     * 强调色
     */
    String THEME_COLOR_ACCENT_FG="-color-accent-fg";
    /**
     * 边框色
     */
    String THEME_COLOR_BORDER_DEFAULT="-color-border-default";
    /**
     * 需要的样式颜色
     */
    List<String> NEED_COLORS=List.of(
            THEME_COLOR_BG_SUBTLE,
            THEME_COLOR_FG_DEFAULT,
            THEME_COLOR_SUCCESS_FG,
            THEME_COLOR_ACCENT_FG,
            THEME_COLOR_BORDER_DEFAULT
    );

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
    String ALERT_MESSAGE_DEL_CONNECTION = "alert.message.delConnection";
    String ALERT_MESSAGE_DEL_FLUSH = "alert.message.delFlush";
    String ALERT_MESSAGE_DEL_GROUP ="alert.message.delGroup" ;
    String ALERT_MESSAGE_DEL_ALL = "alert.message.delAll";
    String TITLE_NEW_KEY ="title.newKey";

    String MAIN_FILE_CONNECT = "main.file.connect";

    /*默认key标签颜色*/
    String COLOR_HASH="#364CFF";
    String COLOR_STREAM="#6A741B";
    String COLOR_LIST="#008556";
    String COLOR_SET="#9C5C2B";
    String COLOR_STRING="#6A1DC3";
    String COLOR_ZSET="#A00A6B";
    //调色板找的颜色
    String COLOR_JSON="#838fa4";
//    String COLOR_JSON="#b8c5db";
//    String COLOR_JSON="#3f4b5f";
    String COLOR_UNKNOWN ="#ff7f0e";

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
    String REDIS_INFO_INSTANTANEOUS_INPUT_KBPS = "instantaneous_input_kbps";
    String REDIS_INFO_INSTANTANEOUS_OUTPUT_KBPS = "instantaneous_output_kbps";

    /**
     * 报表中需要用到的key进行缓存，到时候用于筛选提取需要的数据
     */
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
            REDIS_INFO_INSTANTANEOUS_OPS_PER_SEC,
            REDIS_INFO_INSTANTANEOUS_INPUT_KBPS,
            REDIS_INFO_INSTANTANEOUS_OUTPUT_KBPS
    );


    String INFO_KEYSPACE = "Keyspace";
    String KEY_SEPARATOR =":" ;
    /**
     * 应用主页
     */
    String APP_HOME_PAGE = "app.homepage";
      /**
     * 应用版本
     */
    String APP_VERSION = "app.version";
    String APP_PROPERTIES = "app.properties";
    String APP_PROPERTIES2 = "app.properties2";
    String CONSOLE_TAB_NAME = "Console" ;
    String MONITOR_TAB_NAME = "Monitor";
    String PUBSUB_TAB_NAME = "Pub/Sub";
    String REPORT_TAB_NAME = "Report";
    String WELCOME_TAB_NAME = "Welcome";
}
