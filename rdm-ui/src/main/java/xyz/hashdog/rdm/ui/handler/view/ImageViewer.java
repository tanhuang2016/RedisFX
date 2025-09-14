package xyz.hashdog.rdm.ui.handler.view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import xyz.hashdog.rdm.common.util.FileUtil;

import java.io.ByteArrayInputStream;

public class ImageViewer implements ValueViewer{

    private final StackPane stackPane;
    private final ImageView imageView;
    private byte[] value;

    public ImageViewer() {
        stackPane = new StackPane();
        stackPane.setPadding(new Insets(10));
        stackPane.setPrefHeight(500);
        stackPane.setPrefWidth(500);
        imageView = new ImageView();
        stackPane.getChildren().add(imageView);
        // 设置ImageView居中对齐
        StackPane.setAlignment(imageView, javafx.geometry.Pos.CENTER);;

    }

    @Override
    public ViewerTypeEnum getType() {
        return ViewerTypeEnum.Binary;
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

    @Override
    public boolean accept(byte[] data) {
        //不会有任何数据默认用2进制展示，一般是用户手动选择
        return false;
    }
}
