package xyz.hashdog.rdm.ui.handler;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import xyz.hashdog.rdm.common.util.DataUtil;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/8 22:48
 */
public class TextJsonConvertHandler implements ValueConvertHandler{

    @Override
    public byte[] text2Byte(String text, Charset charset) {
        return DataUtil.json2Byte(text,charset,false);
    }

    @Override
    public String byte2Text(byte[] bytes, Charset charset) {
        if(DataUtil.isBlank(new String(bytes))){
            return "";
        }
        return DataUtil.formatJson(bytes,charset,true);
    }

    private static final String JSON_KEYWORD = "\"(?:[^\"\\\\]|\\\\.)*\"|\\b(?:true|false|null)\\b|[{}\\[\\]:,]|-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";

    private static final Pattern JSON_PATTERN = Pattern.compile(JSON_KEYWORD);
    @Override
    public Parent view(byte[] bytes, Charset charset) {
        String json = this.byte2Text(bytes, charset);
        CodeArea codeArea = new CodeArea();
        codeArea.setStyle("-fx-background-color: red");

        // 设置文本变化监听器，用于语法高亮
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });

        codeArea.replaceText(json);

        StackPane root = new StackPane();
        root.getChildren().add(codeArea);
        // 直接添加样式表
        root.getStylesheets().add(TextJsonConvertHandler.class.getResource("/css/json-highlighting.css").toExternalForm());

        return root;
    }

    @Override
    public boolean isView() {
        return false;
    }

    /**
     * 计算并返回文本的语法高亮样式
     * @param text 需要高亮的文本
     * @return 样式跨度集合
     */
    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = JSON_PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = getStyleClass(matcher.group(),text,matcher.start());
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);

        return spansBuilder.create();
    }

    /**
     * 根据匹配的文本和上下文返回相应的CSS样式类
     * @param group 匹配的文本组
     * @param fullText 完整文本
     * @param position 匹配位置
     * @return CSS样式类名
     */
    private static String getStyleClass(String group, String fullText, int position) {
        if (group.startsWith("\"") && group.endsWith("\"")) {
            // 检查是否为键名（后面跟着冒号）
            // 查找后面最近的非空白字符
            for (int i = position + group.length(); i < fullText.length(); i++) {
                char c = fullText.charAt(i);
                if (c == ':') {
                    return "key";
                } else if (!Character.isWhitespace(c)) {
                    break;
                }
            }
            return "value";
        } else if (group.equals("true") || group.equals("false")) {
            return "boolean";
        } else if (group.equals("null")) {
            return "null";
        } else if (group.matches("-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")) {
            return "number";
        } else if (group.equals("{") || group.equals("}") || group.equals("[") || group.equals("]")) {
            return "bracket";
        } else if (group.equals(":")) {
            return "colon";
        } else if (group.equals(",")) {
            return "comma";
        }
        return "text";
    }


}
