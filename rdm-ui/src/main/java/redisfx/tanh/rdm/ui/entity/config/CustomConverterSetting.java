package redisfx.tanh.rdm.ui.entity.config;

import redisfx.tanh.rdm.ui.common.ConfigSettingsEnum;
import redisfx.tanh.rdm.ui.handler.convert.CustomInvokeConverter;

import java.util.ArrayList;
import java.util.List;

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
