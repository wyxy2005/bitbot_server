package bitbot.handler.mina;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.core.session.IoSession;

public class MapleCodecFactory implements ProtocolCodecFactory {

    private final ProtocolEncoder encoder = new MaplePacketEncoder();
    private final ProtocolDecoder decoder = new PacketDecoder();

    @Override
    public ProtocolEncoder getEncoder(IoSession is) throws Exception {
	return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession is) throws Exception {
	return decoder;
    }
}
