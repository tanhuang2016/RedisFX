package xyz.hashdog.rdm.ui.handler.view;

/**
 * 查看器文本类型抽象
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public abstract class AbstractTextViewer implements ValueViewer ,CharacterEncoding{
    @Override
    public ViewerTypeEnum getType() {
        return ViewerTypeEnum.TEXT;
    }
}
