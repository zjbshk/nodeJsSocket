package top.jxnu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class Main {


    private static int port = 8024;
    private static ServerSocket serverSocket;

    public static void main(String args[]) {

        initPropertis();
        launchServer();
        runServer();
    }

    private static void launchServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器启动成功");
        } catch (IOException e) {
            System.out.println("启动服务器Socket失败");
        }
    }

    private static void runServer() {
        while (true) {
            try {
                System.out.println("服务器进入等待访问状态");
                Socket socket = serverSocket.accept();
                DealTask dealTask = DealTask.dealSocket(socket);
                Thread thread = new Thread(dealTask);
                thread.start();
                System.out.println("开始提供服务");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initPropertis() {
        File propertiesFile = new File("config.properties");
        if (propertiesFile.exists()) {
            Field[] declaredFields = Main.class.getDeclaredFields();

            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(propertiesFile));
                Set<String> strings = properties.stringPropertyNames();
                Iterator<String> iterator = strings.iterator();
                while (iterator.hasNext()) {
                    String next = iterator.next();
                    Field field;
                    if ((field = isIn(declaredFields, next)) != null) {
                        try {
                            if (field.getType().getSimpleName().equals("int")) {
                                field.setInt(Main.class, Integer.parseInt(properties.getProperty(next)));
                            } else if (field.getType().getSimpleName().equals("boolean")) {
                                field.setBoolean(Main.class, Boolean.parseBoolean(properties.getProperty(next)));
                            } else if (field.getType().getSimpleName().equals("long")) {
                                field.setLong(Main.class, Long.parseLong(properties.getProperty(next)));
                            } else {
                                field.set(Main.class, properties.getProperty(next));
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Field isIn(Field[] declaredFields, String next) {
        for (Field declaredField : declaredFields) {
            if (declaredField.getName().equals(next)) {
                return declaredField;
            }
        }
        return null;
    }
}
