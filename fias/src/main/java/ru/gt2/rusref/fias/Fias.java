package ru.gt2.rusref.fias;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Все файлы ФИАС.
 */
public enum Fias {
    ADROBJPK(AddressObjectGuid.class),
    ADDROBJ(AddressObjects.class, AddressObject.class, "01"),
    HOUSE(Houses.class, House.class, "02"),
    HOUSEINT(HouseIntervals.class, HouseInterval.class, "03"),
    LANDMARK(Landmarks.class, Landmark.class, "04"),
    NRMDOCTP(NormativeDocumentType.class),
    NORMDOC(NormativeDocumentes.class, NormativeDocument.class, "05"),
    ADROBJLV(AddressObjectLevel.class),
    SOCRBASE(AddressObjectTypes.class, AddressObjectType.class, "06"),
    CURENTST(CurrentStatuses.class, CurrentStatus.class, "07"),
    ACTSTAT(ActualStatuses.class, ActualStatus.class, "08"),
    OPERSTAT(OperationStatuses.class, OperationStatus.class, "09"),
    CENTERST(CenterStatuses.class, CenterStatus.class, "10"),
    INTVSTAT(IntervalStatuses.class, IntervalStatus.class, "11"),
    HSTSTAT(HouseStateStatuses.class, HouseStateStatus.class, "12"),
    ESTSTAT(EstateStatuses.class, EstateStatus.class, "13"),
    STRSTAT(StructureStatuses.class, StructureStatus.class, "14");

    /** Вспомогательный, не содержит данных */
    public final boolean intermediate;
    /** Класс обёртки. */
    public final Class<?> wrapper;
    /** Внутренний класс — сам справочник. */
    public final Class<?> item;
    /** Поля справочника. */
    public final ImmutableList<Field> itemFields;
    /** Поля справочника с первичным ключём в первом поля — для загрузки данных с GUID'ами. */
    public final ImmutableList<Field> reorderedItemFields;
    /** Ключевое поле. */
    public final Field idField;
    /** Название файла со схемой. */
    public final String schemePrefix;

    public static final ImmutableMap<Class<?>, Fias> FROM_ITEM_CLASS;

    private static final Predicate<Field> FIAS_REF;

    public static final Function<AnnotatedElement, Fias> FIAS_REF_TARGET;
    public static final Predicate<Fias> FIAS_NOT_ITERMEDIATE;

    static {
        FIAS_REF = new Predicate<Field>() {
            @Override
            public boolean apply(@Nullable Field field) {
                return (null != field.getAnnotation(FiasRef.class));
            }
        };

        FIAS_REF_TARGET = new Function<AnnotatedElement, Fias>() {
            @Override
            public Fias apply(@Nullable AnnotatedElement annotatedElement) {
                Preconditions.checkNotNull(annotatedElement);
                FiasRef ref = annotatedElement.getAnnotation(FiasRef.class);
                Preconditions.checkNotNull(ref);
                Class<?> targetClass = ref.value();
                Fias target = Fias.FROM_ITEM_CLASS.get(targetClass);
                Preconditions.checkNotNull(target);
                return target;
            }
        };

        FIAS_NOT_ITERMEDIATE = new Predicate<Fias>() {
            @Override
            public boolean apply(@Nullable Fias fias) {
                Preconditions.checkNotNull(fias);
                return !fias.intermediate;
            }
        };

        Map<Class<?>, Fias> fromType = Maps.newHashMap();
        for (Fias fias : values()) {
            fromType.put(fias.item, fias);
        }
        FROM_ITEM_CLASS = ImmutableMap.copyOf(fromType);
    }

    public static Iterable<Field> getReferences(Fias fias) {
        return Iterables.filter(fias.itemFields, FIAS_REF);
    }

    public static ImmutableList<Field> getSelfReferenceFields(Fias fias) {
        List<Field> result = Lists.newArrayList();
        for (Field field : fias.itemFields) {
            FiasRef ref = field.getAnnotation(FiasRef.class);
            if (null == ref) {
                continue;
            }
            if (!ref.value().equals(fias.item)) {
                continue;
            }
            result.add(field);
        }
        return ImmutableList.copyOf(result);
    }

    public static Set<Fias> orderForCreation() {
        return orderFor(false);
    }

    public static Set<Fias> orderForLoading() {
        return orderFor(true);
    }

    public Object[] getFieldValuesReordered(Object entity) throws IllegalAccessException {
        int index = 0;
        Object[] result = new Object[reorderedItemFields.size()];
        for (Field field : reorderedItemFields) {
            result[index] = field.get(entity);
            index++;
        }
        return result;
    }

    public ImmutableSet<Object> getNotNullFieldValues(final Object entity, Iterable<Field> fields) {
        Iterable<Object> values = Iterables.transform(fields, new Function<Field, Object>() {
            @Override
            @SneakyThrows
            public Object apply(@Nullable Field field) {
                Preconditions.checkNotNull(field);
                return field.get(entity);
            }
        });

        Iterable<Object> notNullValues = Iterables.filter(values, Predicates.notNull());
        return ImmutableSet.copyOf(notNullValues);
    }

    private Fias(Class<?> wrapper, Class<?> item, String schemePart) {
        this.wrapper = wrapper;
        intermediate = null == wrapper;
        this.item = item;
        if (!Strings.isNullOrEmpty(schemePart)) {
            schemePrefix = "AS_" + name() + "_2_250_" + schemePart + "_04_01_";
        } else {
            schemePrefix = null;
        }
        this.itemFields = getAllFields(item);
        this.reorderedItemFields = getAllFieldsReordered(itemFields);
        this.idField = getId(itemFields);
    }

    private Fias(Class<?> item) {
        this(null, item, null);
    }

    private static ImmutableList<Field> getAllFields(Class<?> item) {
        String[] propOrder = getPropOrder(item);

        Map<String, Field> fieldByName = getAllFieldsMap(item);

        if (0 == propOrder.length) {
            return ImmutableList.copyOf(fieldByName.values());
        }

        // Упорядочить поля по XmlType.propOrder, если есть.
        List<Field> fields = Lists.newArrayListWithCapacity(propOrder.length);
        for (String name : propOrder) {
            Field field = fieldByName.get(name);
            Preconditions.checkNotNull(field, "Field: {0} not found, class {1}", name, item);
            fields.add(field);
        }
        return ImmutableList.copyOf(fields);
    }

    private static ImmutableList<Field> getAllFieldsReordered(ImmutableList<Field> fields) {
        List<Field> result = Lists.newArrayListWithCapacity(fields.size());
        for (Field field : fields) {
            Id id = field.getAnnotation(Id.class);
            if (null != id) {
                result.add(0, field);
            } else {
                result.add(field);
            }
        }
        return ImmutableList.copyOf(result);
    }

    private static Map<String, Field> getAllFieldsMap(Class<?> item) {
        Map<String, Field> result = Maps.newHashMap();
        while (!Object.class.equals(item)) {
            for (Field field : item.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Field existing = result.put(field.getName(), field);
                if (null != existing) {
                    throw new IllegalArgumentException("Duplicate field, trying to add: " + field
                            + ", while " + existing + " is already exists");
                }
            }
            item = item.getSuperclass();
        }
        return result;
    }

    private static String[] getPropOrder(Class<?> item) {
        XmlType xmlType = item.getAnnotation(XmlType.class);
        String[] propOrder = null;
        if (null != xmlType) {
            propOrder = xmlType.propOrder();
        }
        return Objects.firstNonNull(propOrder, new String[0]);
    }

    private static Field getId(Collection<Field> fields) {
        Field result = null;
        for (Field field : fields) {
            Id id = field.getAnnotation(Id.class);
            if (null == id) {
                continue;
            }
            if (null != result) {
                throw new IllegalArgumentException("Duplication of id " + field);
            }
            result = field;
        }
        if (null == result) {
            throw new IllegalArgumentException("Id field is not defined!");
        }
        return result;
    }

    private static Set<Fias> orderFor(boolean loading) {
        // Нужно получить упорядоченный по использованию список
        // Чтобы сначала были все зависимости, а потом уже таблица, которая зависит.
        // Способ такой идём по списку необработанных, проверяем если все зависимости в обработанных
        // если есть (или зависимостей вообще нет) — перемещаем в список обработанных. Иначе — переходим к следующему.
        // Во внешнем цикле проверяем наличие элементов в списке необработанных.
        // Для того чтобы не скатиться в бесконечный цикл — проверяем наличие перемещения, если прошли по циклу
        // перемещения — то получаем циклическую зависимость.
        List<Fias> notProcessed = Lists.newArrayList(values());
        Set<Fias> result = Sets.newLinkedHashSet();

        while (!notProcessed.isEmpty()) {
            boolean moveHappens = false;
            for (Iterator<Fias> iterator = notProcessed.iterator(); iterator.hasNext(); ) {
                Fias fias = iterator.next();
                Iterable<Fias> referenceTargets = Iterables.transform(getReferences(fias), FIAS_REF_TARGET);
                if (loading) {
                    if (fias.intermediate) {
                        Fias refTarget = FIAS_REF_TARGET.apply(fias.item);
                        referenceTargets = Iterables.concat(referenceTargets, Collections.singleton(refTarget));
                    } else {
                        referenceTargets = Iterables.filter(referenceTargets, FIAS_NOT_ITERMEDIATE);
                    }
                }

                ImmutableSet<Fias> references = ImmutableSet.copyOf(referenceTargets);

                Sets.SetView<Fias> notProcessedReferences =
                        Sets.difference(references,
                                Sets.union(result, Collections.singleton(fias)));
                moveHappens = notProcessedReferences.isEmpty();
                if (moveHappens) {
                    iterator.remove();
                    result.add(fias);
                    break;
                }
            }
            if (!moveHappens) {
                throw new RuntimeException("Cycle dependencies, not processed: " + notProcessed.toString());
            }
        }
        return result;
    }

}
