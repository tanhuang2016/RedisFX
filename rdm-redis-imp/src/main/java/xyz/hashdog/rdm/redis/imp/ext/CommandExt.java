package xyz.hashdog.rdm.redis.imp.ext;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum CommandExt implements ProtocolCommand {
    QUIT
    ;

    private final byte[] raw;

    private CommandExt() {
        raw = SafeEncoder.encode(name());
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }

}
