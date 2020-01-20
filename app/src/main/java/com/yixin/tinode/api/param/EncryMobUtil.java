package com.yixin.tinode.api.param;


import com.yixin.tinode.api.encrypt.MD5Util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 手机端的加密
 * <p>
 * 1.1.1.服务器端(server)和客户端(client)使用一套密钥对（此处使用不正规，正常会有两套）<br/>
 * 1.1.2. server使用私钥和client使用公钥 <br/>
 * 1.1.3. client使用MD5（参数从小到大的排列 +随机数生成） {取中间8位}作为token，随机数是只由字母和数字组成的16位字符串。
 * <p>
 * 1.1.4. 将token加入到请求参数中(data)，然后转换为json格式<br/>
 * 1.1.5. client使用sever的RSA公钥对token进行加密(sign)。
 * 首先对token进行加密，将密文进行base64编码，最后返回utf-8编码的密文。 <br/>
 * 1.1.6. 分别将data和sign作为参数传输给服务器端。<br/>
 * 1.1.7. server使用server的私钥解密key得到token值，与data中的token对比
 *
 * @author liulei
 * @version 1.0
 * @date 2017年7月5日 下午2:56:00
 */
public class EncryMobUtil {
    private static final String SECRET = "zy1wxhn6qkf9kym1";

    /**
     * 将实体类转换成请求参数,以map<k,v>形式返回
     *
     * @param <T>
     * @return
     */
    private static <T> Map<String, Object> bean2Map(T obj) {
        Class<? extends Object> clazz = obj.getClass();
        Class<? extends Object> superclass = clazz.getSuperclass();

        Field[] fields = clazz.getDeclaredFields();
        Field[] superFields = superclass.getDeclaredFields();

        if (fields == null || fields.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                params.put(field.getName(), field.get(obj));
            }

            for (Field superField : superFields) {
                superField.setAccessible(true);
                params.put(superField.getName(), superField.get(obj));
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return params;
    }

    /**
     * @return String
     * @author liulei
     */
    public static BaseReq encrypt(BaseReq data) {
        Map<String, Object> params = null;
        if (data == null) {
            data = new BaseReq();
        }
        data.setTs(System.currentTimeMillis() / 1000);

        params = bean2Map(data);
        params.remove("token");
        params.put("secret", SECRET);

        String param = "";
        Object[] key = params.keySet().toArray();
        Arrays.sort(key);
        for (int i = 0; i < key.length; i++) {
            Object object = params.get(key[i]);
            if (object != null && object.toString().length() > 0) {
                param = param + key[i] + object.toString();
            }
        }

        data.setToken(MD5Util.encrypt(param));
        return data;
    }

}
