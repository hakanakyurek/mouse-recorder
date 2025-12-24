import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecordingManagerTest {

    private RecordingManager recordingManager;
    private MockMouseEventGenerator mockMouse;

    @BeforeEach
    void init() {
        this.recordingManager = new RecordingManager();
        this.mockMouse = new MockMouseEventGenerator(recordingManager);
    }

    @AfterEach
    void cleanup() {
        recordingManager.cleanup();
    }

    private static class MockMouseEventGenerator {
        private final RecordingManager recordingManager;

        MockMouseEventGenerator(RecordingManager recordingManager) {
            this.recordingManager = recordingManager;
        }

        void move(int x, int y) {
            NativeMouseEvent event = new NativeMouseEvent(
                NativeMouseEvent.NATIVE_MOUSE_MOVED,
                0, x, y, 0, 0
            );
            recordingManager.nativeMouseMoved(event);
        }

        void drag(int x, int y) {
            NativeMouseEvent event = new NativeMouseEvent(
                NativeMouseEvent.NATIVE_MOUSE_DRAGGED,
                0, x, y, 0, 0
            );
            recordingManager.nativeMouseDragged(event);
        }

        void press(int x, int y, int button) {
            NativeMouseEvent event = new NativeMouseEvent(
                NativeMouseEvent.NATIVE_MOUSE_PRESSED,
                0, x, y, 0, button
            );
            recordingManager.nativeMousePressed(event);
        }

        void release(int x, int y, int button) {
            NativeMouseEvent event = new NativeMouseEvent(
                NativeMouseEvent.NATIVE_MOUSE_RELEASED,
                0, x, y, 0, button
            );
            recordingManager.nativeMouseReleased(event);
        }

        void click(int x, int y, int button) {
            press(x, y, button);
            release(x, y, button);
        }
    }

    @Test
    void testIsRecordingInitiallyFalse() {
        assertFalse(recordingManager.isRecording());
    }

    @Test
    void testStartRecording() {
        recordingManager.startRecording();

        assertTrue(recordingManager.isRecording());
    }

    @Test
    void testStopRecording() {
        recordingManager.startRecording();
        recordingManager.stopRecording();

        assertFalse(recordingManager.isRecording());
    }

    @Test
    void testGetActionsInitiallyEmpty() {
        ArrayList<MouseAction> actions = recordingManager.getActions();

        assertNotNull(actions);
        assertTrue(actions.isEmpty());
    }

    @Test
    void testSetActions() {
        List<MouseAction> actions = new ArrayList<>();
        actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 200, 1000L, 0));
        actions.add(new MouseAction(MouseAction.Type.PRESS, 150, 250, 2000L, 1));

        recordingManager.setActions(actions);

        assertEquals(2, recordingManager.getActionCount());
        assertEquals(actions.get(0), recordingManager.getActions().get(0));
        assertEquals(actions.get(1), recordingManager.getActions().get(1));
    }

    @Test
    void testClear() {
        List<MouseAction> actions = new ArrayList<>();
        actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 200, 1000L, 0));
        recordingManager.setActions(actions);

        recordingManager.clear();

        assertEquals(0, recordingManager.getActionCount());
        assertTrue(recordingManager.getActions().isEmpty());
    }

    @Test
    void testStartRecordingClearsExistingActions() {
        List<MouseAction> actions = new ArrayList<>();
        actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 200, 1000L, 0));
        recordingManager.setActions(actions);

        recordingManager.startRecording();

        assertEquals(0, recordingManager.getActionCount());
    }

    @Test
    void testGetActionsReturnsNewList() {
        List<MouseAction> actions = new ArrayList<>();
        actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 200, 1000L, 0));
        recordingManager.setActions(actions);

        ArrayList<MouseAction> retrieved = recordingManager.getActions();
        retrieved.clear();

        assertEquals(1, recordingManager.getActionCount());
    }

    @Test
    void testSetActionsCopiesList() {
        List<MouseAction> actions = new ArrayList<>();
        actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 200, 1000L, 0));
        recordingManager.setActions(actions);

        actions.clear();

        assertEquals(1, recordingManager.getActionCount());
    }

    @Test
    void testMouseMoveNotRecordedWhenNotRecording() {
        mockMouse.move(100, 200);

        assertEquals(0, recordingManager.getActionCount());
    }

    @Test
    void testMouseMoveRecordedWhenRecording() {
        recordingManager.startRecording();
        mockMouse.move(100, 200);

        assertEquals(1, recordingManager.getActionCount());
        MouseAction action = recordingManager.getActions().get(0);
        assertEquals(MouseAction.Type.MOVE, action.getType());
        assertEquals(100, action.getX());
        assertEquals(200, action.getY());
    }

    @Test
    void testMouseDragRecordedWhenRecording() {
        recordingManager.startRecording();
        mockMouse.drag(150, 250);

        assertEquals(1, recordingManager.getActionCount());
        MouseAction action = recordingManager.getActions().get(0);
        assertEquals(MouseAction.Type.MOVE, action.getType());
        assertEquals(150, action.getX());
        assertEquals(250, action.getY());
    }

    @Test
    void testMousePressRecordedWhenRecording() {
        recordingManager.startRecording();
        mockMouse.press(100, 200, 1);

        assertEquals(1, recordingManager.getActionCount());
        MouseAction action = recordingManager.getActions().get(0);
        assertEquals(MouseAction.Type.PRESS, action.getType());
        assertEquals(100, action.getX());
        assertEquals(200, action.getY());
        assertEquals(1, action.getButton());
    }

    @Test
    void testMouseReleaseRecordedWhenRecording() {
        recordingManager.startRecording();
        mockMouse.release(100, 200, 1);

        assertEquals(1, recordingManager.getActionCount());
        MouseAction action = recordingManager.getActions().get(0);
        assertEquals(MouseAction.Type.RELEASE, action.getType());
        assertEquals(100, action.getX());
        assertEquals(200, action.getY());
        assertEquals(1, action.getButton());
    }

    @Test
    void testClickRecordsPressAndRelease() {
        recordingManager.startRecording();
        mockMouse.click(100, 200, 1);

        assertEquals(2, recordingManager.getActionCount());
        assertEquals(MouseAction.Type.PRESS, recordingManager.getActions().get(0).getType());
        assertEquals(MouseAction.Type.RELEASE, recordingManager.getActions().get(1).getType());
    }

    @Test
    void testMultipleMovesRecorded() {
        recordingManager.startRecording();
        mockMouse.move(100, 100);
        mockMouse.move(200, 200);
        mockMouse.move(300, 300);

        assertEquals(3, recordingManager.getActionCount());
    }

    @Test
    void testEventsNotRecordedAfterStop() {
        recordingManager.startRecording();
        mockMouse.move(100, 100);
        recordingManager.stopRecording();
        mockMouse.move(200, 200);

        assertEquals(1, recordingManager.getActionCount());
    }

    @Test
    void testTimestampIsRecorded() {
        recordingManager.startRecording();
        mockMouse.move(100, 100);

        MouseAction action = recordingManager.getActions().get(0);
        assertTrue(action.getTimestamp() >= 0);
    }
}
