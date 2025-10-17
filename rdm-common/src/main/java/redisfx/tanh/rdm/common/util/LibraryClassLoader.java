package redisfx.tanh.rdm.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class LibraryClassLoader extends ClassLoader {
    private static final Logger log = LoggerFactory.getLogger(LibraryClassLoader.class);
    private  File libDirectory;
    private final List<URLClassLoader> jarLoaders = new ArrayList<>();

    public LibraryClassLoader(ClassLoader parent) {
        super(parent);
        this.libDirectory = new File("./libs");
        initializeJarLoaders();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            // 先尝试父类加载器
            return super.loadClass(name, resolve);
        } catch (Throwable e) {
            // 父类加载器无法加载时，使用自定义加载逻辑
            Class<?> clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 在 lib/* 目录中查找并加载类
        for (URLClassLoader jarLoader : jarLoaders) {
            try {
                return jarLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                // 继续尝试下一个 JAR
            }
        }
        throw new ClassNotFoundException(name);
    }

    public void initializeJarLoaders() {
        jarLoaders.clear();
        if (libDirectory.exists() && libDirectory.isDirectory()) {
            File[] jarFiles = libDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                for (File jarFile : jarFiles) {
                    try {
                        URLClassLoader jarLoader = new URLClassLoader(
                            new URL[]{jarFile.toURI().toURL()},
                            this.getParent()
                        );
                        jarLoaders.add(jarLoader);
                    } catch (Exception e) {
                        log.error("Failed to load JAR: {}",  jarFile.getName(),e);
                    }
                }
            }
            log.info("Loaded {} JARs from {}", jarLoaders.size(), libDirectory.getAbsolutePath());
        }
    }
}
