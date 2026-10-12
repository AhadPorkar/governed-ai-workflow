package io.github.ahadporkar.ordertocash.governance;
import java.util.*;
public final class DemoFixtures {
    private DemoFixtures() {}
    public static Map<String,Object> order(String id,String tenant,long captured) {
        return Data.map("id",id,"tenant",tenant,"sku","SKU-HEADSET","status","UNFULFILLED","currency","EUR",
                "totalCents",12900,"capturedCents",captured,"refundedCents",0,"unitPriceCents",12900,"quantity",1,
                "originalStock",0,"supplierId","SUP-ORIGINAL","alternatives",new ArrayList<>(List.of(
                    Data.map("id","SUP-BERLIN","name","Berlin Components (synthetic)","stock",12,"unitPriceCents",12500,"approved",true),
                    Data.map("id","SUP-HAMBURG","name","Hamburg Supply (synthetic)","stock",0,"unitPriceCents",11900,"approved",true),
                    Data.map("id","SUP-UNVERIFIED","name","Unverified supplier (synthetic)","stock",50,"unitPriceCents",9900,"approved",false))));
    }
    public static void seed(GovernanceEngine engine,String tenant) {
        var admin=new Actor("seed-admin",tenant,Set.of("ADMIN"));
        engine.registerOrder(admin,order("ORD-1001",tenant,12900),false);
        engine.registerOrder(admin,order("ORD-1002",tenant,0),false);
        var shipped=order("ORD-1003",tenant,12900);shipped.put("status","SHIPPED");engine.registerOrder(admin,shipped,false);
        engine.registerOrder(admin,order("ORD-1005",tenant,12900),false);
        var noStock=order("ORD-1004",tenant,0);noStock.put("alternatives",List.of());engine.registerOrder(admin,noStock,false);
    }
}
