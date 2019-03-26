package top.jxnu;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import top.itreatment.net.bean.CommonBean;
import top.itreatment.net.bean.SeatBean;
import top.itreatment.net.core.impl.ChooseSeatClientImpl;
import top.itreatment.net.res.Resource;
import top.itreatment.net.util.Util;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.*;

public class DealTask implements Runnable {


    private static BufferedReader br;
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private ChooseSeatClientImpl chooseSeatClient;
    private Map<String, Method> methodMap;
    private static String defaultEnding = "utf-8";

    public static DealTask dealSocket(Socket socket) throws IOException {
        DealTask dealTask = new DealTask();
        dealTask.socket = socket;
        dealTask.is = socket.getInputStream();
        dealTask.os = socket.getOutputStream();
        br = new BufferedReader(new InputStreamReader(dealTask.is, defaultEnding));

        dealTask.chooseSeatClient = new ChooseSeatClientImpl();
        dealTask.methodMap = new HashMap<>();
        Method[] methods = ChooseSeatClientImpl.class.getMethods();
        for (Method method : methods) {
            String name = method.getName();
            dealTask.methodMap.put(name, method);
        }
        return dealTask;
    }


    @Override
    public void run() {
        Gson gson = new Gson();
        while (true) {
            try {
                socket.sendUrgentData(0);
                String jsonStr = br.readLine();
                MyXy myXy = gson.fromJson(jsonStr, MyXy.class);
                if (myXy == null || myXy.method == null) continue;
                if ("exit".equalsIgnoreCase(myXy.method)) break;

                CommonBean commonBean = (CommonBean) dealMethod(myXy);
                JsonObject jsonObj = gson.toJsonTree(commonBean).getAsJsonObject();
                jsonObj.addProperty("id", myXy.id);

                if ("login".equals(myXy.method)) {
                    jsonObj.add("cookies", chooseSeatClient.getCookie() == null ? null : gson.toJsonTree(chooseSeatClient.getCookie()));
                }

                String responseText = gson.toJson(jsonObj);

                os.write(responseText.getBytes(defaultEnding));
                os.write("\n".getBytes(defaultEnding));
                os.flush();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                break;
            }
        }
        close();
    }

    /*
    else if ("lockSeats".equals(myXy.method)) {
            int beginTime = Integer.parseInt(myXy.params.get("beginTime").toString());
            int duration = Integer.parseInt(myXy.params.get("duration").toString());
            ArrayList seatsList = (ArrayList) myXy.params.get("seats");
            String[] seats = new String[seatsList.size()];
            Util.toArray(seatsList, seats);
            com = chooseSeatClient.lockSeats(beginTime, duration, seats);
        }
        */
    private Object dealMethod(MyXy myXy) {
        Method method = methodMap.get(myXy.method);
        Object com = null;
        if (myXy.cookies != null && !myXy.cookies.isEmpty()) {
            chooseSeatClient.setCookie(myXy.cookies);
        }
        if (myXy.userInfo != null) {
            chooseSeatClient.setUserBean(myXy.userInfo);
        }
        if (method == null) {
            return new CommonBean(Resource.FAIL, "方法没有找到");
        } else if ("getBlankPOIs".equals(myXy.method)) {
            String contentld = (String) myXy.params.get("contentld");
            CommonBean<List<SeatBean>> blankPOIs = chooseSeatClient.getBlankPOIs(contentld);
            if (blankPOIs.getStatus().equals(Resource.SUCCESS)) {
                CommonBean<List<SimpleSeatBean>> simpleSeatBeanCommonBean = new CommonBean<>();
                List<SimpleSeatBean> simpleSeatBeans = new ArrayList<>();
                simpleSeatBeanCommonBean.setData(simpleSeatBeans);
                simpleSeatBeanCommonBean.setStatus(Resource.SUCCESS);
                for (SeatBean datum : blankPOIs.getData()) {
                    SimpleSeatBean simpleSeatBean = new SimpleSeatBean(datum.getId(), datum.getState(), datum.getTitle(), datum.getX(), datum.getY());
                    simpleSeatBeans.add(simpleSeatBean);
                }
                com = simpleSeatBeanCommonBean;
            } else {
                com = blankPOIs;
            }
        } else {
            Class<?>[] parameterTypes = method.getParameterTypes();
            int parameterCount = parameterTypes.length;

            Object[] objects = new Object[parameterCount];
            for (int i = 1; i <= parameterCount; i++) {
                String key = String.valueOf(i);
                if (myXy.params != null && myXy.params.get(key) != null) {
                    Object o = myXy.params.get(key);
                    String simpleTypeName = parameterTypes[i - 1].getSimpleName();
                    switch (simpleTypeName) {
                        case "Integer":
                            objects[i - 1] = Integer.parseInt((String) o);
                            break;
                        case "Double":
                            objects[i - 1] = Double.parseDouble((String) o);
                            break;
                        case "Boolean":
                            objects[i - 1] = Boolean.parseBoolean((String) o);
                            break;
                        case "Long":
                            objects[i - 1] = Long.parseLong((String) o);
                            break;
                        case "String[]":
                            ArrayList<String> s_aos = (ArrayList<String>) o;
                            String[] s_sos = new String[s_aos.size()];
                            Util.toArray(s_aos, s_sos);
                            objects[i - 1] = s_sos;
                            break;
                        case "int[]":
                        case "Integer[]":
                            ArrayList<Integer> i_aos = (ArrayList<Integer>) o;
                            Integer[] i_sos = new Integer[i_aos.size()];
                            Util.toArray(i_aos, i_sos);
                            objects[i - 1] = i_sos;
                            break;
                        default:
                            objects[i - 1] = o;
                    }
                }
            }
            try {
                System.out.println("开始执行方法："+myXy.method+",参数是:"+Arrays.asList(objects));
                com = method.invoke(chooseSeatClient, objects);

                Gson gson = new Gson();
                System.out.println(gson.toJson(com));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                com = new CommonBean(Resource.ERROR, "方法执行错误");
            }
        }
        return com;
    }

    private void close() {
        try {
            br.close();
            os.close();
            is.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
