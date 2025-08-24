package xyz.hashdog.rdm.ui;

import atlantafx.base.theme.Dracula;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.hashdog.rdm.redis.exceptions.RedisException;
import xyz.hashdog.rdm.ui.common.Applications;
import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.controller.MainController;
import xyz.hashdog.rdm.ui.entity.config.ConfigSettings;
import xyz.hashdog.rdm.ui.entity.config.LanguageSetting;
import xyz.hashdog.rdm.ui.entity.config.ThemeSetting;
import xyz.hashdog.rdm.ui.exceptions.GeneralException;
import xyz.hashdog.rdm.ui.sampler.event.Save;
import xyz.hashdog.rdm.ui.sampler.theme.SamplerTheme;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;
import xyz.hashdog.rdm.ui.util.GuiUtil;
import xyz.hashdog.rdm.ui.util.LanguageManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author th
 */
public class Main extends Application {
    protected static Logger log = LoggerFactory.getLogger(Main.class);
    public static ResourceBundle RESOURCE_BUNDLE=ResourceBundle.getBundle(LanguageManager.BASE_NAME, LanguageManager.DEFAULT_LOCALE);

    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * 重启窗口
     */
    public static void restart() {
        try {
            // 关闭所有窗口
            ObservableList<Window> windows = Window.getWindows();
            windows.getFirst().hide();
            // 重新启动主应用
            Stage primaryStage = new Stage();
            new Main().start(primaryStage);
        } catch (Exception e) {
            log.error("restart Exception",e);
            // 错误处理
            GuiUtil.alert(Alert.AlertType.ERROR,"Failed to restart application: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage)  {
        try {
            log.error("Application started");
            Save.init();
            // 设置默认的未捕获异常处理器
            Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                log.error("",throwable);
                if(throwable instanceof RedisException||throwable instanceof GeneralException){
                    // 在此处您可以自定义处理异常的逻辑
                    GuiUtil.alert(Alert.AlertType.WARNING,throwable.getLocalizedMessage());
                    return;
                }
                Throwable cause = getRootCause(throwable);
                // 在此处您可以自定义处理异常的逻辑
                GuiUtil.alert(Alert.AlertType.ERROR,cause.getMessage());
            });
            stage.setTitle(Applications.TITLE);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"),RESOURCE_BUNDLE);
            AnchorPane root = fxmlLoader.load();
            MainController controller = fxmlLoader.getController();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/global.css")).toExternalForm());
            stage.setScene(scene);
            controller.setCurrentStage(stage);
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();

            // 确保窗口不会超出屏幕边界
            stage.setWidth(Math.min(root.getPrefWidth(), bounds.getWidth()));
            stage.setHeight(Math.min(root.getPrefHeight(), bounds.getHeight()));
            initTm(scene);
            stage.show();
            //先默认打开
            controller.openServerLinkWindo(null);
        }catch (Exception e){
           log.error("start Exception", e);
        }

    }

    /**
     * 初始化主题
     */
    public static void initTm(Scene scene) {
        ThemeSetting themeSetting = Applications.getConfigSettings(ConfigSettingsEnum.THEME.name);
        ThemeManager tm = ThemeManager.getInstance();
        tm.setScene(scene);
        SamplerTheme theme = tm.getTheme(themeSetting.getColorTheme());
        tm.setTheme(theme,false);
        tm.setFontSize(themeSetting.getFontSize(),false);
        tm.setFontFamily(themeSetting.getFont(),false);
    }

    /**
     * 获取异常的根异常
     * @param throwable 异常
     * @return 根异常
     */
    public Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return throwable;
        }
        return getRootCause(cause);
    }
    @Override
    public void init() throws Exception {
        loadApplicationProperties();

//        DEFAULT_LOCALE= new Locale("en", "US");
//        DEFAULT_LOCALE=Locale.JAPAN;
//        DEFAULT_LOCALE=Locale.US;
//        RESOURCE_BUNDLE=ResourceBundle.getBundle(LanguageManager.BASE_NAME, LanguageManager.DEFAULT_LOCALE);
        LanguageSetting configSettings = Applications.getConfigSettings(ConfigSettingsEnum.LANGUAGE.name);
        Main.RESOURCE_BUNDLE= ResourceBundle.getBundle(LanguageManager.BASE_NAME,Locale.of(configSettings.getLocalLanguage(),configSettings.getLocalCountry()));
    }

    /**
     * 加载应用属性
     */
    private void loadApplicationProperties() {
        Properties properties = new Properties();
        try (InputStreamReader in = new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/application.properties")),
                UTF_8)) {
            properties.load(in);
            properties.forEach((key, value) -> {
                if (!((String) key).contains("[")) {
                    System.setProperty(String.valueOf(key), String.valueOf(value));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        parseArrayProperties(properties);
    }
    /**
     * 解析数组属性
     *
     * @param properties 属性
     */
    private void parseArrayProperties(Properties properties) {
        // 使用 Map 存储所有数组属性
        Map<String, List<String>> arrayProperties = new HashMap<>();

        properties.forEach((key, value) -> {
            String keyStr = (String) key;
            // 检查是否为数组格式 (包含 [index] 的键)
            if (keyStr.contains("[")) {
                // 提取数组名称 (如 app.updates)
                int bracketIndex = keyStr.indexOf('[');
                String arrayName = keyStr.substring(0, bracketIndex);
                // 提取索引
                int index = Integer.parseInt(keyStr.substring(bracketIndex + 1, keyStr.length() - 1));

                // 确保列表存在
                arrayProperties.computeIfAbsent(arrayName, k -> new ArrayList<>());

                // 确保列表大小足够
                List<String> list = arrayProperties.get(arrayName);
                while (list.size() <= index) {
                    list.add(null);
                }
                // 设置值
                list.set(index, String.valueOf(value));
            }
        });
    }

}