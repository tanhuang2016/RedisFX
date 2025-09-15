package xyz.hashdog.rdm.ui.handler.view;

import com.google.gson.JsonSyntaxException;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import xyz.hashdog.rdm.common.util.DataUtil;

import java.nio.charset.Charset;

public class JsonViewer extends AbstractTextViewer {
    public static final String NAME="JSON";



    @Override
    public String name() {
        return NAME;
    }
    @Override
    public boolean accept(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        // 只检查前100个字节以提高性能
        int checkLength = Math.min(data.length, 100);
        String str = new String(data, 0, checkLength).trim();
        //检查开头
        if(str.startsWith("{")||str.startsWith("[")){
            // 尝试解析整个JSON数据来确认
            String jsonStr = new String(data).trim();
            // 可能是JSON对象
            if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                return true;
            }
            // 可能是JSON数组
            return jsonStr.startsWith("[") && jsonStr.endsWith("]");
        }
        return false;
    }

    @Override
    public ViewerNode newViewerNode() {
        return new JsonViewerNode();
    }

    class JsonViewerNode implements ViewerNode, CharacterEncoding {

        private final TextArea textArea;
        private Charset charset;
        private byte[] value;

        public JsonViewerNode() {
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
            return DataUtil.json2Byte(textArea.getText(),charset,false);
        }

        @Override
        public void set(byte[] value) {
            this.value=value;
            try {
                textArea.setText(DataUtil.formatJson(value,charset,true));
            }catch (JsonSyntaxException e){
                textArea.setText(new String(value,charset));
            }

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
