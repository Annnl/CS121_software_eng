import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Train extends Entity {
  private Train(String name) { super(name); }
  private static final Map<String, Train> cache = new ConcurrentHashMap<>();
  public static synchronized Train make(String name) {
    // Change this method!
    if (!cache.containsKey(name)) {
      cache.put(name, new Train(name));
    }
    return cache.get(name);
  }
}
