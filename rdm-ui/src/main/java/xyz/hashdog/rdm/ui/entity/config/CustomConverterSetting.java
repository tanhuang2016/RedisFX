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
    private List<CustomInvokeConverter> converters;


    @Override
    public String getName() {
        return ConfigSettingsEnum.CONVERTER.name;
    }

    @Override
    public CustomConverterSetting init() {
        this.converters = new ArrayList<>();
        return this;
    }

    public List<CustomInvokeConverter> getConverters() {
        return converters;
    }

    public void setConverters(List<CustomInvokeConverter> converters) {
        this.converters = converters;
    }

    public CustomInvokeConverter getByName(String name) {
        for (CustomInvokeConverter converter : converters) {
            if(converter.getName().equals(name)){
                return converter;
            }
        }
        return null;
    }
}
