package xyz.hashdog.rdm.ui.handler.convert;

import org.junit.Test;
import xyz.hashdog.rdm.common.util.FileUtil;

public class CustomInvokeConverterTest {

    @Test
    public void invoke() {
        CustomInvokeConverter.Invoker invoker = new CustomInvokeConverter.Invoker("D:\\soft\\miniconda3\\envs\\python3.11\\python.exe      E:\\code\\pycode\\bilibili\\stdio.py", null, true);
        byte[] bytes = FileUtil.file2byte("C:\\Users\\Administrator\\Desktop\\test.png");
        byte[] invoke = invoker.invoke(bytes);
        FileUtil.byteWrite2file(invoke, "C:\\Users\\Administrator\\Desktop\\test2.png");

    }
}
