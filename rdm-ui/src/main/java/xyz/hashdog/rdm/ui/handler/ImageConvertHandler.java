package xyz.hashdog.rdm.ui.handler;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import xyz.hashdog.rdm.common.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
 * 属于二进制类型
 * @author th
 * @version 1.0.0
 * @since 2023/8/8 22:48
 */
@Deprecated
public class ImageConvertHandler implements ValueConvertHandler{

    @Override
    public byte[] text2Byte(String text, Charset charset) {
        return FileUtil.binaryStringToByteArray(text);
    }

    @Override
    public String byte2Text(byte[] bytes, Charset charset) {
        return FileUtil.byteArrayToBinaryString(bytes);
    }

    @Override
    public Parent view(byte[] bytes, Charset charset) {
        // 创建StackPane并将ImageView添加到其中
        StackPane stackPane = new StackPane();
        stackPane.setPadding(new Insets(10));
        stackPane.setPrefHeight(500);
        stackPane.setPrefWidth(500);
        ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(bytes)));
        stackPane.getChildren().add(imageView);
        // 设置ImageView居中对齐
        StackPane.setAlignment(imageView, javafx.geometry.Pos.CENTER);;

        return stackPane;
    }

    @Override
    public boolean isView() {
        return true;
    }
}
