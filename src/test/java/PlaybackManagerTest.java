import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlaybackManagerTest {

  private PlaybackManager playbackManager;
  private TestPlaybackListener listener;

  @BeforeEach
  void init() {
    this.playbackManager = new PlaybackManager();
    this.listener = new TestPlaybackListener();
  }

  @Test
  void testPlayWithNullActions() {
    playbackManager.play(null, listener);

    assertEquals("No actions to play", listener.lastError);
    assertFalse(listener.started);
    assertFalse(listener.completed);
  }

  @Test
  void testPlayWithEmptyActions() {
    List<MouseAction> emptyActions = new ArrayList<>();

    playbackManager.play(emptyActions, listener);

    assertEquals("No actions to play", listener.lastError);
    assertFalse(listener.started);
    assertFalse(listener.completed);
  }

  @Test
  void testPlayWithNullListener() {
    assertDoesNotThrow(() -> playbackManager.play(null, null));
    assertDoesNotThrow(() -> playbackManager.play(new ArrayList<>(), null));
  }

  @Test
  void testIsPlayingInitiallyFalse() {
    assertFalse(playbackManager.isPlaying());
  }

  @Test
  void testStop() {
    playbackManager.stop();

    assertFalse(playbackManager.isPlaying());
  }

  @Test
  void testPlayStartsPlayback() throws InterruptedException {
    ArrayList<MouseAction> actions = new ArrayList<>();
    actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 100, 0L, 0));

    playbackManager.play(actions, listener);
    Thread.sleep(50);

    assertTrue(listener.started);
    assertTrue(playbackManager.isPlaying());

    playbackManager.stop();
  }

  @Test
  void testPlayPlayback() throws InterruptedException {
    ArrayList<MouseAction> actions = new ArrayList<>();
    actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 100, 0L, 0));
    actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 100, 0L, 0));
    actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 100, 0L, 0));

    playbackManager.play(actions, listener);
    Thread.sleep(50);

    assertTrue(listener.started);
    assertTrue(playbackManager.isPlaying());

    playbackManager.stop();

    assertEquals(actions.size(), listener.actions.size());
    for (int i = 0; i < actions.size(); i++) {
      assertTrue(listener.actions.contains(actions.get(i)));
    }

  }

  @Test
  void testStopDuringPlayback() throws InterruptedException {
    ArrayList<MouseAction> actions = new ArrayList<>();
    actions.add(new MouseAction(MouseAction.Type.MOVE, 100, 100, 0L, 0));
    actions.add(new MouseAction(MouseAction.Type.MOVE, 200, 200, 1000L, 0));

    playbackManager.play(actions, listener);
    Thread.sleep(50);
    playbackManager.stop();
    Thread.sleep(50);

    assertFalse(playbackManager.isPlaying());

    playbackManager.play(actions, listener);
    Thread.sleep(50);

    assertTrue(playbackManager.isPlaying());
  }

  private static class TestPlaybackListener implements PlaybackManager.PlaybackListener {
    boolean started = false;
    boolean completed = false;
    String lastError = null;
    HashSet<MouseAction> actions = new HashSet<MouseAction>();

    @Override
    public void onPlaybackStarted() {
      this.started = true;
    }

    @Override
    public void onPlaybackCompleted() {
      this.completed = true;
    }

    @Override
    public void onPlaybackError(String error) {
      this.lastError = error;
    }

    @Override
    public void onPlaybackActionExecute(MouseAction action) {
      this.actions.add(action);
    }
  }
}
