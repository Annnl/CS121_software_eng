import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class Passenger extends Entity {
  private Passenger(String name) { super(name); }

  private static final Map<String, Passenger> cache = new ConcurrentHashMap<>();
  public static synchronized Passenger make(String name) {
    // Change this method!
    if (!cache.containsKey(name)) {
      cache.put(name, new Passenger(name));
    }
    return cache.get(name);
  }
}
