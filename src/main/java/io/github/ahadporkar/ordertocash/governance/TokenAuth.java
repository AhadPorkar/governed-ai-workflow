package io.github.ahadporkar.ordertocash.governance;
import java.time.*;
import java.util.*;
/** Static, hashed, expiring bearer credentials for this reference application. Production OIDC is a release gate. */
public final class TokenAuth {
    private record Credential(Actor actor,String hash,Instant expires) {}
    private final List<Credential> credentials;
    private final Clock clock;
    public TokenAuth(Map<String,Object> config,Clock clock) {
        this.clock=clock;var list=new ArrayList<Credential>();var hashes=new HashSet<String>();var identities=new HashSet<String>();
        for(Object value:Json.list(config,"identities")) {
            var row=Json.object(value);String hash=Data.str(row,"tokenHash",64);
            Problem.require(hash.matches("[a-f0-9]{64}")&&hashes.add(hash),500,"INVALID_AUTH_CONFIG");
            Set<String> roles=new HashSet<>();
            for(Object role:Json.list(row,"roles")) {Problem.require(role instanceof String&&Set.of("READ","OPERATOR","REVIEWER","WORKER","ADMIN").contains(role),500,"INVALID_ROLE");roles.add((String)role);}
            Actor actor=new Actor(Data.str(row,"id",100),Data.str(row,"tenant",100),roles);
            Problem.require(identities.add(actor.tenant()+"/"+actor.id()),500,"DUPLICATE_IDENTITY");
            list.add(new Credential(actor,hash,Instant.parse(Data.str(row,"expiresAt",40))));
        }
        Problem.require(!list.isEmpty()&&list.size()<=100,500,"INVALID_AUTH_CONFIG");credentials=List.copyOf(list);
    }
    public Actor authenticate(String header) {
        Problem.require(header!=null&&header.startsWith("Bearer ")&&header.length()<=200,401,"AUTHENTICATION_REQUIRED");
        String token=header.substring(7);Problem.require(token.matches("[A-Za-z0-9_-]{40,100}"),401,"AUTHENTICATION_REQUIRED");
        String hash=Data.sha(token);Actor found=null;
        for(var c:credentials)if(Data.equal(hash,c.hash())&&clock.instant().isBefore(c.expires()))found=c.actor();
        Problem.require(found!=null,401,"AUTHENTICATION_REQUIRED");return found;
    }
}
