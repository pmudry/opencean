package org.enocean.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.enocean.java.address.EnoceanId;
import org.enocean.java.address.EnoceanParameterAddress;
import org.enocean.java.common.EEPId;
import org.enocean.java.common.ParameterValueChangeListener;
import org.enocean.java.common.values.Value;
import org.enocean.java.eep.EEPParser;
import org.enocean.java.eep.EEPParserFactory;
import org.enocean.java.packets.BasicPacket;
import org.enocean.java.packets.RadioPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterChangeNotifier implements EnoceanReceiver {

    private static Logger logger = LoggerFactory.getLogger(ParameterChangeNotifier.class);

    private List<ParameterValueChangeListener> valueChangeListeners = new ArrayList<ParameterValueChangeListener>();
    private Map<EnoceanId, EEPId> deviceToEEP = new HashMap<EnoceanId, EEPId>();
    private EEPParserFactory parserFactory = new EEPParserFactory();

    public void addDeviceProfile(EnoceanId id, EEPId epp) {
        deviceToEEP.put(id, epp);
    }

    public void addParameterValueChangeListener(ParameterValueChangeListener listener) {
        valueChangeListeners.add(listener);
    }

    public void removeParameterValueChangeListener(ParameterValueChangeListener listener) {
        valueChangeListeners.remove(listener);
    }

    @Override
    public void receivePacket(BasicPacket packet) {
        if (packet instanceof RadioPacket) {
            RadioPacket radioPacket = (RadioPacket) packet;
            Map<EnoceanParameterAddress, Value> values = retrieveValue(radioPacket);
            for (EnoceanParameterAddress address : values.keySet()) {
                for (ParameterValueChangeListener listener : valueChangeListeners) {
                    listener.valueChanged(address, values.get(address));
                }
            }
        }
    }

    private Map<EnoceanParameterAddress, Value> retrieveValue(RadioPacket radioPacket) {
        if (deviceToEEP.containsKey(radioPacket.getSenderId())) {
            EEPId profile = deviceToEEP.get(radioPacket.getSenderId());
            EEPParser parser = parserFactory.getParserFor(profile);
            if (profile != null && parser != null) {
                return parser.parsePacket(radioPacket);
            } else {
                logger.info("Device with id=" + radioPacket.getSenderId() + " and eep=" + profile
                        + " is not properly configured or not supported.");
            }
        }
        return new HashMap<EnoceanParameterAddress, Value>();
    }

}
