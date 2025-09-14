package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.TextArea;

import java.nio.charset.Charset;

public class TextViewer extends AbstractTextViewer{

    private final TextArea textArea;
    private Charset charset;

    public TextViewer() {
        this.textArea = defaultPane();
    }

    @Override
    public void change(Charset charset) {
        this.charset = charset;
    }

    @Override
    public byte[] get() {
        return textArea.getText().getBytes(charset);
    }

    @Override
    public void set(byte[] value) {
        textArea.setText(new String(value,charset));
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
