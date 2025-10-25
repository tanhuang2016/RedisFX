package redisfx.tanh.rdm.ui.handler.view;

import com.google.gson.JsonSyntaxException;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import redisfx.tanh.rdm.common.util.DataUtil;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * json查看器
 * @author th
 * @version 2.3.5
 * @since 2025/9/13 22:48
 */
public class JsonViewer extends AbstractTextViewer {
    public static final String NAME="Json";



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

    private class JsonViewerNode extends AbstractAreaCodeNode {
        private static final String JSON_KEYWORD = "\"(?:[^\"\\\\]|\\\\.)*\"|\\b(?:true|false|null)\\b|[{}\\[\\]:,]|-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?";

        private static final Pattern JSON_PATTERN = Pattern.compile(JSON_KEYWORD);

        public JsonViewerNode() {
            super();
        }



        @Override
        public byte[] get() {
            return DataUtil.json2Byte(codeArea.getText(),charset,false);
        }

        @Override
        public void set(byte[] value) {
            this.value=value;
            try {
                codeArea.replaceText(DataUtil.formatJson(value,charset,true));
            }catch (JsonSyntaxException e){
                String s = new String(value, charset);
                codeArea.replaceText(DataUtil.ifEmpty(s,""));
            }

        }





        /**
         * 计算并返回文本的语法高亮样式
         * @param text 需要高亮的文本
         * @return 样式跨度集合
         */
        @Override
        protected  StyleSpans<Collection<String>> computeHighlighting(String text) {
            Matcher matcher = JSON_PATTERN.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

            while (matcher.find()) {
                String styleClass = "json-"+getStyleClass(matcher.group(),text,matcher.start());
                spansBuilder.add(Collections.singletonList("json-warn"), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }
            // 处理最后未匹配的文本部分
            if (lastKwEnd < text.length()) {
                spansBuilder.add(Collections.singletonList("json-warn"), text.length() - lastKwEnd);
            }
//            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);

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
            } else if ("true".equals(group) || "false".equals(group)) {
                return "boolean";
            } else if ("null".equals(group)) {
                return "null";
            } else if (group.matches("-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")) {
                return "number";
            } else if ("{".equals(group) || "}".equals(group) || "[".equals(group) || "]".equals(group)) {
                return "bracket";
            } else if (":".equals(group)) {
                return "colon";
            } else if (",".equals(group)) {
                return "comma";
            }
            return "other";
        }
    }



}
