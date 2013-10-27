package org.eminmamedov.smscenter.receivers.smpp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eminmamedov.smscenter.datamodel.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SmppConnectionPoolTest {

    private SmppConnectionPool factory;
    @Mock
    private SmppConnectionHandler handler1;
    @Mock
    private SmppConnectionHandler handler2;
    @Mock
    private SmppConnectionHandler handler3;

    @Before
    public void setUp() throws Exception {
        factory = new SmppConnectionPool();

        User user1 = new User();
        user1.setName("User1");
        when(handler1.getUser()).thenReturn(user1);
        factory.add(handler1);

        User user2 = new User();
        user2.setName("User2");
        when(handler2.getUser()).thenReturn(user2);
        factory.add(handler2);

        User user3 = new User();
        user3.setName("User3");
        when(handler3.getUser()).thenReturn(user3);
        factory.add(handler3);
    }

    @Test
    public void testHandlerExistsAlreadyFalse() {
        User user = new User();
        user.setName("User4");
        assertFalse(factory.handlerExistsAlready(user));
    }

    @Test
    public void testHandlerExistsAlreadyTrue() {
        User user = new User();
        user.setName("User2");
        assertTrue(factory.handlerExistsAlready(user));
    }

    @Test
    public void testGetConnectionsHandlers() {
        List<SmppConnectionHandler> handlers = factory.getConnectionHandlers();
        assertNotNull(handlers);
        assertFalse(handlers.isEmpty());
        assertEquals(3, handlers.size());
        assertTrue(handlers.contains(handler1));
        assertTrue(handlers.contains(handler2));
        assertTrue(handlers.contains(handler3));
        assertFalse(handlers.contains(mock(SmppConnectionHandler.class)));
    }

    @Test
    public void testClose() {
        factory.close(handler2);
        verify(handler2, times(1)).setBoundState(eq(BoundState.UNBOUND));

        List<SmppConnectionHandler> handlers = factory.getConnectionHandlers();
        assertNotNull(handlers);
        assertFalse(handlers.isEmpty());
        assertEquals(2, handlers.size());
        assertTrue(handlers.contains(handler1));
        assertFalse(handlers.contains(handler2));
        assertTrue(handlers.contains(handler3));
        assertFalse(handlers.contains(mock(SmppConnectionHandler.class)));
    }

    @Test
    public void testCloseWithNotification() {
        factory.closeWithNotification(handler2);
        verify(handler2, times(1)).setBoundState(eq(BoundState.UNBOUNDING));

        List<SmppConnectionHandler> handlers = factory.getConnectionHandlers();
        assertNotNull(handlers);
        assertFalse(handlers.isEmpty());
        assertEquals(2, handlers.size());
        assertTrue(handlers.contains(handler1));
        assertFalse(handlers.contains(handler2));
        assertTrue(handlers.contains(handler3));
        assertFalse(handlers.contains(mock(SmppConnectionHandler.class)));
    }

    @Test
    public void testCloseAll() {
        factory.closeAll();
        verify(handler1, times(1)).setBoundState(eq(BoundState.UNBOUNDING));
        verify(handler2, times(1)).setBoundState(eq(BoundState.UNBOUNDING));
        verify(handler3, times(1)).setBoundState(eq(BoundState.UNBOUNDING));

        List<SmppConnectionHandler> handlers = factory.getConnectionHandlers();
        assertNotNull(handlers);
        assertTrue(handlers.isEmpty());
    }

}
