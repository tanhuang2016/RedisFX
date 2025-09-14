package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import xyz.hashdog.rdm.common.util.FileUtil;

import java.nio.charset.Charset;

public class HexViewer extends AbstractTextViewer{
    public static final String NAME="Hex";
    private final TextArea textArea;

    public HexViewer() {
        this.textArea = defaultPane();
    }



    @Override
    public byte[] get() {
        return FileUtil.hexStringToByteArray(textArea.getText());
    }

    @Override
    public void set(byte[] value) {
        textArea.setText(FileUtil.byte2HexString(value));
    }

    @Override
    public Node view() {
        return textArea;
    }

    @Override
    public boolean accept(byte[] data) {
        //不会有任何数据默认用16进制展示，一般是用户手动选择
        return false;
    }


}
