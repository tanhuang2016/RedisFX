package redisfx.tanh.rdm.ui.handler.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import redisfx.tanh.rdm.ui.common.Constant;

import java.io.ByteArrayInputStream;
/**
 * 图片查看器
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class ImageViewer implements ValueViewer{
    public static final String NAME="Image";


    @Override
    public ViewerTypeEnum type() {
        return ViewerTypeEnum.Binary;
    }
    @Override
    public String name() {
        return NAME;
    }
    @Override
    public ViewerNode newViewerNode() {
        return new ImageViewerNode();
    }


    @Override
    public boolean accept(byte[] data) {
        //判断是否是图片数据
        if (data == null || data.length < 4) {
            return false;
        }
        // PNG
        if (data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
            return true;
        }
        // JPG/JPEG
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) {
            return true;
        }
        // GIF
        return data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46 && data[3] == 0x38;
    }
    private class ImageViewerNode implements ViewerNode{
        private final StackPane stackPane;
        private final ImageView imageView;
        private byte[] value;

        public ImageViewerNode() {
            stackPane = new StackPane();
            // 设置背景色和边框
            stackPane.setStyle("""
                    -fx-border-color:%s;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                    """.formatted(Constant.THEME_COLOR_BORDER_DEFAULT)
            );
            stackPane.setPadding(new Insets(10));
            stackPane.setPrefHeight(500);
            stackPane.setPrefWidth(500);
            imageView = new ImageView();
            // 设置自动缩放属性
            imageView.setPreserveRatio(true);  // 保持图片原始比例
            imageView.setFitWidth(stackPane.getPrefWidth() - 20);  // 减去padding
            imageView.setFitHeight(stackPane.getPrefHeight() - 20);
            stackPane.getChildren().add(imageView);
            // 设置ImageView居中对齐
            StackPane.setAlignment(imageView, javafx.geometry.Pos.CENTER);

        }
        @Override
        public byte[] get() {
            return value;
        }

        @Override
        public void set(byte[] value) {
            this.value = value;
            imageView.setImage(new Image(new ByteArrayInputStream(value)));
        }

        @Override
        public Node view() {
            return stackPane;
        }
    }


}
