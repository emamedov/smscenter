package org.eminmamedov.smscenter.receivers.smpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eminmamedov.smscenter.datamodel.User;
import org.springframework.stereotype.Component;

@Component
public class SmppConnectionPool {

    private List<SmppConnectionHandler> connectionHandlers;

    public SmppConnectionPool() {
        this.connectionHandlers = Collections.synchronizedList(new ArrayList<SmppConnectionHandler>());
    }

    public void add(SmppConnectionHandler handler) {
        connectionHandlers.add(handler);
    }

    public void close(SmppConnectionHandler handler) {
        if (handler.getBoundState() != BoundState.UNBOUND) {
            handler.setBoundState(BoundState.UNBOUND);
        }
        connectionHandlers.remove(handler);
    }

    public void closeWithNotification(SmppConnectionHandler handler) {
        if (handler.getBoundState() != BoundState.UNBOUND) {
            handler.setBoundState(BoundState.UNBOUNDING);
        }
        connectionHandlers.remove(handler);
    }

    public void closeAll() {
        for (SmppConnectionHandler handler : getConnectionHandlers()) {
            handler.setBoundState(BoundState.UNBOUNDING);
        }
        connectionHandlers.clear();
    }

    public List<SmppConnectionHandler> getConnectionHandlers() {
        return new ArrayList<SmppConnectionHandler>(connectionHandlers);
    }

    public boolean handlerExistsAlready(User user) {
        for (SmppConnectionHandler handler : getConnectionHandlers()) {
            if (user.getName().equals(handler.getUser().getName())) {
                return true;
            }
        }
        return false;
    }

}
