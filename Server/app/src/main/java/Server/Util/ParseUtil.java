package Server.Util;



import org.json.JSONObject;



public class ParseUtil 
{
    public static JSONObject parseJSON(String temp)
    {
       JSONObject json=null;
        try
        {       json=new JSONObject(temp);
                if(!json.has("REQUESTTYPE"))
                    json=null;
        }
        catch(Exception e)
        {   
            System.out.println("An Error Occured While Parsing the JSON in ParseUtil");
        }
     return json;
    }

    
}
