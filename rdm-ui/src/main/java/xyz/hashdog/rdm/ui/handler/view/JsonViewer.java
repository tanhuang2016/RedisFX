package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import xyz.hashdog.rdm.common.util.DataUtil;

import java.nio.charset.Charset;

public class JsonViewer extends AbstractTextViewer{

    private final TextArea textArea;
    private Charset charset;

    public JsonViewer() {
        this.textArea = defaultPane();
    }

    @Override
    public void change(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] get() {
        return DataUtil.json2Byte(textArea.getText(),charset,false);
    }

    @Override
    public void set(byte[] value) {
        textArea.setText(DataUtil.formatJson(value,charset,true));
    }

    @Override
    public Node view() {
        return textArea;
    }
}
