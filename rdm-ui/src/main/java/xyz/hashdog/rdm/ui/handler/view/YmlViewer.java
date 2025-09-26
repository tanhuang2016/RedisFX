package xyz.hashdog.rdm.ui.handler.view;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YML查看器
 *
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class YmlViewer extends AbstractTextViewer {
    public static final String NAME = "Yml";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean accept(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        for (String s : new String(data).split("\n")) {
            if (s.startsWith("#") || s.startsWith("---")) {
                continue;
            }
            return s.contains(":");
        }
        return false;
    }

    @Override
    public ViewerNode newViewerNode() {
        return new YmlViewerNode();
    }

    private class YmlViewerNode extends AbstractAreaCodeNode {
        // YML语法高亮正则表达式
        private static final String YML_PATTERN =
                // 注释行（行首的注释）
                // 空值键
                // 带值的键
                // 值部分（区分有空格的注释和无空格的值）
                // 行内注释（前面必须有空格）
                "#[^\n]*" +
                        "|(?<=^|\\n)[\\s]*[a-zA-Z0-9_-]+:(?=\\s|$)" +
                        "|(?<=^|\\n)[\\s]*[a-zA-Z0-9_-]+:(?=\\s)" +
                        "|(?<=:\\s)([^\\n]*?)(?=\\s#[^\\n]*|\\n|$)" +
                        "|(?<=\\s)(#[^\\n]*)";


        private static final Pattern YAML_PATTERN = Pattern.compile(YML_PATTERN);

        public YmlViewerNode() {
            super();
        }

        @Override
        public byte[] get() {
            return codeArea.getText().getBytes(charset);
        }

        @Override
        public void set(byte[] value) {
            this.value = value;
            codeArea.replaceText(new String(value, charset));
        }

        /**
         * 计算并返回文本的语法高亮样式
         *
         * @param text 需要高亮的文本
         * @return 样式跨度集合
         */
        @Override
        protected StyleSpans<Collection<String>> computeHighlighting(String text) {
            Matcher matcher = YAML_PATTERN.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

            while (matcher.find()) {
                String styleClass = "yml-" + getStyleClass(matcher.group(), text, matcher.start());
                spansBuilder.add(Collections.singletonList("yml-default"), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }
            // 处理最后未匹配的文本部分
            if (lastKwEnd < text.length()) {
                spansBuilder.add(Collections.singletonList("yml-default"), text.length() - lastKwEnd);
            }

            return spansBuilder.create();
        }

        /**
         * 根据匹配的文本和上下文返回相应的CSS样式类
         *
         * @param group    匹配的文本组
         * @param fullText 完整文本
         * @param position 匹配位置
         * @return CSS样式类名
         */
        private static String getStyleClass(String group, String fullText, int position) {
            if (group.startsWith("#")) {
                return "comment";  // 注释
            } else if (group.matches("^[\\s]*[a-zA-Z0-9_-]+:(?=\\s|$)")) {
                // 键（可能带空值）
                return "key";
            } else if (group.matches("^[\\s]*[a-zA-Z0-9_-]+:(?=\\s)")) {
                // 带值的键
                return "key";
            }
            return "value";
        }
    }
}
