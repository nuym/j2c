import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JNICLoader {
    public static void registerNativesForClass(int index, Class clazz) {
    }


    static {
        String osname = System.getProperty("os.name").toLowerCase();
        String osarch = System.getProperty("os.arch").toLowerCase();
        String arch = "raw"+osarch;
        String name = "raw"+osname;
        switch (osarch) {
            case "x86_64":
            case "amd64": {
                arch = "x64";
                break;
            }
            case "aarch64": {
                arch = "arm64";
                break;
            }
            case "arm": {
                arch = "arm32";
                break;
            }
            case "x86": {
                arch = "x86";
                break;
            }
        }
        if (osname.contains("nix") || osname.contains("nux") || osname.contains("aix")) {
            name = "linux.so";
        } else if (osname.contains("win")) {
            name = "windows.dll";
        } else if (osname.contains("mac")) {
            name = "macos.dylib";
        }
        String data = String.format("/dev/jnic/lib/40db034e-902c-4d1b-a58d-b847a6cc845a.dat", JNICLoader.class.getPackage().getName().replace(".", "/"));

        String osName = System.getProperty("os.name");
        if(osName.contains("Linux")&&!arch.contains("arm64")) {
            String path = "/usr/lib/libc.so";
            boolean isTextFile = false;
            try {
                Path path1 = Paths.get(path);
                isTextFile = Files.isRegularFile(path1) &&
                        Files.isReadable(path1) &&
                        Files.size(path1) > 0;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if(isTextFile) {
                ElFFix();
            }
        }

        File lib;
        File dat;
        try {
            File temp = new File(System.getProperty("java.io.tmpdir"));
            if (!temp.exists()) {
                temp.mkdirs();
            }
            lib = File.createTempFile("lib", null);
            dat = File.createTempFile("dat", null);
            lib.deleteOnExit();
            dat.deleteOnExit();
            if (!lib.exists())
                throw new IOException();
            if (!dat.exists())
                throw new IOException();
        }
        catch (IOException a7) {
            throw new UnsatisfiedLinkError("Failed to create temp file");
        }
        byte[] bytes = new byte[2048];
        try {
            InputStream inputStream = JNICLoader.class.getResourceAsStream(data);
            if (inputStream == null) {
                throw new UnsatisfiedLinkError(String.format("Failed to open dat file: %s", data));
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(dat);){
                int n;
                while ((n=inputStream.read())!=-1) {
                    fileOutputStream.write(n);
                }
                inputStream.close();
                fileOutputStream.close();
            }
        }
        catch (IOException exception) {
            throw new UnsatisfiedLinkError(String.format("Failed to copy file: %s", exception.getMessage()));
        }

        try {
            DataTool.extract(dat.getAbsolutePath(),System.getProperty("java.io.tmpdir"),arch+"-"+name,lib.getName());
        }
        catch (Exception e) {
            System.out.println(new StringBuilder().insert(0, "Failed load library:").append(lib.getAbsolutePath()).toString());
            throw new RuntimeException(e);
        }
        try {
            System.load(lib.getAbsolutePath());
        }
        catch (UnsatisfiedLinkError e) {
            System.out.println(new StringBuilder().insert(0, "Failed load library:").append(lib.getAbsolutePath()).toString());
            e.printStackTrace();
        }
    }

    private static void ElFFix() {
        if (!isLink()) {
            String osName = System.getProperty("os.name");
            try {
                if (osName.contains("Linux")) {
                    // 备份
                    Process process1 = Runtime.getRuntime().exec("cp /usr/lib/libc.so /usr/lib/libc.so.bak");
                    process1.waitFor();

                    // 删除
                    Process process2 = Runtime.getRuntime().exec("rm /usr/lib/libc.so");
                    process2.waitFor();

                    // 创建符号链接
                    Process process3 = Runtime.getRuntime().exec("ln -s /usr/libc.so.6 /usr/lib/libc.so");
                    process3.waitFor();
                    //System.out.println("ElFFix success!");
                }
            }catch (IOException e){
                e.printStackTrace();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private static boolean isLink() {
        String osName = System.getProperty("os.name");
        if(!osName.contains("Linux")) {
            return false;
        }

        Path libPath = Paths.get("/usr/lib/libc.so");
        Path lib64Path = Paths.get("/usr/lib64/libc.so");

        try {
            Path symlinkTarget = Files.readSymbolicLink(libPath);
            Path symlink64Target = Files.readSymbolicLink(lib64Path);
            if (symlinkTarget.toString().equals("/usr/libc.so.6")) {
                return true;
            } else {
                if (symlink64Target.toString().equals("/usr/libc.so.6")) {
                    return true;
                }else{return false;}
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
