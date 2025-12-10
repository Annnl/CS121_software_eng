package jrails;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JRouter {
    private static class Route {
        String verb;
        String path;
        Class<?> clazz;
        String methodName;
        
        Route(String verb, String path, Class<?> clazz, String methodName) {
            this.verb = verb;
            this.path = path;
            this.clazz = clazz;
            this.methodName = methodName;
        }
        
        boolean matches(String verb, String path) {
            return this.verb.equals(verb) && this.path.equals(path);
        }
    }
    
    // List to store all registered routes
    private List<Route> routes;
    
    // Valid HTTP verbs
    private static final Set<String> VALID_VERBS = new HashSet<>(
        Arrays.asList("GET", "POST", "PUT", "DELETE")
    );
    
    public JRouter() {
        this.routes = new ArrayList<>();
    }
    public void addRoute(String verb, String path, Class<?> clazz, String method) {
        // Implement me!
        if (!VALID_VERBS.contains(verb)) {
            throw new RuntimeException();
        }
        
        // Validate that clazz exists and is a subclass of Controller
        if (clazz == null) {
            throw new RuntimeException();
        }
        
        if (!Controller.class.isAssignableFrom(clazz)) {
            throw new RuntimeException();
        }
        
        // Validate that the method exists and has the correct signature
        Method targetMethod = null;
        try {
            // Controller methods should take Map<String, String> as parameter
            targetMethod = clazz.getMethod(method, Map.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException();
        }
        
        // Validate return type is Html
        if (!Html.class.isAssignableFrom(targetMethod.getReturnType())) {
            throw new RuntimeException();
        }
        
        // Add the route
        routes.add(new Route(verb, path, clazz, method));
    }

    // Returns "clazz#method" corresponding to verb+URN
    // Null if no such route
    public String getRoute(String verb, String path) {
        for (Route route : routes) {
            if (route.matches(verb, path)) {
                return route.clazz.getSimpleName() + "#" + route.methodName;
            }
        }
        return null;
    }

    // Call the appropriate controller method and
    // return the result
    public Html route(String verb, String path, Map<String, String> params) {
        Route matchedRoute = null;
        for (Route route : routes) {
            if (route.matches(verb, path)) {
                matchedRoute = route;
                break;
            }
        }
        
        if (matchedRoute == null) {
            throw new UnsupportedOperationException();
        }
        
        try {
            // Create an instance of the controller
            Object controllerInstance = matchedRoute.clazz.getDeclaredConstructor().newInstance();
            
            // Get the method
            Method method = matchedRoute.clazz.getMethod(matchedRoute.methodName, Map.class);
            
            // Invoke the method with params
            Object result = method.invoke(controllerInstance, params);
            
            // Return the Html result
            return (Html) result;
            
        } catch (Exception e) {
            // Wrap any reflection exceptions
            throw new RuntimeException();
        }
    }
}
