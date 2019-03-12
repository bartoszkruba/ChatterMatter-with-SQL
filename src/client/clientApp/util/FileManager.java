package client.clientApp.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {

   private FileManager() {
   }

   public static boolean saveFile(Serializable object, Path path) {
      boolean success = false;

      try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path))) {
         success = writeToStream(object, output);
      } catch (IOException e) {
         e.printStackTrace();
      }
      return success;
   }

   public static boolean saveFile(Serializable object, String filename) {
      boolean success = false;

      Path path = Paths.get(filename);

      try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path))) {
         success = writeToStream(object, output);
      } catch (IOException e) {
         e.printStackTrace();
      }
      return success;
   }

   public static Serializable loadFile(Path path) {
      try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
         return readFromStream(input);
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   public static Serializable loadFile(String filename) {
      Path path = Paths.get(filename);

      try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
         return readFromStream(input);
      } catch (IOException e) {
         return null;
      }
   }

   public static boolean writeToStream(Serializable object, ObjectOutputStream output) {
      boolean succes = false;
      try {
         output.reset();
         output.writeObject(object);
         succes = true;
      } catch (IOException e) {
         e.printStackTrace();
      }
      return succes;
   }

   public static Serializable readFromStream(ObjectInputStream input) {
      Serializable object = null;

      try {
         object = (Serializable) input.readObject();
      } catch (IOException | ClassNotFoundException e) {
         e.printStackTrace();
      }
      return object;
   }
}
