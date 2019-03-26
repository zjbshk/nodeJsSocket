package top.jxnu;

import top.itreatment.net.bean.UserBean;

import java.util.Map;

public class MyXy {

    public String id;

    public String method;

    public Map<String, Object> params;

    public Map<String, String> cookies;

    public UserBean userInfo;

    @Override
    public String toString() {
        return "MyXy{" +
                "id='" + id + '\'' +
                ", method='" + method + '\'' +
                ", params=" + params +
                ", cookie=" + cookies +
                ", userBean=" + userInfo +
                '}';
    }
}
