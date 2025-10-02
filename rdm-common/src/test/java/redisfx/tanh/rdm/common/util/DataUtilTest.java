package redisfx.tanh.rdm.common.util;

import org.junit.Test;

import java.util.List;
import java.util.Locale;

/**
 * @author th
 * @version 1.0.0
 * @since 2023/8/6 23:40
 */
public class DataUtilTest {

    @Test
    public void getFonts() {
        List<String> list= DataUtil.getFonts(Locale.CHINA);
        System.out.println(list);

    }
}
