package xyz.hashdog.rdm.ui.util;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.Labeled;
import xyz.hashdog.rdm.ui.common.Constant;
import xyz.hashdog.rdm.ui.controller.base.BaseController;
import xyz.hashdog.rdm.ui.sampler.event.DefaultEventBus;
import xyz.hashdog.rdm.ui.sampler.event.ThemeEvent;
import xyz.hashdog.rdm.ui.sampler.theme.SamplerTheme;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author th
 */
public class SvgManager {
    /**
     * 缓存 key是控制器，value是图标
     * 当窗口关闭，可以根据控制器删除图标
     */
    private static final Map<Object,List<SvgManager>> MAP = new ConcurrentHashMap<>();
    private final Labeled base;
    private final String svg;
    private final int w;
    private  static boolean light;
    static {
        light=ThemeManager.getInstance().getTheme().getName().contains(Constant.THEME_LIGHT);
        DefaultEventBus.getInstance().subscribe(ThemeEvent.class, e -> {
            var eventType = e.getEventType();
            if (eventType == ThemeEvent.EventType.THEME_CHANGE ) {
                SamplerTheme theme = ThemeManager.getInstance().getTheme();
                boolean light1 = theme.getName().contains(Constant.THEME_LIGHT);
                if(light1!=light){
                    light=light1;
                    changeSvg();
                }
            }
        });
    }

    public SvgManager(ButtonBase base, String svg, int w) {
        this.base = base;
        this.svg = svg;
        this.w = w;
    }

    private static void changeSvg() {
        MAP.forEach((baseController, svgManagers) -> {
            for (SvgManager svgManager : svgManagers) {
                svgManager.setGraphic();
            }
        });
    }

    private SvgManager(Labeled base, String svg) {
        this.base = base;
        this.svg = svg;
        this.w = 20;
    }

    /**
     * 加载svg图标 默认20w
     * @param base 按钮
     * @param svg 文件名
     */
    public static void load(BaseController<?> controller, Labeled base, String svg) {
        SvgManager svgManager = new SvgManager(base, svg);
        svgManager.setGraphic();
        List<SvgManager> list = MAP.computeIfAbsent(controller, k -> new ArrayList<>());
        list.add(svgManager);
    }

    /**
     * 创建图标，默认16w
     * @param base 按钮
     * @param svg 文件名
     */
    public static void loadMini(BaseController<?> controller,ButtonBase base, String svg) {
        SvgManager svgManager = new SvgManager(base, svg,16);
        svgManager.setGraphic();
        List<SvgManager> list = MAP.computeIfAbsent(controller, k -> new ArrayList<>());
        list.add(svgManager);
    }

    /**
     * 删除图标
     * @param controller 控制器
     */
    public static  void clear(BaseController<?> controller) {
        MAP.remove(controller);
    }

    /**
     * 设置图标
     */
    private void setGraphic() {
        if(light){
            GuiUtil.setIcon(this.base,GuiUtil.svgImageView(this.svg,w));
        }else{
            GuiUtil.setIcon(this.base,GuiUtil.svgImageView(this.svg.replace(".svg","_dark.svg"),w));
        }
    }
}
