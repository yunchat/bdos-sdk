package com.bfd.bdos.auth;

/**
 * Created by hi.yan.li on 2016/4/1.
 */
@Deprecated
public enum ShaHmac1Singleton {
    INSTANCE;

    private ISigner signer;

    ShaHmac1Singleton() {
        signer = new ShaHmac1();
    }

    public ISigner getInstance() {
        return this.signer;
    }

}
