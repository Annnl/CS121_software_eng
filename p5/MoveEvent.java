import java.util.*;

public class MoveEvent implements Event {
  public final Train t; public final Station s1, s2;
  public MoveEvent(Train t, Station s1, Station s2) {
    this.t = t; this.s1 = s1; this.s2 = s2;
  }
  public boolean equals(Object o) {
    if (o instanceof MoveEvent e) {
      return t.equals(e.t) && s1.equals(e.s1) && s2.equals(e.s2);
    }
    return false;
  }
  public int hashCode() {
    return Objects.hash(t, s1, s2);
  }
  public String toString() {
    return "Train " + t + " moves from " + s1 + " to " + s2;
  }
  public List<String> toStringList() {
    return List.of(t.toString(), s1.toString(), s2.toString());
  }
  public void replayAndCheck(MBTA mbta) {
    Station currentLocation = mbta.getTrainLocation(t);
    
    if (!currentLocation.equals(s1)) {
      throw new RuntimeException();
    }

    List<Station> line = mbta.getTrainLine(t);
    int currentPos = mbta.getTrainPosition(t);
    int direction = mbta.getTrainDirection(t);
    
    int nextPos = currentPos + direction;
    
    if (nextPos >= line.size()) {
      direction = -1;
      nextPos = currentPos - 1;

    } else if (nextPos < 0) {
      direction = 1;
      nextPos = currentPos + 1;

    }
    
    Station expectedNext = line.get(nextPos);
    if (!s2.equals(expectedNext)) {
      throw new RuntimeException();
    }

    if (mbta.isStationOccupied(s2, t)) {
      throw new RuntimeException();
    }

    mbta.moveTrain(t, s1, s2);
  }
}
