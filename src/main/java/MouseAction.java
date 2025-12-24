import java.io.Serializable;

public class MouseAction implements Serializable {
  enum Type {
    MOVE,
    PRESS,
    RELEASE
  }

  Type type;
  int x, y;
  long timestamp;
  int button;

  MouseAction(Type type, int x, int y, long timestamp, int button) {
    this.type = type;
    this.x = x;
    this.y = y;
    this.timestamp = timestamp;
    this.button = button;
  }

  public Type getType() {
    return type;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public int getButton() {
    return button;
  }

  @Override
  public String toString() {
    return String.format("%s at (%d, %d) @ %dms [button=%d]",
        type, x, y, timestamp, button);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MouseAction that = (MouseAction) o;
    return x == that.x &&
        y == that.y &&
        timestamp == that.timestamp &&
        button == that.button &&
        type == that.type;
  }

}
