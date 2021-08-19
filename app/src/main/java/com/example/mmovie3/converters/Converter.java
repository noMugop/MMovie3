package com.example.mmovie3.converters;

import androidx.room.TypeConverter;

import com.example.mmovie3.pojo.TestClass;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

//Вот так это выглядит в ручную, но можно воспользоваться GSON
/*public class Converter {
    public String listTestClassToString(List<TestClass> testClasses) {
        JSONArray jsonArray = new JSONArray();
        for(TestClass testClass : testClasses) {
            //в цикле foreach, каждый объект testClass, взятый из testClasses, приобразовываем в JSONObject
            JSONObject jsonObject = new JSONObject();
            try {
                //копируя в jsonObject каждое поле TestClass объекта
                jsonObject.put("testClassId", testClass.getTestClassId());
                jsonObject.put("name", testClass.getName());
                //затем полученный JSONObject ложим в JSONArray
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //возвращаем массив JSONObject-ов в виде строки
        return jsonArray.toString();
    }
}*/

public class Converter {

    //этот короткий код заменяет весь выше написанный код, TypeConverter значит, что это конвертер
    @TypeConverter
    public String listTestClassToString(List<TestClass> testClasses) {
        return new Gson().toJson(testClasses);
    }

    //метод, который приобразовывает строки обратно в объекты
    @TypeConverter
    public List<TestClass> stringToListTestClass(String testClassesAsString) {
        //объект Gson умеет приобразовывать объекты в JSON и обратно
        Gson gson = new Gson();
        //здесь вместо ArrayList<TestClass> пишем просто ArrayList, что в данном случае равносильно, и получаем объекты типа Object
        ArrayList objects =  gson.fromJson(testClassesAsString, ArrayList.class);
        //теперь нужно приобразовать полученные Object в TestClass
        ArrayList<TestClass> testClasses = new ArrayList<>();
        for(Object o : objects) {
            //приобразовываем Object в TestClass сразу во время добавления в коллекцию
            testClasses.add(gson.fromJson(o.toString(), TestClass.class));
        }
        return testClasses;
    }
}
