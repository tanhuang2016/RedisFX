package redisfx.tanh.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.TextArea;
import redisfx.tanh.rdm.common.util.FileUtil;


/**
 * 16进制查看器
 *
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class HexViewer extends AbstractTextViewer {
    public static final String NAME = "Hex";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean accept(byte[] data) {
        //不会有任何数据默认用16进制展示，一般是用户手动选择
        return false;
    }

    @Override
    public ViewerNode newViewerNode() {
        return new HexViewerNode();
    }

    private class HexViewerNode implements ViewerNode {
        private final TextArea textArea;

        public HexViewerNode() {
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
    }


}
