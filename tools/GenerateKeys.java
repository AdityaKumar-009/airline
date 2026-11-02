import java.nio.file.*;
import java.security.*;
import java.util.Base64;

/** Run once from the repository root: java tools/GenerateKeys.java */
public class GenerateKeys {
  public static void main(String[] args) throws Exception {
    Path directory = Path.of("keys");
    Files.createDirectories(directory);
    if (Files.exists(directory.resolve("private.pem"))) {
      System.out.println("Keys already exist; leaving them unchanged."); return;
    }
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair pair = generator.generateKeyPair();
    Files.writeString(directory.resolve("private.pem"), pem("PRIVATE KEY", pair.getPrivate().getEncoded()));
    Files.writeString(directory.resolve("public.pem"), pem("PUBLIC KEY", pair.getPublic().getEncoded()));
    System.out.println("Created local demo RSA keys in ./keys (not for production). ");
  }
  private static String pem(String label, byte[] bytes) {
    return "-----BEGIN " + label + "-----\n" + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(bytes)
        + "\n-----END " + label + "-----\n";
  }
}
