package redisfx.tanh.rdm.ui.entity.config;

import javafx.geometry.Side;
import redisfx.tanh.rdm.ui.common.TabPaneStyleEnum;

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
}
