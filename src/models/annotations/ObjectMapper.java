package models.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class ObjectMapper<T> {
   private Class clazz;
   private HashMap<String, Field> fields;

   public ObjectMapper(Class clazz) {
      this.clazz = clazz;
      this.fields = new HashMap<>();

      List<Field> fieldList = Arrays.asList(clazz.getDeclaredFields());

      for (Field field : fieldList) {
         Column column = field.getAnnotation(Column.class);
         if (column != null) {
            field.setAccessible(true);
            this.fields.put(column.value(), field);
         }
      }
   }

   public T map(Map<String, Object> row) {
      try {

         T mappedObject = (T) clazz.getConstructor().newInstance();

         for (Map.Entry<String, Object> e : row.entrySet()) {
            if (e == null) {
               continue;
            }

            String column = e.getKey();
            Field field = fields.get(column);
            if (field != null) {
               if (clazz.getSimpleName().equals("Message") && field.getName().equals("TYPE")) {
                  Method method = clazz.getMethod("setTYPE", String.class);
                  method.invoke(mappedObject, e.getValue());
               } else {
                  Object value = e.getValue();
                  field.set(mappedObject, value);
               }
            }
         }

         return mappedObject;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public List<T> map(List<Map<String, Object>> rows) {
      List<T> mappedObjects = new ArrayList<>();
      rows.forEach(r -> {
         T object = this.map(r);
         if (object != null) {
            mappedObjects.add(object);
         }
      });
      return mappedObjects;
   }

   public List<T> map(ResultSet results) throws SQLException {

      List<Map<String, Object>> rows = new ArrayList<>();

      ResultSetMetaData metadata = results.getMetaData();

      int count = metadata.getColumnCount();

      while (results.next()) {
         Map<String, Object> row = new HashMap<>();
         for (int i = 1; i <= count; i++) {
            String column = metadata.getColumnName(i);
            Object value = results.getObject(i);
            row.put(column, value);
         }
         rows.add(row);
      }
      return map(rows);
   }

}
