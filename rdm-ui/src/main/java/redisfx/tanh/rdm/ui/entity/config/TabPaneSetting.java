package redisfx.tanh.rdm.ui.entity.config;

import javafx.geometry.Side;
import redisfx.tanh.rdm.ui.common.TabPaneStyleEnum;

import java.util.Objects;

public abstract class TabPaneSetting implements ConfigSettings{
    protected String side;
    protected String style ;
    /**
     * 动画效果
     */
    protected boolean animated;
    /**
     * 满宽
     */
    protected boolean fullWidth;
    /**
     * 紧凑
     */
    protected boolean dense;

    /**
     * 版本
     */
    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }



    @Override
    public TabPaneSetting init(){
        side= Side.TOP.name();
        style = TabPaneStyleEnum.DEFAULT.name;
        animated = true;
        fullWidth = false;
        dense = false;
        return this;
    }



    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public boolean isFullWidth() {
        return fullWidth;
    }

    public void setFullWidth(boolean fullWidth) {
        this.fullWidth = fullWidth;
    }

    public boolean isDense() {
        return dense;
    }

    public void setDense(boolean dense) {
        this.dense = dense;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TabPaneSetting that = (TabPaneSetting) o;
        return animated == that.animated && fullWidth == that.fullWidth && dense == that.dense && version == that.version && Objects.equals(side, that.side) && Objects.equals(style, that.style);
    }

    @Override
    public int hashCode() {
        return Objects.hash(side, style, animated, fullWidth, dense, version);
    }
}
