package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import xyz.hashdog.rdm.common.util.FileUtil;

import java.nio.charset.Charset;

public class HexViewer extends AbstractTextViewer{

    private final TextArea textArea;
    private Charset charset;

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
        return true;
    }
}
