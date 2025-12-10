import java.util.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
public class Unit {
    public static Map<String, Throwable> testClass(String name) {
        Map<String, Throwable> result = new LinkedHashMap<>();
        try {
            Class<?> c = Class.forName(name);
            Method[] methods = c.getMethods();
            List<Method> beforeC = new ArrayList<>();
            List<Method> before = new ArrayList<>();
            List<Method> afterC = new ArrayList<>();
            List<Method> after = new ArrayList<>();
            List<Method> test = new ArrayList<>();
            for (Method method : methods){
                int count = 0;
                if (method.isAnnotationPresent(BeforeClass.class)){
                    count++;
                    if (!Modifier.isStatic(method.getModifiers())){
                        throw new RuntimeException();
                    }
                    beforeC.add(method);
                }
                if (method.isAnnotationPresent(Before.class)){
                    count++;
                    before.add(method);
                }
                if (method.isAnnotationPresent(AfterClass.class)){
                    count++;
                    if (!Modifier.isStatic(method.getModifiers())){
                        throw new RuntimeException();
                    }
                    afterC.add(method);
                }
                if (method.isAnnotationPresent(After.class)){
                    count++;
                    after.add(method);
                }
                if (method.isAnnotationPresent(Test.class)){
                    count++;
                    test.add(method);
                }
                if (count > 1){
                    throw new RuntimeException();
                }

            }
            Comparator<Method> alphaNameComparator = Comparator.comparing(Method::getName);
            beforeC.sort(alphaNameComparator);
            before.sort(alphaNameComparator);
            afterC.sort(alphaNameComparator);
            after.sort(alphaNameComparator);
            test.sort(alphaNameComparator);

            for (Method method : beforeC){
                try{
                    method.invoke(null);
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }

            for (Method method : test){
                Throwable error = null;
                Object instance = c.getConstructor().newInstance();
                try {
                    for (Method m : before){
                        try {
                            m.invoke(instance);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    try {
                        method.invoke(instance);
                    } catch (InvocationTargetException e) {
                        error = e.getCause();
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    for (Method m : after){
                        try {
                            m.invoke(instance);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                result.put(method.getName(), error);
            }

            for (Method method : afterC){
                try{
                    method.invoke(null);
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        
	    return result;
    }

    public static Map<String, Object[]> quickCheckClass(String name) {
	    Map<String, Object[]> result = new LinkedHashMap<>();
        try {
            Class<?> c = Class.forName(name);
            Method[] methods = c.getMethods();
            List<Method> propertyMethods = new ArrayList<>();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Property.class)) {
                    propertyMethods.add(method);
                }
            }
            
            propertyMethods.sort(Comparator.comparing(Method::getName));
            
            for (Method propertyMethod : propertyMethods) {
                Object testInstance = c.getConstructor().newInstance();
                Object[] failingArgs = runProperty(propertyMethod, testInstance);
                result.put(propertyMethod.getName(), failingArgs);
            }
            
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return result;
    }

    private static Object[] runProperty(Method method, Object instance) {
        Parameter[] params = method.getParameters();
        
        List<Object[]> argCombinations = generateArgumentCombinations(params, instance);

        int maxRuns = Math.min(argCombinations.size(), 100);
        for (int i = 0; i < maxRuns; i++) {
            Object[] args = argCombinations.get(i);
            try {
                Object result = method.invoke(instance, args);
                if (result instanceof Boolean && !(Boolean) result) {
                    return args;
                }
            } catch (InvocationTargetException e) {
                return args;
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }

        return null;
    }
    
    private static List<Object[]> generateArgumentCombinations(Parameter[] params, Object instance) {
        if (params.length == 0) {
            List<Object[]> result = new ArrayList<>();
            result.add(new Object[0]);
            return result;
        }
        
        if (params[0].isAnnotationPresent(ForAll.class)) {
            return generateForAllArguments(params[0], instance);
        }

        List<List<Object>> possibleValues = new ArrayList<>();
        for (Parameter param : params) {
            possibleValues.add(generateValuesForParameter(param, instance));
        }

        return cartesianProduct(possibleValues);
    }
    
    private static List<Object[]> generateForAllArguments(Parameter param, Object instance) {
        ForAll forAll = param.getAnnotation(ForAll.class);
        String methodName = forAll.name();
        int times = forAll.times();
        
        List<Object[]> result = new ArrayList<>();
        try {
            Method generatorMethod = instance.getClass().getMethod(methodName);
            for (int i = 0; i < times; i++) {
                Object value = generatorMethod.invoke(instance);
                result.add(new Object[]{value});
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        
        return result;
    }
    
    private static List<Object> generateValuesForParameter(Parameter param, Object instance) {
        List<Object> values = new ArrayList<>();
        
        Annotation[] annotations = param.getAnnotations();
        if (annotations.length != 1) {
            throw new RuntimeException();
        }
        
        if (param.isAnnotationPresent(IntRange.class)) {
            IntRange range = param.getAnnotation(IntRange.class);
            for (int i = range.min(); i <= range.max(); i++) {
                values.add(i);
            }
        } else if (param.isAnnotationPresent(StringSet.class)) {
            StringSet stringSet = param.getAnnotation(StringSet.class);
            for (String s : stringSet.strings()) {
                values.add(s);
            }
        } else if (param.getParameterizedType() instanceof ParameterizedType) {
            values.addAll(generateListValues(param));
        } else {
            throw new RuntimeException();
        }
        
        return values;
    }
    
    private static List<Object> generateListValues(Parameter param) {
        List<Object> values = new ArrayList<>();

        AnnotatedType annotatedType = param.getAnnotatedType();
        if (!(annotatedType instanceof AnnotatedParameterizedType)) {
            throw new RuntimeException();
        }

        ListLength listLength = param.getAnnotation(ListLength.class);
        if (listLength == null) {
            throw new RuntimeException();
        }

        AnnotatedParameterizedType apt = (AnnotatedParameterizedType) annotatedType;
        AnnotatedType[] annotatedTypeArgs = apt.getAnnotatedActualTypeArguments();
        if (annotatedTypeArgs.length != 1) {
            throw new RuntimeException();
        }

        List<Object> elementValues = generateElementValuesFromAnnotatedType(annotatedTypeArgs[0]);
        for (int length = listLength.min(); length <= listLength.max(); length++) {
            generateListsRecursive(values, elementValues, new ArrayList<>(), length);
        }

        return values;
    }

    private static List<Object> generateElementValuesFromAnnotatedType(AnnotatedType annotatedType) {
        List<Object> values = new ArrayList<>();
        Annotation[] annotations = annotatedType.getAnnotations();

        for (Annotation ann : annotations) {
            if (ann instanceof IntRange) {
                IntRange range = (IntRange) ann;
                for (int i = range.min(); i <= range.max(); i++) {
                    values.add(i);
                }
                return values;
            } else if (ann instanceof StringSet) {
                StringSet ss = (StringSet) ann;
                values.addAll(Arrays.asList(ss.strings()));
                return values;
            }
        }

        if (annotatedType instanceof AnnotatedParameterizedType) {
            AnnotatedParameterizedType apt = (AnnotatedParameterizedType) annotatedType;
            AnnotatedType[] typeArgs = apt.getAnnotatedActualTypeArguments();
            if (typeArgs.length != 1) {
                throw new RuntimeException();
            }

            ListLength lenAnn = annotatedType.getAnnotation(ListLength.class);
            if (lenAnn == null) {
                throw new RuntimeException();
            }

            List<Object> innerElementValues = generateElementValuesFromAnnotatedType(typeArgs[0]);

            List<Object> result = new ArrayList<>();
            for (int length = lenAnn.min(); length <= lenAnn.max(); length++) {
                generateListsRecursive(result, innerElementValues, new ArrayList<>(), length);
            }

            return result;
        }

        throw new RuntimeException();
    }
    
    private static void generateListsRecursive(List<Object> result, List<Object> elements, 
                                               List<Object> current, int remaining) {
        if (remaining == 0) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (Object element : elements) {
            current.add(element);
            generateListsRecursive(result, elements, current, remaining - 1);
            current.remove(current.size() - 1);
        }
    }
    
    private static List<Object[]> cartesianProduct(List<List<Object>> lists) {
        List<Object[]> result = new ArrayList<>();
        cartesianProductHelper(lists, 0, new Object[lists.size()], result);
        return result;
    }
    
    private static void cartesianProductHelper(List<List<Object>> lists, int index, 
                                              Object[] current, List<Object[]> result) {
        if (index == lists.size()) {
            result.add(current.clone());
            return;
        }
        
        for (Object value : lists.get(index)) {
            current[index] = value;
            cartesianProductHelper(lists, index + 1, current, result);
        }
    }
}