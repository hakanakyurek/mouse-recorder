import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class MouseActionTest {

  @Test
  void testEquals() {
    MouseAction action1 = new MouseAction(MouseAction.Type.PRESS, 100, 200, System.currentTimeMillis(), 1);
    MouseAction action2 = new MouseAction(MouseAction.Type.MOVE, 100, 200, System.currentTimeMillis(), 1);
    MouseAction action3 = new MouseAction(MouseAction.Type.MOVE, 100, 200, System.currentTimeMillis(), 1);

    assertNotEquals(action1, action2);
    assertEquals(action2, action2);
    assertEquals(action3, action2);
  }
}
