package com.sten1ee.olli;

import java.util.Collection;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class TopEnv extends HashMapEnv {

    TopEnv() {
        super(null); // null is for 'no parentEnv'

        defineAll(PredefSymbol.predefSymbols.values());
    }

    void  defineAll(Collection<PredefSymbol> predefSymbols) {
        for (PredefSymbol ps : predefSymbols) {
            boolean res = localDefine(ps, ps.val);
            assert res : "local(pre)Define failed " + ps.sym;
        }
    }
}
