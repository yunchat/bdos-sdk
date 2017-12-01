package com.bfd.bdos.utils;



import java.lang.reflect.Type;

import com.bfd.google.gson.Gson;


public class GsonUtil {

    public static String beanToJson(Object bean) {
        Gson gson = new Gson();
        return gson.toJson(bean);
    }

    public static <T> T jsonToBean(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return (T)gson.fromJson(json, clazz);
    }
    
    public static <T> T jsonToBean(String json, Type typeOfT) {
        Gson gson = new Gson();
        return (T)gson.fromJson(json, typeOfT);
    }
    
    public static void main(String[] args) {
//    	User user = new User();
//    	user.setUid("uid");
//    	user.setName("name222");
//    	System.out.println(GsonUtil.beanToJson(user));
    }

}
