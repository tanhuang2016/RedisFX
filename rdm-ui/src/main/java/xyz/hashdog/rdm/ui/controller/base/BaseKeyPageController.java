package xyz.hashdog.rdm.ui.controller.base;


/**
 *
 *
 * 需要分页的窗口用这个基类
 * @author th
 * @version 2.3.0
 * @since 2023/7/22 10:43
 */
public abstract class BaseKeyPageController extends BaseKeyController {

    protected static final String SIZE = "Size:%dB";
    protected static final String TOTAL = "Total:%d";
    protected static final int ROWS_PER_PAGE = 32;

}
