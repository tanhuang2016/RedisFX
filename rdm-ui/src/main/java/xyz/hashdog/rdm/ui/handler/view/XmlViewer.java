package xyz.hashdog.rdm.ui.handler.view;

import com.google.gson.JsonSyntaxException;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.ui.Main;
import xyz.hashdog.rdm.ui.common.Constant;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xyz.hashdog.rdm.ui.util.LanguageManager.language;

/**
 * xml查看器
 * @author th
 * @version 2.3.9
 * @since 2025/9/15 22:48
 */
public class XmlViewer extends AbstractTextViewer {
    public static final String NAME="Xml";



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

        // 检查是否为XML格式
        // XML应该以<?xml开头或者以<开头
        if (str.startsWith("<?xml") || str.startsWith("<")) {
            // 进一步验证是否为有效的XML结构
            String xmlStr = new String(data).trim();
            // 简单的XML验证：必须有开始和结束标签
            return xmlStr.contains("<") && xmlStr.contains(">");
        }
        return false;
    }

    @Override
    public ViewerNode newViewerNode() {
        return new XmlViewerNode();
    }

    private class XmlViewerNode implements ViewerNode, CharacterEncoding {
        private final StackPane stackPane;
        private final CodeArea codeArea;
        private Charset charset;
        private byte[] value;
        private static final Pattern XML_TAG = Pattern.compile("(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*/?>))"
                + "|(?<COMMENT><!--(.|\\v)+?-->)");

        private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");

        private static final int GROUP_OPEN_BRACKET = 2;
        private static final int GROUP_ELEMENT_NAME = 3;
        private static final int GROUP_ATTRIBUTES_SECTION = 4;
        private static final int GROUP_CLOSE_BRACKET = 5;
        private static final int GROUP_ATTRIBUTE_NAME = 1;
        private static final int GROUP_EQUAL_SYMBOL = 2;
        private static final int GROUP_ATTRIBUTE_VALUE = 3;

        public XmlViewerNode() {
            codeArea = new CodeArea();
            codeArea.setStyle("""
                    -fx-background-color: %s;
                    -fx-border-color: %s;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                    """.formatted(Constant.THEME_COLOR_BG_DEFAULT, Constant.THEME_COLOR_BORDER_DEFAULT));

            // 设置文本变化监听器，用于语法高亮
            codeArea.textProperty().addListener((obs, oldText, newText) -> codeArea.setStyleSpans(0, computeHighlighting(newText)));
            stackPane = new StackPane();
            stackPane.getChildren().add(new VirtualizedScrollPane<>(codeArea));
            // 直接添加样式表
            stackPane.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/css/text.css")).toExternalForm());
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
            return DataUtil.json2Byte(codeArea.getText(), charset, false);
        }

        @Override
        public void set(byte[] value) {
            this.value = value;
            try {
                codeArea.replaceText(DataUtil.formatJson(value, charset, true));
            } catch (JsonSyntaxException e) {
                codeArea.replaceText(new String(value, charset));
            }

        }

        @Override
        public String text() {
            return codeArea.getText();
        }

        @Override
        public Node view() {
            return stackPane;
        }

        @Override
        public List<MenuItem> options() {
            RadioMenuItem showLineNumber = new RadioMenuItem(language("key.string.viewer.options.showLine"));
            showLineNumber.setOnAction(event -> {
                if (showLineNumber.isSelected()) {
                    codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
                } else {
                    codeArea.setParagraphGraphicFactory(null);
                }
            });
            return List.of(showLineNumber);
        }

        /**
         * 计算并返回文本的语法高亮样式
         *
         * @param text 需要高亮的文本
         * @return 样式跨度集合
         */
        private static StyleSpans<Collection<String>> computeHighlighting(String text) {

            Matcher matcher = XML_TAG.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            while (matcher.find()) {

                spansBuilder.add(Collections.singleton("default"), matcher.start() - lastKwEnd);
                if (matcher.group("COMMENT") != null) {
                    spansBuilder.add(Collections.singleton("comment"), matcher.end() - matcher.start());
                } else {
                    if (matcher.group("ELEMENT") != null) {
                        String attributesText = matcher.group(GROUP_ATTRIBUTES_SECTION);

                        spansBuilder.add(Collections.singleton("tagmark"), matcher.end(GROUP_OPEN_BRACKET) - matcher.start(GROUP_OPEN_BRACKET));
                        spansBuilder.add(Collections.singleton("anytag"), matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET));

                        if (!attributesText.isEmpty()) {

                            lastKwEnd = 0;

                            Matcher amatcher = ATTRIBUTES.matcher(attributesText);
                            while (amatcher.find()) {
                                spansBuilder.add(Collections.singleton("other2"), amatcher.start() - lastKwEnd);
                                spansBuilder.add(Collections.singleton("attribute"), amatcher.end(GROUP_ATTRIBUTE_NAME) - amatcher.start(GROUP_ATTRIBUTE_NAME));
                                spansBuilder.add(Collections.singleton("tagmark"), amatcher.end(GROUP_EQUAL_SYMBOL) - amatcher.end(GROUP_ATTRIBUTE_NAME));
                                spansBuilder.add(Collections.singleton("avalue"), amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
                                lastKwEnd = amatcher.end();
                            }
                            if (attributesText.length() > lastKwEnd)
                                spansBuilder.add(Collections.singleton("other3"), attributesText.length() - lastKwEnd);
                        }

                        lastKwEnd = matcher.end(GROUP_ATTRIBUTES_SECTION);

                        spansBuilder.add(Collections.singleton("tagmark"), matcher.end(GROUP_CLOSE_BRACKET) - lastKwEnd);
                    }
                }
                lastKwEnd = matcher.end();
            }
            spansBuilder.add(Collections.singleton("other4"), text.length() - lastKwEnd);
            return spansBuilder.create();
        }

    }


}
