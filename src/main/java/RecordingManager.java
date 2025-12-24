import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordingManager implements NativeMouseInputListener {
  private List<MouseAction> actions;
  private boolean isRecording;
  private long startTime;

  public RecordingManager() {
    this.actions = new ArrayList<>();
    this.isRecording = false;

    Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);
    logger.setUseParentHandlers(false);

    try {
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException e) {
      throw new RuntimeException("Failed to register native hook", e);
    }

    GlobalScreen.addNativeMouseListener(this);
    GlobalScreen.addNativeMouseMotionListener(this);
  }

  @Override
  public void nativeMouseMoved(NativeMouseEvent e) {
    if (!this.isRecording)
      return;

    Point currentPoint = new Point(e.getX(), e.getY());
    long timestamp = System.currentTimeMillis() - this.startTime;

    synchronized (this) {
      this.actions.add(new MouseAction(MouseAction.Type.MOVE,
          currentPoint.x, currentPoint.y, timestamp, 0));
    }
  }

  @Override
  public void nativeMouseDragged(NativeMouseEvent e) {
    if (!this.isRecording)
      return;

    Point currentPoint = new Point(e.getX(), e.getY());
    long timestamp = System.currentTimeMillis() - this.startTime;

    synchronized (this) {
      this.actions.add(new MouseAction(MouseAction.Type.MOVE,
          currentPoint.x, currentPoint.y, timestamp, 0));
    }
  }

  @Override
  public void nativeMousePressed(NativeMouseEvent e) {
    if (!this.isRecording)
      return;

    Point currentPoint = new Point(e.getX(), e.getY());
    long timestamp = System.currentTimeMillis() - this.startTime;

    synchronized (this) {
      this.actions.add(new MouseAction(MouseAction.Type.PRESS,
          currentPoint.x, currentPoint.y, timestamp, e.getButton()));
    }
  }

  @Override
  public void nativeMouseReleased(NativeMouseEvent e) {
    if (!this.isRecording)
      return;

    Point currentPoint = new Point(e.getX(), e.getY());
    long timestamp = System.currentTimeMillis() - this.startTime;

    synchronized (this) {
      this.actions.add(new MouseAction(MouseAction.Type.RELEASE,
          currentPoint.x, currentPoint.y, timestamp, e.getButton()));
    }
  }

  public synchronized void startRecording() {
    this.actions.clear();
    this.startTime = System.currentTimeMillis();
    this.isRecording = true;
  }

  public synchronized void stopRecording() {
    this.isRecording = false;
  }

  public boolean isRecording() {
    return this.isRecording;
  }

  public synchronized ArrayList<MouseAction> getActions() {
    return new ArrayList<>(this.actions);
  }

  public synchronized void setActions(List<MouseAction> actions) {
    this.actions = new ArrayList<>(actions);
  }

  public synchronized void clear() {
    this.actions.clear();
  }

  public synchronized int getActionCount() {
    return this.actions.size();
  }

  public void cleanup() {
    try {
      GlobalScreen.removeNativeMouseListener(this);
      GlobalScreen.removeNativeMouseMotionListener(this);
      GlobalScreen.unregisterNativeHook();
    } catch (NativeHookException e) {
      e.printStackTrace();
    }
  }
}
