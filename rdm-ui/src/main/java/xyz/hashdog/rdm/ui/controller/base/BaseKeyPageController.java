package xyz.hashdog.rdm.ui.controller.base;
import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Styles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.hashdog.rdm.common.util.DataUtil;
import xyz.hashdog.rdm.ui.entity.ITable;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 *
 * 需要分页的窗口用这个基类
 * @author th
 * @version 2.3.2
 * @since 2023/7/22 10:43
 */
public abstract class BaseKeyPageController<P extends ITable> extends BaseKeyController {

    protected static final String SIZE = "Size:%dB";
    protected static final String TOTAL = "Total:%d";
    protected static final int ROWS_PER_PAGE = 32;

    @FXML
    public Label total;
    @FXML
    public Label size;

    @FXML
    public CustomTextField findTextField;

    @FXML
    public Button findButton;
    @FXML
    public Pagination pagination;

    @FXML
    public TableView<P> tableView;

    /**
     * 缓存所有表格数据
     */
    protected final ObservableList<P> list = FXCollections.observableArrayList();
    /**
     * 查询后的表格数据
     */
    protected final ObservableList<P> findList = FXCollections.observableArrayList();
    /**
     * 最后选中的行缓存
     */
    protected P lastSelect;


    @Override
    void initCommon() {
        super.initCommon();
        initTextField();
        initButtonIcon();
        initButtonStyles();
        paginationListener();
    }
    /**
     * 初始化文本框
     */
    private void initTextField() {
        findTextField.setRight(findButton);
    }

    /**
     * 初始化按钮图标
     */
    private void initButtonIcon() {
        findButton.setGraphic(new FontIcon(Feather.SEARCH));
    }
    /**
     * 初始化按钮样式
     */
    private void initButtonStyles() {
        findButton.getStyleClass().addAll(Styles.BUTTON_ICON,Styles.FLAT,Styles.ROUNDED,Styles.SMALL);
        findButton.setCursor(Cursor.HAND);
    }

    /**
     * 分页监听
     * 数据显示,全靠分页监听
     */
    private void paginationListener() {
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int pageIndex = (int) newIndex;
            setCurrentPageIndex(pageIndex);

        });
    }

    /**
     * 可以手动触发分页
     *
     * @param pageIndex 页码
     */
    protected void setCurrentPageIndex(int pageIndex) {
        if (pageIndex < pagination.getPageCount() - 1) {
            List<P> pageList = findList.subList(pageIndex * ROWS_PER_PAGE, (pageIndex + 1) * ROWS_PER_PAGE + 1);
            tableView.setItems(FXCollections.observableArrayList(pageList));
        } else {
            List<P> pageList = findList.subList(pageIndex * ROWS_PER_PAGE, findList.size());
            tableView.setItems(FXCollections.observableArrayList(pageList));
        }
        tableView.refresh();
    }

    /**
     * 列表查询
     *
     * @param actionEvent 事件
     */
    @FXML
    public void find(ActionEvent actionEvent) {
        String text = this.findTextField.getText();
        List<P> newList;
        if (DataUtil.isBlank(text)) {
            text = "*";
        }
        Predicate<P> nameFilter = createNameFilter(text);
        newList = this.list.stream().filter(nameFilter).toList();
        findList.clear();
        findList.addAll(newList);
        pagination.setPageCount((int) Math.ceil((double) findList.size() / ROWS_PER_PAGE));
        //当前页就是0页才需要手动触发,否则原事件触发不了
        if (pagination.getCurrentPageIndex() == 0) {
            this.setCurrentPageIndex(0);
        }
    }
    /**
     * 创建过滤器
     *
     * @param text 搜索文本
     * @return 过滤器
     */
    protected abstract Predicate<P> createNameFilter(String text);
}
