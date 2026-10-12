package io.github.ahadporkar.ordertocash.governance;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
/** Shared validation and deterministic encoding. Canonical JSON is project-local, not RFC 8785. */
public final class Data {
    private Data() {}
    public static Map<String,Object> map(Object... pairs) {
        if(pairs.length%2!=0) throw new IllegalArgumentException();
        var m=new LinkedHashMap<String,Object>();
        for(int i=0;i<pairs.length;i+=2)m.put((String)pairs[i],pairs[i+1]);
        return m;
    }
    public static Map<String,Object> copy(Map<String,Object> m) { return Json.object(Json.copy(m)); }
    public static String str(Map<String,Object> m,String key,int max) {
        Object v=m.get(key);
        Problem.require(v instanceof String && !((String)v).isBlank() && ((String)v).length()<=max,400,"INVALID_"+key.toUpperCase(Locale.ROOT));
        return (String)v;
    }
    public static String id(String v) {
        Problem.require(v!=null && v.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,99}"),400,"INVALID_ID"); return v;
    }
    public static long integer(Map<String,Object> m,String key,long min,long max) {
        Problem.require(m.get(key) instanceof Number,400,"INVALID_"+key.toUpperCase(Locale.ROOT));
        long v=Json.number(m,key,0); Problem.require(v>=min && v<=max,400,"INVALID_"+key.toUpperCase(Locale.ROOT));return v;
    }
    public static void keys(Map<String,Object> m,String... names) {
        var allowed=Set.of(names);Problem.require(allowed.equals(m.keySet()),400,"INVALID_FIELDS");
    }
    public static String sha(String s) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8))); }
        catch(NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static boolean equal(String a,String b) {
        return a!=null&&b!=null&&MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),b.getBytes(StandardCharsets.UTF_8));
    }
    public static String randomToken() { byte[] b=new byte[32];new SecureRandom().nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b); }
    public static String orderKey(String tenant,String id) { return Data.id(tenant)+"/"+Data.id(id); }
}
