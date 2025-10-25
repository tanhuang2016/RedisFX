package redisfx.tanh.rdm.ui.handler.view;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import redisfx.tanh.rdm.common.util.DataUtil;
import redisfx.tanh.rdm.ui.Main;
import redisfx.tanh.rdm.ui.common.Constant;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static redisfx.tanh.rdm.ui.util.LanguageManager.language;
/**
 * 抽象代码文本查看器
 * @author th
 * @version 2.3.9
 * @since 2025/9/26 22:48
 */
public abstract class AbstractAreaCodeNode implements ViewerNode, CharacterEncoding{

    protected byte[] value;

    protected Charset charset;
    protected final StackPane stackPane;
    protected final CodeArea codeArea;

    public AbstractAreaCodeNode() {
        codeArea = new CodeArea();
        codeArea.setStyle("""
                    -fx-background-color: %s;
                    -fx-border-color: %s;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                    """.formatted(Constant.THEME_COLOR_BG_DEFAULT, Constant.THEME_COLOR_BORDER_DEFAULT));
        stackPane = new StackPane();
        stackPane.getChildren().add(new VirtualizedScrollPane<>(codeArea));
        // 直接添加样式表
        stackPane.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/css/text.css")).toExternalForm());
        // 设置文本变化监听器，用于语法高亮
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            if(DataUtil.isNotBlank(newText)){
                codeArea.setStyleSpans(0, computeHighlighting(newText));
            }
        });
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

    protected abstract StyleSpans<? extends Collection<String>> computeHighlighting(String newText) ;

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
    public String text() {
        return codeArea.getText();
    }

    @Override
    public Node view() {
        return stackPane;
    }
}
