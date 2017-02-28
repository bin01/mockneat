package com.mockneat.mock.unit.objects;

import com.mockneat.mock.interfaces.MockUnit;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.mockneat.mock.utils.ValidationUtils.*;
import static java.lang.String.format;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;

public class Objs<T> implements MockUnit<T> {

    private Map<String, MockValue> fields = new LinkedHashMap<>();
    private Class<T> cls;

    private Objs() {}

    public Objs(Class<T> cls) {
        this.cls = cls;
    }

    public Objs type(Class<T> cls) {
        this.cls = cls;
        return this;
    }

    public <T1> Objs<T> field(String fieldName, MockUnit<T1> mockUnit) {
        this.fields.put(fieldName, new MockRandUnitValue(mockUnit));
        return this;
    }

    public <T1> Objs<T> field(String fieldName, MockUnit<T1> mockUnit, boolean forced) {
        this.fields.put(fieldName, new MockRandUnitValue(mockUnit, forced));
        return this;
    }

    public Objs<T> field(String fieldName, Object value) {
        this.fields.put(fieldName, new MockConstValue(value));
        return this;
    }

    public Objs<T> field(String fieldName, Object value, boolean forced) {
        this.fields.put(fieldName, new MockConstValue(value, forced));
        return this;
    }

    protected T instance() {
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            String fmt = format(CANNOT_INSTANTIATE_OBJECT_OF_CLASS,
                    cls.getSimpleName(),
                    cls.getSimpleName());
            throw new IllegalArgumentException(fmt, e);
        }
    }

    protected void setValues(T object) {
        fields.forEach((key, val) -> {
            Object cVal = val.get();
            try {
                writeField(object, key, cVal, val.isForced());
            } catch (IllegalAccessException e) {
                String fmt = format(CANNOT_SET_FIELD_WITH_VALUE,
                        cls.getSimpleName(),
                        key,
                        cVal);
               throw new IllegalArgumentException(fmt, e);
            }
        });
    }

    protected String listTypes(Object[] objs) {
        final StringBuilder buff = new StringBuilder("(");
        Arrays.stream(objs).forEach(obj -> {
            if (null != obj) { buff.append(obj.getClass().getName()); }
            else { buff.append("null"); }
            buff.append(",");
        });
        buff.deleteCharAt(buff.length()-1);
        buff.append(")");
        return buff.toString();
    }

    public MockUnit<T> constructor(MockUnit... mockUnits) {
        Supplier<T> supp = () -> {
            Object[] args = new Object[mockUnits.length];
            range(0, mockUnits.length).forEach(i -> {
                if (mockUnits[i]==null) { args[i] = null; }
                else { args[i] = mockUnits[i].val(); }
            });
            try {
                return invokeConstructor(cls, args);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                String fmt = format(CANNOT_INFER_CONSTRUCTOR, cls.getName(), listTypes(args));
                throw new IllegalArgumentException(fmt, e);
            }
        };
        return () -> supp;
    }


    @Override
    public Supplier<T> supplier() {
        return () -> {
            T instance = instance();
            setValues(instance);
            return instance;
        };
    }
}