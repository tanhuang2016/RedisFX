package xyz.hashdog.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.TextArea;

import java.nio.charset.Charset;

public class TextViewer extends AbstractTextViewer {

    public static final String NAME="Text";


    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean accept(byte[] data) {
        return true;
    }

    @Override
    public ViewerNode newViewerNode() {
        return new TextViewerNode();
    }
    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    class TextViewerNode implements ViewerNode,CharacterEncoding{
        private final TextArea textArea;
        private Charset charset;
        private byte[] value;

        public TextViewerNode() {
            this.textArea = defaultPane();
        }

        @Override
        public void change(Charset charset) {
            init(charset);
            set(value);
        }

        @Override
        public void init(Charset charset) {
            this.charset = charset;
        }
        @Override
        public byte[] get() {
            return textArea.getText().getBytes(charset);
        }

        @Override
        public void set(byte[] value) {
            this.value=value;
            textArea.setText(new String(value,charset));
        }

        @Override
        public String text() {
            return textArea.getText();
        }

        @Override
        public Node view() {
            return textArea;
        }
    }


}
