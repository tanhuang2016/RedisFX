package redisfx.tanh.rdm.ui.entity.config;

public interface ConfigSettings {

    String getName();

    ConfigSettings init();

    int getVersion();

    void setVersion(int version);

}
