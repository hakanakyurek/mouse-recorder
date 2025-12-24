import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class PlaybackManager {
  private Robot robot;
  private boolean isPlaying;

  public PlaybackManager() {
    try {
      this.robot = new Robot();
    } catch (AWTException e) {
      throw new RuntimeException("Failed to create Robot instance", e);
    }
    this.isPlaying = false;
  }

  public void play(List<MouseAction> actions, PlaybackListener listener) {
    if (actions == null || actions.isEmpty()) {
      if (listener != null) {
        listener.onPlaybackError("No actions to play");
      }
      return;
    }

    new Thread(() -> {
      try {
        this.isPlaying = true;
        if (listener != null) {
          listener.onPlaybackStarted();
        }

        while (this.isPlaying) {

          long lastTimestamp = 0;

          for (MouseAction action : actions) {
            if (listener != null) {
              listener.onPlaybackActionExecute(action);
            }
            if (!this.isPlaying) {
              break;
            }

            long delay = action.getTimestamp() - lastTimestamp;
            if (delay > 0) {
              Thread.sleep(delay);
            }

            executeAction(action);
            lastTimestamp = action.getTimestamp();
          }

        }
        if (listener != null) {
          listener.onPlaybackCompleted();
        }

      } catch (InterruptedException e) {
        this.isPlaying = false;
        if (listener != null) {
          listener.onPlaybackError("Playback interrupted: " + e.getMessage());
        }
      }
    }).start();

  }

  private void executeAction(MouseAction action) {
    switch (action.getType()) {
      case MOVE:
        this.robot.mouseMove(action.getX(), action.getY());
        break;
      case PRESS:
        this.robot.mousePress(getButtonMask(action.getButton()));
        break;
      case RELEASE:
        this.robot.mouseRelease(getButtonMask(action.getButton()));
        break;
    }
  }

  private int getButtonMask(int button) {
    switch (button) {
      case MouseEvent.BUTTON1:
        return InputEvent.BUTTON1_DOWN_MASK;
      case MouseEvent.BUTTON2:
        // https://stackoverflow.com/a/14709224
        return InputEvent.BUTTON3_DOWN_MASK;
      default:
        return InputEvent.BUTTON1_DOWN_MASK;
    }
  }

  public void stop() {
    this.isPlaying = false;
  }

  public boolean isPlaying() {
    return this.isPlaying;
  }

  public interface PlaybackListener {
    void onPlaybackStarted();

    void onPlaybackCompleted();

    void onPlaybackError(String error);

    void onPlaybackActionExecute(MouseAction action);
  }
}
