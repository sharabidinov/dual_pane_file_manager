import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Model {

  public List<File> getDirectoryContents(String directoryPath) {
    File directory = new File(directoryPath);
    if (!directory.exists() || !directory.isDirectory()) {
      return List.of();
    }

    File[] files = directory.listFiles();
    if (files == null) {
      return List.of();
    }

    return Arrays.stream(files)
        .sorted(Comparator.comparing(File::isDirectory).reversed()
            .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER))
        .collect(Collectors.toList());
  }

  public String getParentDirectory(String directoryPath) {
    File directory = new File(directoryPath);
    File parent = directory.getParentFile();
    return parent != null ? parent.getAbsolutePath() : directoryPath;
  }

  public boolean copyFile(String sourcePath, String destinationPath) {
    try {
      Path source = Paths.get(sourcePath);
      Path destination = Paths.get(destinationPath);

      File destFile = destination.toFile();
      if (destFile.isDirectory()) {
        destination = destination.resolve(source.getFileName());
      }

      if (Files.isDirectory(source)) {
        return copyDirectory(source.toFile(), destination.toFile());
      } else {
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private boolean copyDirectory(File sourceDir, File destDir) {
    try {
      if (!destDir.exists()) {
        destDir.mkdir();
      }

      File[] files = sourceDir.listFiles();
      if (files == null) {
        return true;
      }

      for (File file : files) {
        File destFile = new File(destDir, file.getName());
        if (file.isDirectory()) {
          copyDirectory(file, destFile);
        } else {
          Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean moveFile(String sourcePath, String destinationPath) {
    try {
      Path source = Paths.get(sourcePath);
      Path destination = Paths.get(destinationPath);

      File destFile = destination.toFile();
      if (destFile.isDirectory()) {
        destination = destination.resolve(source.getFileName());
      }

      Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean deleteFile(String path) {
    try {
      File file = new File(path);
      if (file.isDirectory()) {
        return deleteDirectory(file);
      } else {
        return file.delete();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private boolean deleteDirectory(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          file.delete();
        }
      }
    }
    return directory.delete();
  }

  public boolean createDirectory(String parentPath, String directoryName) {
    try {
      File newDir = new File(parentPath, directoryName);
      return newDir.mkdir();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean renameFile(String path, String newName) {
    try {
      File file = new File(path);
      File parent = file.getParentFile();
      File newFile = new File(parent, newName);
      return file.renameTo(newFile);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
