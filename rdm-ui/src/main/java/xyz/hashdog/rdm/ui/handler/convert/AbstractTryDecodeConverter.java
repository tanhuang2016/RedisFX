package xyz.hashdog.rdm.ui.handler.convert;


/**
 * 需要通过尝试解码，来判断当前解码器是否可用
 * 会缓存尝试解码后的数据，避免二次解码的消耗
 * @author th
 * @version 2.3.6
 * @since 2025/9/17 22:48
 */
public abstract class AbstractTryDecodeConverter implements ValueConverter{
    private volatile byte[] decode;

    @Override
    public byte[] decode(byte[] data) {
        if (decode!=null){
            byte[] decode=this.decode;
            this.decode=null;
            return decode;
        }
        return doDecode(data);
    }

    /**
     * 执行解码
     * @param data 原始数据
     * @return 解码数据
     */
    protected abstract byte[] doDecode(byte[] data);

    @Override
    public boolean accept(byte[] data) {
        try {
            this.decode= decode(data);
            return true;
        }catch (Exception e){
            return false;
        }

    }


}
