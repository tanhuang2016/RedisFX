package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import xyz.hashdog.rdm.common.util.FileUtil;

import java.nio.charset.Charset;

public class BinaryViewer extends AbstractTextViewer{

    private final TextArea textArea;
    private byte[] value;
    public BinaryViewer() {
        this.textArea = defaultPane();
        //二进制展示，不让编辑，只能导入
        this.textArea.setEditable(false);
    }

    @Override
    public ViewerTypeEnum getType() {
        return ViewerTypeEnum.Binary;
    }


    @Override
    public byte[] get() {
//        return FileUtil.binaryStringToByteArray(textArea.getText());
        return value;
    }

    @Override
    public void set(byte[] value) {
        this.value=value;
        textArea.setText(FileUtil.byteArrayToBinaryString(value));
    }

    @Override
    public Node view() {
        return textArea;
    }

    @Override
    public boolean accept(byte[] data) {
        //不会有任何数据默认用2进制展示，一般是用户手动选择
        return false;
    }
}
