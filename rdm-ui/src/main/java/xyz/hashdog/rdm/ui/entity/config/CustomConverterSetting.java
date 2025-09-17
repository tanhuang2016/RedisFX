package xyz.hashdog.rdm.ui.entity.config;

import xyz.hashdog.rdm.ui.common.ConfigSettingsEnum;
import xyz.hashdog.rdm.ui.handler.convert.CustomInvokeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomConverterSetting implements ConfigSettings{

    /**
     * 连接超时
     */
    private List<CustomInvokeConverter> list;


    @Override
    public String getName() {
        return ConfigSettingsEnum.CONVERTER.name;
    }

    @Override
    public CustomConverterSetting init() {
        this.list = new ArrayList<>();
        return this;
    }

    public List<CustomInvokeConverter> getList() {
        return list;
    }

    public void setList(List<CustomInvokeConverter> list) {
        this.list = list;
    }
}
