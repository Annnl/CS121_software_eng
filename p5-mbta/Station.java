import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class Station extends Entity {
  private Station(String name) { super(name); }

  private static final Map<String, Station> cache = new ConcurrentHashMap<>();
  public static synchronized Station make(String name) {
    // Change this method!
    if (!cache.containsKey(name)) {
      cache.put(name, new Station(name));
    }
    return cache.get(name);
  }
}
