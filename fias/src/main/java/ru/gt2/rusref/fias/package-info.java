/**
 * FIXME Как-то описать комментарии в аннотациях.
 *
 * FIXME Писать в CSV: Оригинальные ограничения, описания полей.
 *
 * FIXME Пройтись по всем классам и сделать имена, соответствующие общим (например везде использовать name и shortName).
 *
 * FIXME Добавление справочных значений, вместо идентификаторов.
 * С одной стороны, справочники можно проверить введя например класс References с методами для получения
 * объектов для каждого значения, но с другой стороны, для связи больших данных этот способ уже не подойдёт.
 *
 * FIXME Проверка конвертации путём обратного преобразования в xml-строку.
 *
 * FIXME Паралелизация обработки (JAXBContext — он ThreadSafe, а Marshaller — нет).
 * http://stackoverflow.com/questions/1134189/can-jaxb-parse-large-xml-files-in-chunks
 *
 * FIXME Спросить про метаданные для JAXB, аналогично JPA2.
 *
 * FIXME Отдельная страница по стилям, например порядок аннотация:
 * FiasRef, JPA (то как будет в базе), BeanValidation (ограничения на поле), JAXB (то что было исходно), Lombok
 */
package ru.gt2.rusref.fias;
