package com.fenjiread.learner.activity.utils;


import android.content.Context;
import android.widget.Toast;


import com.fenjiread.learner.activity.activity.Constants;
import com.fenjiread.learner.activity.model.BookPayPostInfo;
import com.fenjiread.learner.activity.model.OrederSendInfo;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by xmg on 2016/12/5.
 */

public class WXpayUtils {

    private static IWXAPI iwxapi;
    private static PayReq req;
    private static Context mContext;

    public static IWXAPI getWXAPI(Context context){
        mContext = context;
        if (iwxapi == null){
            //通过WXAPIFactory创建IWAPI实例
            iwxapi = WXAPIFactory.createWXAPI(context, null);
            req = new PayReq();
            //将应用的appid注册到微信
            iwxapi.registerApp(Constants.APP_ID);
        }
        return iwxapi;
    }

    //生成随机字符串
    public static String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    //获得时间戳
    private static long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    //生成预支付随机签名
    public static  String genSign(OrederSendInfo info) {
        StringBuffer sb = new StringBuffer(info.toString());
        if (Constants.API_KEY.equals("")){
            Toast.makeText(mContext,"APP_ID为空",Toast.LENGTH_LONG).show();
        }
        //拼接密钥
        sb.append("key=");
        sb.append(Constants.API_KEY);

        String appSign = MD5.getMessageDigest(sb.toString().getBytes());

        return appSign;
    }
    //生成预支付随机签名
    public static  String genSign(BookPayPostInfo info) {
        StringBuffer sb = new StringBuffer(info.toString());
        if (Constants.API_KEY.equals("")){
            Toast.makeText(mContext,"APP_ID为空",Toast.LENGTH_LONG).show();
        }
        //拼接密钥
        sb.append("key=");
        sb.append(Constants.API_KEY);

        String appSign = MD5.getMessageDigest(sb.toString().getBytes());

        return appSign;
    }
    //生成支付随机签名
    private static String genAppSign(List<OkHttpUtils.Param> params){
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).key);
            sb.append('=');
            sb.append(params.get(i).value);
            sb.append('&');
        }
        //拼接密钥
        sb.append("key=");
        sb.append(Constants.API_KEY);

        String appSign = MD5.getMessageDigest(sb.toString().getBytes());
        return appSign.toUpperCase();
    }

    //生成支付参数
    private static void genPayReq(String prepayid) {
        req.appId = Constants.APP_ID;
        req.partnerId = Constants.MCH_ID;
        req.prepayId = prepayid;
        req.packageValue = "Sign=" + prepayid;
        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());

        List<OkHttpUtils.Param> signParams = new LinkedList<OkHttpUtils.Param>();
        signParams.add(new OkHttpUtils.Param("appid", req.appId));
        signParams.add(new OkHttpUtils.Param("noncestr", req.nonceStr));
        signParams.add(new OkHttpUtils.Param("package", req.packageValue));
        signParams.add(new OkHttpUtils.Param("partnerid", req.partnerId));
        signParams.add(new OkHttpUtils.Param("prepayid", req.prepayId));
        signParams.add(new OkHttpUtils.Param("timestamp", req.timeStamp));

        req.sign = genAppSign(signParams);
    }

    public static void Pay(String prepayid,Context context){
        mContext = context;
        if (judgeCanGo()){
            genPayReq(prepayid);
            iwxapi.registerApp(Constants.APP_ID);
            iwxapi.sendReq(req);
        }
    }

    private static boolean judgeCanGo(){
        getWXAPI(mContext);
        if (!iwxapi.isWXAppInstalled()) {
            Toast.makeText(mContext, "请先安装微信应用", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!iwxapi.isWXAppSupportAPI()) {
            Toast.makeText(mContext, "请先更新微信应用", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}
