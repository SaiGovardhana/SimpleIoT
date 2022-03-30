package Server;

import java.util.HashMap;

import org.json.JSONArray;



public class JSONTest {

    public static void main(String[] args) 
    {
        JSONArray arr=new JSONArray();
        HashMap<String,String>map=new HashMap<>();
        map.put("Hello","world");
        map.put("Bye","world");
        arr.put(map);
        System.out.println(arr); 
    }
    
}
