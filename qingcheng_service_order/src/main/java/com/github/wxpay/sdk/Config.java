package com.github.wxpay.sdk;

import java.io.InputStream;

/**
 * <p>
 * <code>Config</code>
 * </p>
 *
 * @author huiwang45@iflytek.com
 * @description
 * @date 2020/07/23 15:39
 */
public class Config extends WXPayConfig{

    @Override
    public String getAppID() {
        return "wx8397f8696b538317";
    }

    @Override
    public String getMchID() {
        return "1473426802";
    }

    @Override
    public String getKey() {
        return "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb";
    }

    @Override
    InputStream getCertStream() {
        return null;
    }

    @Override
    IWXPayDomain getWXPayDomain() {
        return new IWXPayDomain() {
            @Override
            public void report(String s, long l, Exception e) {

            }

            @Override
            public DomainInfo getDomain(WXPayConfig wxPayConfig) {
                return new DomainInfo("api.mch.weixin.qq.com", true);
            }
        };
    }
}
