package xyz.hashdog.rdm.ui.util;

import javafx.scene.control.ButtonBase;
import xyz.hashdog.rdm.ui.sampler.event.DefaultEventBus;
import xyz.hashdog.rdm.ui.sampler.event.ThemeEvent;
import xyz.hashdog.rdm.ui.sampler.theme.SamplerTheme;
import xyz.hashdog.rdm.ui.sampler.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class SvgManager {
    /**
     * todo 要改成map装，在close的时候，将缓存释放掉，根据key去查对应组件缓存的图标
     */
    private static final List<SvgManager> list = new ArrayList<>();
    private final ButtonBase base;
    private final String svg;
    private  int w;
    private  static boolean light;
    static {
        light=ThemeManager.getInstance().getTheme().getName().contains("Light");
        DefaultEventBus.getInstance().subscribe(ThemeEvent.class, e -> {
            var eventType = e.getEventType();
            if (eventType == ThemeEvent.EventType.THEME_CHANGE ) {
                SamplerTheme theme = ThemeManager.getInstance().getTheme();
                boolean light1 = theme.getName().contains("Light");
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
        for (SvgManager svgManager : list) {
            svgManager.setGraphic();
        }
    }

    private SvgManager(ButtonBase base, String svg) {
        this.base = base;
        this.svg = svg;
        this.w = 20;
    }

    /**
     * 加载svg图标 默认20w
     * @param base
     * @param svg
     */
    public static void load(ButtonBase base, String svg) {
        SvgManager svgManager = new SvgManager(base, svg);
        svgManager.setGraphic();
        list.add(svgManager);
    }

    /**
     * 创建图标，默认16w
     * @param base
     * @param svg
     */
    public static void loadMini(ButtonBase base, String svg) {
        SvgManager svgManager = new SvgManager(base, svg,16);
        svgManager.setGraphic();
        list.add(svgManager);
    }

    private void setGraphic() {
        if(light){
            GuiUtil.setIcon(this.base,GuiUtil.svgImageView(this.svg,w));
        }else{
            GuiUtil.setIcon(this.base,GuiUtil.svgImageView(this.svg.replace(".svg","_dark.svg"),w));
        }
    }
}
